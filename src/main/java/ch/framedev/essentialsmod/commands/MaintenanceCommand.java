package ch.framedev.essentialsmod.commands;

import ch.framedev.essentialsmod.utils.Config;
import ch.framedev.essentialsmod.utils.OfflinePlayerUUID;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;

import java.util.*;

@SuppressWarnings({"MismatchedQueryAndUpdateOfCollection", "unchecked"})
public class MaintenanceCommand implements ICommand {

    private static final String PLAYERS_KEY = "players";
    private static final String ENABLED_KEY = "enabled";
    public static final Map<String,Object> DEFAULT_MAP = (Map<String, Object>) new Config().getConfig().getData().getOrDefault("maintenance", new HashMap<String,Object>());
    public static final String MOTD_MAINTENANCE = "The server is currently not available\nMaintenance!";
    public static final String MOTD_ACTIVE = "Welcome to the server!";

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("maintenance")
                .requires(source -> source.hasPermission(3))
                .then(Commands.argument("playerName", StringArgumentType.word())
                        .suggests(PLAYER_SUGGESTION)
                        .executes(this::handlePlayer))
                .executes(this::toggleMaintenanceMode);
    }

    private static final SuggestionProvider<CommandSourceStack> PLAYER_SUGGESTION = (context, builder) -> {
        // Add player names to the suggestions
        context.getSource().getServer().getPlayerList().getPlayers().stream().map(player -> player.getGameProfile().getName()).forEach(builder::suggest);
        return builder.buildFuture();
    };

    private int handlePlayer(CommandContext<CommandSourceStack> command) {
        Config config = new Config();
        String playerName = command.getArgument("playerName", String.class);

        UUID uuid = OfflinePlayerUUID.getUUIDFromMojang(playerName);
        if (uuid == null) {
            sendFailure(command);
            return 0;
        }

        // Retrieve or initialize maintenance players list
        List<String> uuids = (List<String>) DEFAULT_MAP.get(PLAYERS_KEY);
        if (uuids == null) {
            uuids = new ArrayList<>();
        }

        // Toggle player's status in the maintenance list
        if (uuids.contains(uuid.toString())) {
            uuids.remove(uuid.toString());
            sendSuccess(command, playerName + " has been removed from the maintenance list!", ChatFormatting.GREEN);
        } else {
            uuids.add(uuid.toString());
            sendSuccess(command, playerName + " has been added to the maintenance list!", ChatFormatting.GREEN);
        }

        // Save updated list
        DEFAULT_MAP.put(PLAYERS_KEY, uuids);
        config.getConfig().set("maintenance", DEFAULT_MAP);
        config.getConfig().save();
        return 1;
    }

    private int toggleMaintenanceMode(CommandContext<CommandSourceStack> command) {
        Config config = new Config();

        // Retrieve or initialize maintenance enabled status
        boolean isEnabled = (boolean) DEFAULT_MAP.getOrDefault(ENABLED_KEY, false);
        DEFAULT_MAP.put(ENABLED_KEY, !isEnabled);
        config.getConfig().set("maintenance", DEFAULT_MAP);
        config.getConfig().save();

        String status = isEnabled ? "disabled" : "enabled";
        sendSuccess(command, "Maintenance mode has been " + status + "!", status.equalsIgnoreCase("enabled") ? ChatFormatting.RED : ChatFormatting.GREEN);

        if (isEnabled) {
            command.getSource().getServer().setMotd(MOTD_ACTIVE);
            for (ServerPlayer serverPlayer : command.getSource().getServer().getPlayerList().getPlayers()) {
                serverPlayer.connection.send(new net.minecraft.network.protocol.game.ClientboundTabListPacket(
                        new TextComponent(""), // Empty header
                        new TextComponent("")  // Empty footer
                ));
            }
        } else {
            command.getSource().getServer().setMotd(MOTD_MAINTENANCE);
            String tabHeader = (String) DEFAULT_MAP.getOrDefault("tabHeader", "This server is in maintenance mode!");
            for (ServerPlayer serverPlayer : command.getSource().getServer().getPlayerList().getPlayers()) {
                serverPlayer.connection.send(new net.minecraft.network.protocol.game.ClientboundTabListPacket(
                        new TextComponent(tabHeader).withStyle(ChatFormatting.RED),
                        new TextComponent("")
                ));
            }
        }
        return 1;
    }

    private void sendSuccess(CommandContext<CommandSourceStack> command, String message, ChatFormatting chatFormatting) {
        command.getSource().sendSuccess(new TextComponent(message).withStyle(chatFormatting), true);
    }

    private void sendFailure(CommandContext<CommandSourceStack> command) {
        command.getSource().sendFailure(new TextComponent("Player not found!").withStyle(ChatFormatting.RED));
    }
}