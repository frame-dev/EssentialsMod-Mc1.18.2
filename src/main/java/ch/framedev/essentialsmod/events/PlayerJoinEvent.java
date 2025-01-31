package ch.framedev.essentialsmod.events;

import ch.framedev.essentialsmod.commands.MaintenanceCommand;
import ch.framedev.essentialsmod.commands.VanishCommand;
import ch.framedev.essentialsmod.utils.Config;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.status.ServerStatus;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings({"MismatchedQueryAndUpdateOfCollection", "unchecked"})
@Mod.EventBusSubscriber(modid = "essentials") // Replace with your mod's ID
public class PlayerJoinEvent {

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            if (player.getServer() == null) return;
            PlayerList playerList = player.getServer().getPlayerList();

            // Check if player is vanished
            if (VanishCommand.vanishList.contains(player.getName().getString())) {
                for (ServerPlayer otherPlayer : playerList.getPlayers()) {
                    if (otherPlayer != player && !VanishCommand.vanishList.contains(otherPlayer.getName().getString()) && !otherPlayer.hasPermissions(2)) {
                        // Hide vanished player from others
                        otherPlayer.connection.send(new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.REMOVE_PLAYER, player));
                        otherPlayer.connection.send(new ClientboundRemoveEntitiesPacket(player.getId()));
                    }
                }
                // Hide default join message by removing the player temporarily
                playerList.getPlayers().remove(player);
            } else {
                // Hide vanished players from the joining player
                for (ServerPlayer otherPlayer : playerList.getPlayers()) {
                    if (otherPlayer != player && VanishCommand.vanishList.contains(otherPlayer.getName().getString()) && !player.hasPermissions(2)) {
                        player.connection.send(new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.REMOVE_PLAYER, otherPlayer));
                        player.connection.send(new ClientboundRemoveEntitiesPacket(otherPlayer.getId()));
                    }
                }
            }
            Config config = new Config();
            Map<String, Object> defaultMap = (Map<String, Object>) config.getConfig().getData().getOrDefault("maintenance", new HashMap<String, Object>());
            boolean isMaintenanceMode = (boolean) defaultMap.getOrDefault("enabled", false);
            if (isMaintenanceMode) {
                List<String> uuids = (List<String>) defaultMap.getOrDefault("players", new ArrayList<>());
                if (uuids.contains(player.getUUID().toString())) {
                    String tabHeader = (String) defaultMap.getOrDefault("tabHeader", "This server is in maintenance mode!");
                    for (ServerPlayer serverPlayer : player.getServer().getPlayerList().getPlayers()) {
                        serverPlayer.connection.send(new net.minecraft.network.protocol.game.ClientboundTabListPacket(
                                new TextComponent(tabHeader).withStyle(ChatFormatting.RED),
                                new TextComponent("")
                        ));
                    }
                } else {
                    player.sendMessage(new net.minecraft.network.chat.TextComponent("Server is in maintenance mode."), player.getUUID());
                    player.connection.disconnect(new TextComponent("Server is in maintenance mode!").withStyle(ChatFormatting.RED));
                    event.setResult(Event.Result.DENY);
                }
            }
        }
    }

    private static DedicatedServer server;
    private static Boolean cachedMaintenanceMode = null; // Cache for maintenance mode to prevent unnecessary updates


    @SubscribeEvent
    public static void onServerStarting(net.minecraftforge.event.server.ServerStartingEvent event) {
        MinecraftServer eventServer = event.getServer();
        if (eventServer instanceof DedicatedServer) {
            server = (DedicatedServer) eventServer;
        }
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            return; // Only process during the END phase
        }
        if (server != null) {
            updateMotdIfNeeded();
        }
    }

    private static void updateMotdIfNeeded() {
        // Retrieve maintenance mode from the config
        Map<String, Object> maintenanceMap = MaintenanceCommand.DEFAULT_MAP;
        Config config = new Config();

        boolean maintenanceEnabled = (boolean) maintenanceMap.getOrDefault("enabled", false);

        // Check if the MOTD needs to be updated
        if (cachedMaintenanceMode == null || cachedMaintenanceMode != maintenanceEnabled) {
            cachedMaintenanceMode = maintenanceEnabled;
            String newMotd;
            if (maintenanceEnabled) {
                newMotd = "\\u00a7c\\u00a7lThe server is currently under maintenance!\\nPlease try again later.";
            } else {
                newMotd = config.getConfig().getString("defaultMotd");
            }

            server.setMotd(newMotd);
            ServerStatus status = server.getStatus();
            status.setDescription(new TextComponent(parseUnicodeEscapeSequences(newMotd)));

            updateServerProperties(newMotd); // Update the server.properties file as well for the server to reflect the new MOTD immediately.

            System.out.println("Updated MOTD: " + newMotd);
        }
    }

    private static void updateServerProperties(String value) {
        File propertiesFile = new File(server.getServerDirectory(), "server.properties");
        File backupFile = new File(server.getServerDirectory(), "server.properties.bak");
        Properties properties = new Properties();

        // Ensure the file exists
        if (!propertiesFile.exists()) {
            System.err.println("[EssentialsMod] server.properties file not found! Skipping MOTD update.");
            return;
        }

        // Load properties from the file
        try (FileInputStream in = new FileInputStream(propertiesFile)) {
            properties.load(in);
            System.out.println("[EssentialsMod] Loaded properties from server.properties:");
            for (String key : properties.stringPropertyNames()) {
                System.out.println("  " + key + "=" + properties.getProperty(key));
            }
        } catch (IOException e) {
            System.err.println("[EssentialsMod] Failed to load server.properties: " + e.getMessage());
            return;
        }

        // Backup the original file
        try {
            if (!backupFile.exists()) {
                if (propertiesFile.renameTo(backupFile)) {
                    System.out.println("[EssentialsMod] Created backup of server.properties as server.properties.bak.");
                } else {
                    System.err.println("[EssentialsMod] Failed to create backup of server.properties.");
                }
            }
        } catch (Exception e) {
            System.err.println("[EssentialsMod] Failed to create backup of server.properties: " + e.getMessage());
        }

        // Update the MOTD and save the file
        try {
            // Update the desired property
            properties.setProperty("motd", parseUnicodeEscapeSequences(value.replaceAll("§", ""))); // Strip Minecraft color codes

            // Save the updated properties back to the file
            try (FileOutputStream out = new FileOutputStream(propertiesFile)) {
                properties.store(out, "Updated by EssentialsMod");
            }

            System.out.println("[EssentialsMod] Successfully updated server.properties with new MOTD.");
        } catch (IOException e) {
            System.err.println("[EssentialsMod] Failed to update server.properties: " + e.getMessage());
        }
    }

    public static String parseUnicodeEscapeSequences(String input) {
        Pattern unicodePattern = Pattern.compile("\\\\u([0-9A-Fa-f]{4})");
        Matcher matcher = unicodePattern.matcher(input);
        StringBuilder result = new StringBuilder();

        while (matcher.find()) {
            // Parse the Unicode code point (e.g., "00a7") to a character
            String unicodeChar = String.valueOf((char) Integer.parseInt(matcher.group(1), 16));
            matcher.appendReplacement(result, unicodeChar);
        }
        matcher.appendTail(result);
        return result.toString().replace("\\n", "\n"); // Handle escaped newlines
    }
}
