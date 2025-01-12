package ch.framedev.essentialsmod.commands;

import ch.framedev.essentialsmod.utils.Config;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashSet;
import java.util.Set;

public class MuteCommand {

    public static Set<String> mutedPlayers = new HashSet<>(); // Store muted player names

    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("mute")
                .requires(commandSourceStack -> commandSourceStack.hasPermission(2)) // Restrict to operators or permission level 2+
                .then(Commands.argument("playerName", StringArgumentType.word())
                        .suggests(PLAYER_SUGGESTION)
                        .executes(MuteCommand::executeWithPlayerName)); // Executes for a specific player
    }

    private static int executeWithPlayerName(CommandContext<CommandSourceStack> command) {
        String playerName = StringArgumentType.getString(command, "playerName");
        CommandSourceStack source = command.getSource();
        ServerPlayer targetPlayer = source.getServer().getPlayerList().getPlayerByName(playerName);

        if (targetPlayer != null) {
            if (mutedPlayers.contains(playerName)) {
                mutedPlayers.remove(playerName); // Unmute the player
                source.sendSuccess(new TextComponent(playerName + " has been unmuted."), true);
                targetPlayer.sendMessage(new TextComponent("You have been unmuted by an admin."), Util.NIL_UUID);
                Config config = new Config();
                config.getConfig().set("muted", mutedPlayers.stream().toList());
                config.getConfig().save();
            } else {
                mutedPlayers.add(playerName); // Mute the player
                source.sendSuccess(new TextComponent(playerName + " has been muted."), true);
                targetPlayer.sendMessage(new TextComponent("You have been muted by an admin."), Util.NIL_UUID);
                Config config = new Config();
                config.getConfig().set("muted", mutedPlayers.stream().toList());
                config.getConfig().save();
            }
            return 1; // Command executed successfully
        } else {
            source.sendFailure(new TextComponent("Player not found: " + playerName));
            return 0; // Command failed
        }
    }

    public static boolean isPlayerMuted(String playerName) {
        return mutedPlayers.contains(playerName);
    }

    private static final SuggestionProvider<CommandSourceStack> PLAYER_SUGGESTION = (context, builder) -> {
        for (ServerPlayer player : context.getSource().getServer().getPlayerList().getPlayers()) {
            if (!VanishCommand.vanishList.contains(player.getGameProfile().getName()))
                builder.suggest(player.getGameProfile().getName()); // Add player names to the suggestions
        }
        return builder.buildFuture();
    };
}
