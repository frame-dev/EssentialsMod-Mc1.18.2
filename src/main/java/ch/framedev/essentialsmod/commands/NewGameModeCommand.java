package ch.framedev.essentialsmod.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;

public class NewGameModeCommand {

    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("gm")
                .requires(source -> source.hasPermission(3)) // Restrict to operators (level 3 or higher)
                .then(Commands.argument("mode", StringArgumentType.word())
                        .suggests((context, builder) -> builder
                                .suggest("s")
                                .suggest("c")
                                .suggest("a")
                                .suggest("sp")
                                .suggest("0")
                                .suggest("1")
                                .suggest("2")
                                .suggest("3")
                                .buildFuture())
                        .executes(NewGameModeCommand::executeMode) // Handles game mode changes for the sender
                        .then(Commands.argument("target", StringArgumentType.word())
                                .suggests(PLAYER_SUGGESTION)
                                .executes(NewGameModeCommand::executeModeForTarget))); // Handles game mode changes for a target player
    }

    private static final SuggestionProvider<CommandSourceStack> PLAYER_SUGGESTION = (context, builder) -> {
        for (ServerPlayer player : context.getSource().getServer().getPlayerList().getPlayers()) {
            if (!VanishCommand.vanishList.contains(player.getGameProfile().getName()))
                builder.suggest(player.getGameProfile().getName()); // Add player names to the suggestions
        }
        return builder.buildFuture();
    };

    private static int executeMode(CommandContext<CommandSourceStack> context) {
        String mode = StringArgumentType.getString(context, "mode");

        ServerPlayer player;
        try {
            player = context.getSource().getPlayerOrException();
        } catch (CommandSyntaxException e) {
            context.getSource().sendFailure(new TextComponent("This command must be executed by a player."));
            return 0;
        }
        if(!player.hasPermissions(3))
            return 0;

        GameType gameType = getGameTypeFromString(mode);
        if (gameType == null) {
            context.getSource().sendFailure(new TextComponent("Invalid game mode: " + mode));
            return 0;
        }

        player.setGameMode(gameType);
        context.getSource().sendSuccess(new TextComponent("Changed your game mode to " + gameType.getName()), true);
        return 1;
    }

    private static int executeModeForTarget(CommandContext<CommandSourceStack> context) {
        String mode = StringArgumentType.getString(context, "mode");
        String targetName = StringArgumentType.getString(context, "target");

        ServerPlayer targetPlayer = context.getSource().getServer().getPlayerList().getPlayerByName(targetName);
        if (targetPlayer == null) {
            context.getSource().sendFailure(new TextComponent("Player not found: " + targetName));
            return 0;
        }

        GameType gameType = getGameTypeFromString(mode);
        if (gameType == null) {
            context.getSource().sendFailure(new TextComponent("Invalid game mode: " + mode));
            return 0;
        }

        targetPlayer.setGameMode(gameType);
        context.getSource().sendSuccess(new TextComponent("Changed " + targetName + "'s game mode to " + gameType.getName()), true);
        targetPlayer.sendMessage(new TextComponent("Your game mode has been changed to " + gameType.getName()), targetPlayer.getUUID());
        return 1;
    }

    private static GameType getGameTypeFromString(String mode) {
        return switch (mode.toLowerCase()) {
            case "s", "0" -> GameType.SURVIVAL;
            case "c", "1" -> GameType.CREATIVE;
            case "a", "2" -> GameType.ADVENTURE;
            case "sp", "3" -> GameType.SPECTATOR;

            default -> null;
        };
    }
}
