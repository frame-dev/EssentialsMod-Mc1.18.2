package ch.framedev.essentialsmod.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class RepairCommand {

    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("repair")
                .requires(commandSourceStack -> commandSourceStack.hasPermission(2)) // Restrict command to operators or permission level 2+
                .then(Commands.argument("playerName", StringArgumentType.word())
                        .suggests(PLAYER_SUGGESTION)
                        .executes(RepairCommand::executeWithPlayerName)) // Executes for a specific player
                .executes(RepairCommand::executeDefault); // Executes for the command executor
    }


    private static int executeDefault(CommandContext<CommandSourceStack> command) {
        if (command.getSource().getEntity() instanceof ServerPlayer player) {
            ItemStack stack = player.getMainHandItem();

            if (stack.isEmpty()) {
                player.sendMessage(new TextComponent("You are not holding any item!").withStyle(ChatFormatting.RED), Util.NIL_UUID);
                return 0; // Fail if no item is held
            }

            if (!stack.isDamageableItem()) {
                player.sendMessage(new TextComponent("The item in your hand cannot be repaired!").withStyle(ChatFormatting.RED), Util.NIL_UUID);
                return 0; // Fail if the item is not damageable
            }

            // Repair the item by resetting its damage value
            stack.setDamageValue(0);
            player.sendMessage(new TextComponent("Your item has been repaired!"), Util.NIL_UUID);

            return Command.SINGLE_SUCCESS; // Indicate success
        }

        return 0; // Fail if the command is not executed by a player
    }


    private static int executeWithPlayerName(CommandContext<CommandSourceStack> command) {
        String playerName = command.getArgument("playerName", String.class);
        ServerPlayer targetPlayer = command.getSource().getServer().getPlayerList().getPlayerByName(playerName);

        if (targetPlayer != null) {
            ItemStack stack = targetPlayer.getMainHandItem();

            if (stack.isEmpty()) {
                command.getSource().sendSuccess(new TextComponent(playerName + " is not holding any item!").withStyle(ChatFormatting.RED), true);
                return 0; // Fail if no item is held
            }

            if (!stack.isDamageableItem()) {
                command.getSource().sendSuccess(new TextComponent("The item in " + playerName + "'s hand cannot be repaired!").withStyle(ChatFormatting.RED), true);
                return 0; // Fail if the item is not damageable
            }

            // Repair the item by resetting its damage value
            stack.setDamageValue(0);

            // Inform both the target player and the command source
            targetPlayer.sendMessage(new TextComponent("Your item has been repaired by " + command.getSource().getDisplayName().getString() + "!"), Util.NIL_UUID);
            command.getSource().sendSuccess(new TextComponent("Repaired the item for " + playerName + "!"), true);

            return Command.SINGLE_SUCCESS; // Indicate success
        } else {
            // Player not found
            command.getSource().sendFailure(new TextComponent("Player not found: " + playerName).withStyle(ChatFormatting.RED));
            return 0;
        }
    }

    private static final SuggestionProvider<CommandSourceStack> PLAYER_SUGGESTION = (context, builder) -> {
        for (ServerPlayer player : context.getSource().getServer().getPlayerList().getPlayers()) {
            if (!VanishCommand.vanishList.contains(player.getGameProfile().getName()))
                builder.suggest(player.getGameProfile().getName()); // Add player names to the suggestions
        }
        return builder.buildFuture();
    };
}
