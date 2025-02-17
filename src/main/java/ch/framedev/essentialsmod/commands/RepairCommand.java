package ch.framedev.essentialsmod.commands;

import ch.framedev.essentialsmod.utils.ChatUtils;
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

public class RepairCommand implements ICommand {

    public LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("repair")
                .requires(commandSourceStack -> commandSourceStack.hasPermission(2)) // Restrict command to operators or permission level 2+
                .then(Commands.argument("playerName", StringArgumentType.word())
                        .suggests(PLAYER_SUGGESTION)
                        .executes(this::executeWithPlayerName)) // Executes for a specific player
                .executes(this::executeDefault); // Executes for the command executor
    }


    private int executeDefault(CommandContext<CommandSourceStack> command) {
        if (command.getSource().getEntity() instanceof ServerPlayer player) {
            ItemStack stack = player.getMainHandItem();

            if (stack.isEmpty()) {
                player.sendMessage(ChatUtils.getPrefix().append(new TextComponent("You are not holding any item!").withStyle(ChatFormatting.RED)), Util.NIL_UUID);
                return 0; // Fail if no item is held
            }

            if (!stack.isDamageableItem()) {
                player.sendMessage(ChatUtils.getPrefix().append(new TextComponent("The item in your hand cannot be repaired!").withStyle(ChatFormatting.RED)), Util.NIL_UUID);
                return 0; // Fail if the item is not damageable
            }

            // Repair the item by resetting its damage value
            stack.setDamageValue(0);
            player.sendMessage(ChatUtils.getPrefix().append(new TextComponent("Your item has been repaired!")
                    .withStyle(ChatFormatting.GREEN)), Util.NIL_UUID);

            return Command.SINGLE_SUCCESS; // Indicate success
        }

        return 0; // Fail if the command is not executed by a player
    }


    private int executeWithPlayerName(CommandContext<CommandSourceStack> command) {
        String playerName = command.getArgument("playerName", String.class);
        ServerPlayer targetPlayer = command.getSource().getServer().getPlayerList().getPlayerByName(playerName);

        if (targetPlayer != null) {
            ItemStack stack = targetPlayer.getMainHandItem();

            if (stack.isEmpty()) {
                command.getSource().sendSuccess(ChatUtils.getPrefix().append(new TextComponent(playerName + " is not holding any item!")
                        .withStyle(ChatFormatting.RED)), true);
                return 0; // Fail if no item is held
            }

            if (!stack.isDamageableItem()) {
                command.getSource().sendSuccess(ChatUtils.getPrefix().append(new TextComponent("The item in " + playerName + "'s hand cannot be repaired!")
                        .withStyle(ChatFormatting.RED)), true);
                return 0; // Fail if the item is not damageable
            }

            // Repair the item by resetting its damage value
            stack.setDamageValue(0);

            // Inform both the target player and the command source
            targetPlayer.sendMessage(ChatUtils.getPrefix().append(new TextComponent("Your item has been repaired by " + command.getSource().getDisplayName().getString() + "!")
                    .withStyle(ChatFormatting.GREEN)), Util.NIL_UUID);
            command.getSource().sendSuccess(ChatUtils.getPrefix().append(new TextComponent("Repaired the item for " + playerName + "!")
                    .withStyle(ChatFormatting.GREEN)), true);

            return Command.SINGLE_SUCCESS; // Indicate success
        } else {
            // Player not found
            command.getSource().sendFailure(new TextComponent("Player not found: " + playerName).withStyle(ChatFormatting.RED));
            return 0;
        }
    }

    private final SuggestionProvider<CommandSourceStack> PLAYER_SUGGESTION = (context, builder) -> {
        for (ServerPlayer player : context.getSource().getServer().getPlayerList().getPlayers()) {
            if (!VanishCommand.vanishList.contains(player.getGameProfile().getName()))
                builder.suggest(player.getGameProfile().getName()); // Add player names to the suggestions
        }
        return builder.buildFuture();
    };
}
