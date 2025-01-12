package ch.framedev.essentialsmod.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;

public class FlyCommand {

    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("fly")
                .requires(commandSourceStack -> commandSourceStack.hasPermission(2)) // Restrict command to operators or permission level 2+
                .then(Commands.argument("playerName", StringArgumentType.word())
                        .suggests(PLAYER_SUGGESTION)
                        .executes(FlyCommand::executeWithPlayerName)) // Executes for a specific player
                .executes(FlyCommand::executeDefault); // Executes for the command executor
    }

    private static int executeWithPlayerName(CommandContext<CommandSourceStack> command) {
        String playerName = StringArgumentType.getString(command, "playerName");
        ServerPlayer player = command.getSource().getServer().getPlayerList().getPlayerByName(playerName);
        if(player == null) {
            command.getSource().sendFailure(new TextComponent("Player not found!"));
            return 0;
        }
        if (player.getAbilities().mayfly) {
            // Disable flying
            player.getAbilities().mayfly = false;
            player.getAbilities().flying = false;
            player.onUpdateAbilities();
            player.sendMessage(new TextComponent("Flying disabled."), player.getUUID());
            command.getSource().sendSuccess(new TextComponent("Flying for " + player.getGameProfile().getName() + " is disabled"), true);
        } else {
            // Enable flying
            player.getAbilities().mayfly = true;
            player.getAbilities().flying = true;
            player.onUpdateAbilities();
            player.sendMessage(new TextComponent("Flying enabled!"), player.getUUID());
            command.getSource().sendSuccess(new TextComponent("Flying for " + player.getGameProfile().getName() + " is enabled"), true);
        }
        return 1;
    }

    private static int executeDefault(CommandContext<CommandSourceStack> command) {
        if(!(command.getSource().getEntity() instanceof ServerPlayer player)) {
            command.getSource().sendFailure(new TextComponent("Only Player can execute this command!"));
            return 0;
        }
        if (player.getAbilities().mayfly) {
            // Disable flying
            player.getAbilities().mayfly = false;
            player.getAbilities().flying = false;
            player.onUpdateAbilities();
            player.sendMessage(new TextComponent("Flying disabled."), player.getUUID());
        } else {
            // Enable flying
            player.getAbilities().mayfly = true;
            player.getAbilities().flying = true;
            player.onUpdateAbilities();
            player.sendMessage(new TextComponent("Flying enabled!"), player.getUUID());
        }
        return 1; // Indicate success
    }

    private static final SuggestionProvider<CommandSourceStack> PLAYER_SUGGESTION = (context, builder) -> {
        for (ServerPlayer player : context.getSource().getServer().getPlayerList().getPlayers()) {
            if (!VanishCommand.vanishList.contains(player.getGameProfile().getName()))
                builder.suggest(player.getGameProfile().getName()); // Add player names to the suggestions
        }
        return builder.buildFuture();
    };

}
