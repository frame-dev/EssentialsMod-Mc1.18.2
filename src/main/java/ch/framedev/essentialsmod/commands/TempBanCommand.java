package ch.framedev.essentialsmod.commands;

import ch.framedev.essentialsmod.utils.Config;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TempBanCommand implements ICommand {

    // Stores banned players and their ban details
    public static final Map<UUID, BanDetails> tempBanList = new HashMap<>();

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("tempban")
                .requires(source -> source.hasPermission(4))
                .then(Commands.argument("playerName", StringArgumentType.word())
                        .then(Commands.argument("duration", StringArgumentType.word())
                                .then(Commands.argument("reason", StringArgumentType.greedyString())
                                        .executes(this::execute))));
    }

    private int execute(CommandContext<CommandSourceStack> command) {
        String playerName = command.getArgument("playerName", String.class);
        String durationArg = command.getArgument("duration", String.class);
        String reason = command.getArgument("reason", String.class);

        // Parse the duration
        long durationInMillis = parseDuration(durationArg);
        if (durationInMillis <= 0) {
            command.getSource().sendFailure(new TextComponent("Invalid duration format! Use 1h, 10m, 1d, etc."));
            return 0;
        }

        ServerPlayer targetPlayer = command.getSource().getServer().getPlayerList().getPlayerByName(playerName);
        if (targetPlayer == null) {
            command.getSource().sendFailure(new TextComponent("Player not found!"));
            return 0;
        }

        UUID playerId = targetPlayer.getUUID();
        Instant unbanTime = Instant.now().plusMillis(durationInMillis);

        tempBanList.put(playerId, new BanDetails(unbanTime, reason));

        // Notify the player and command executor
        targetPlayer.connection.disconnect(new TextComponent("You are banned for: " + reason + ". Remaining time: " + formatDuration(durationInMillis)));
        command.getSource().sendSuccess(new TextComponent(playerName + " has been banned for " + formatDuration(durationInMillis) + ". Reason: " + reason), false);

        Config config = new Config();
        config.getConfig().set("tempBan." + playerId, tempBanList.get(playerId).toMap());
        config.getConfig().save();
        return 1;
    }

    public static boolean isPlayerBanned(ServerPlayer player) {
        UUID playerId = player.getUUID();
        if (!tempBanList.containsKey(playerId)) {
            return false;
        }

        BanDetails banDetails = tempBanList.get(playerId);
        if (Instant.now().isAfter(banDetails.unbanTime)) {
            // Remove expired ban
            tempBanList.remove(playerId);
            return false;
        }
        return true;
    }

    public static boolean isPlayerBanned(UUID player) {
        if (!tempBanList.containsKey(player)) {
            return false;
        }

        BanDetails banDetails = tempBanList.get(player);
        if (Instant.now().isAfter(banDetails.unbanTime)) {
            // Remove expired ban
            tempBanList.remove(player);
            return false;
        }
        return true;
    }

    public static BanDetails getBanDetails(ServerPlayer player) {
        return tempBanList.get(player.getUUID());
    }

    public static BanDetails getBanDetails(UUID uuid) {
        return tempBanList.get(uuid);
    }

    private static long parseDuration(String duration) {
        try {
            if (duration.endsWith("d")) {
                return Long.parseLong(duration.replace("d", "")) * 24 * 60 * 60 * 1000L;
            } else if (duration.endsWith("h")) {
                return Long.parseLong(duration.replace("h", "")) * 60 * 60 * 1000L;
            } else if (duration.endsWith("m")) {
                return Long.parseLong(duration.replace("m", "")) * 60 * 1000L;
            }
        } catch (NumberFormatException ignored) {
        }
        return -1;
    }

    private static String formatDuration(long millis) {
        long seconds = millis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (days > 0) return days + "d " + (hours % 24) + "h";
        if (hours > 0) return hours + "h " + (minutes % 60) + "m";
        if (minutes > 0) return minutes + "m " + (seconds % 60) + "s";
        return seconds + "s";
    }

    public static class BanDetails {
        public final Instant unbanTime;
        public final String reason;

        public BanDetails(Instant unbanTime, String reason) {
            this.unbanTime = unbanTime;
            this.reason = reason;
        }

        public Map<String, Object> toMap() {
            return Map.of("unbanTime", unbanTime.toEpochMilli(), "reason", reason);
        }

        public static BanDetails fromMap(Map<String, Object> map) {
            Instant unbanTime = Instant.ofEpochMilli((long) map.get("unbanTime"));
            String reason = (String) map.get("reason");
            return new BanDetails(unbanTime, reason);
        }

        public static String formatDuration(long millis) {
            long seconds = millis / 1000;
            long minutes = seconds / 60;
            long hours = minutes / 60;
            long days = hours / 24;

            if (days > 0) return days + "d " + (hours % 24) + "h";
            if (hours > 0) return hours + "h " + (minutes % 60) + "m";
            if (minutes > 0) return minutes + "m " + (seconds % 60) + "s";
            return seconds + "s";
        }
    }

    public static void checkBanDuringLogin(ServerLoginPacketListenerImpl loginListener, Connection connection) {
        UUID playerUUID = loginListener.gameProfile.getId();

        TempBanCommand.BanDetails banDetails = TempBanCommand.getBanDetails(playerUUID);
        if (banDetails != null && isPlayerBanned(loginListener.gameProfile.getId())) {
            Instant now = Instant.now();
            Duration remaining = Duration.between(now, banDetails.unbanTime);

            if (remaining.isNegative() || remaining.isZero()) {
                // Unban expired players
                tempBanList.remove(playerUUID);
                Config config = new Config();
                config.getConfig().set("tempBan." + playerUUID, null);
                config.getConfig().save();
                return;
            }

            // Construct the ban message
            String banMessage = "You are temporarily banned.\n" +
                    "Reason: " + banDetails.reason + "\n" +
                    "Remaining Time: " + formatDuration(remaining.toMillis());

            // Disconnect the player with the custom ban message
            connection.disconnect(new TextComponent(banMessage));
        }
    }

    @Mod.EventBusSubscriber(modid = "essentials") // Replace with your mod's ID
    public static class BanListener {
        @SubscribeEvent
        public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
            if (event.getPlayer() instanceof ServerPlayer player) {
                BanDetails banDetails = getBanDetails(player);
                if (isPlayerBanned(player)) {
                    if (banDetails != null) {
                        // Schedule the disconnection on the server thread
                        player.server.execute(() -> {
                            Instant now = Instant.now();
                            Duration remaining = Duration.between(now, banDetails.unbanTime);

                            String banMessage = "You are temporarily banned.\n" +
                                    "Reason: " + banDetails.reason + "\n" +
                                    "Remaining Time: " + formatDuration(remaining.toMillis());

                            // Disconnect the banned player
                            player.connection.disconnect(new TextComponent(banMessage));
                        });
                    }
                }
            }
        }
    }
}
