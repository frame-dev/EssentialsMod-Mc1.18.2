package ch.framedev.essentialsmod.commands;

import ch.framedev.essentialsmod.utils.ChatUtils;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;

import java.util.ArrayList;
import java.util.List;

public class NewGameModeCommand implements ICommand {

    public LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("gm")
                .requires(source -> source.hasPermission(3))
                .then(Commands.argument("mode", StringArgumentType.word())
                        .suggests(GAMEMODE_SUGGESTIONS)
                        .executes(this::executeMode)
                        .then(Commands.argument("target", StringArgumentType.word())
                                .suggests(PLAYER_SUGGESTION)
                                .executes(this::executeModeForTarget)));
    }

    private final SuggestionProvider<CommandSourceStack> PLAYER_SUGGESTION = (context, builder) -> {
        for (ServerPlayer player : context.getSource().getServer().getPlayerList().getPlayers()) {
            builder.suggest(player.getGameProfile().getName());
        }
        return builder.buildFuture();
    };

    private final SuggestionProvider<CommandSourceStack> GAMEMODE_SUGGESTIONS = (context, builder) -> {
        builder.suggest("survival").suggest("creative").suggest("adventure").suggest("spectator");
        builder.suggest("s").suggest("c").suggest("a").suggest("sp");
        builder.suggest("0").suggest("1").suggest("2").suggest("3");
        return builder.buildFuture();
    };

    private int executeMode(CommandContext<CommandSourceStack> context) {
        String mode = StringArgumentType.getString(context, "mode");

        ServerPlayer player;
        try {
            player = context.getSource().getPlayerOrException();
        } catch (CommandSyntaxException e) {
            context.getSource().sendFailure(new TextComponent("This command must be executed by a player."));
            return 0;
        }

        GameType gameType = getGameTypeFromString(mode);
        if (gameType == null) {
            context.getSource().sendFailure(new TextComponent("Invalid game mode: " + mode).withStyle(ChatFormatting.RED));
            context.getSource().sendFailure(new TextComponent("Available game modes: " + String.join(", ", gameModeList)));
            return 0;
        }

        player.setGameMode(gameType);
        context.getSource().sendSuccess(ChatUtils.getPrefix().append(new TextComponent("Your game mode has been changed to " + gameType.getName()).withStyle(ChatFormatting.GOLD)), true);
        return 1;
    }

    private int executeModeForTarget(CommandContext<CommandSourceStack> context) {
        String mode = StringArgumentType.getString(context, "mode");
        String targetName = StringArgumentType.getString(context, "target");

        ServerPlayer targetPlayer = context.getSource().getServer().getPlayerList().getPlayerByName(targetName);
        if (targetPlayer == null) {
            context.getSource().sendFailure(new TextComponent("Player not found: " + targetName).withStyle(ChatFormatting.RED));
            return 0;
        }

        GameType gameType = getGameTypeFromString(mode);
        if (gameType == null) {
            context.getSource().sendFailure(ChatUtils.getPrefix().append(new TextComponent("Invalid game mode: " + mode).withStyle(ChatFormatting.RED)));
            context.getSource().sendFailure(ChatUtils.getPrefix().append(new TextComponent("Available game modes: " + String.join(", ", gameModeList))));
            return 0;
        }

        targetPlayer.setGameMode(gameType);
        context.getSource().sendSuccess(ChatUtils.getPrefix().append(new TextComponent("Changed " + targetName + "'s game mode to " + gameType.getName()).withStyle(ChatFormatting.GOLD)), true);
        targetPlayer.sendMessage(ChatUtils.getPrefix().append(new TextComponent("Your game mode has been changed to " + gameType.getName()).withStyle(ChatFormatting.GREEN)), targetPlayer.getUUID());
        return 1;
    }

    private GameType getGameTypeFromString(String mode) {
        return switch (mode.toLowerCase()) {
            case "s", "0", "survival" -> GameType.SURVIVAL;
            case "c", "1", "creative" -> GameType.CREATIVE;
            case "a", "2", "adventure" -> GameType.ADVENTURE;
            case "sp", "3", "spectator" -> GameType.SPECTATOR;
            default -> null;
        };
    }

    private final List<String> gameModeList =
            List.of("s", "0", "survival",
                    "c", "1", "creative",
                    "a", "2", "adventure",
                    "sp", "3", "spectator");
}