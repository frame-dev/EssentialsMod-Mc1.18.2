package ch.framedev.essentialsmod.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;

public class HealCommand {

    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("heal")
                .requires(commandSourceStack -> commandSourceStack.hasPermission(2)) // Restrict command to operators or permission level 2+
                .then(Commands.argument("playerName", StringArgumentType.word())
                        .suggests(PLAYER_SUGGESTION)
                        .executes(HealCommand::executeWithPlayerName)) // Executes for a specific player
                .executes(HealCommand::executeDefault); // Executes for the command executor
    }

    private static int executeDefault(CommandContext<CommandSourceStack> command) {
        if (command.getSource().getEntity() instanceof ServerPlayer player) {
            float maxHealth = player.getMaxHealth(); // Get the player's maximum health
            player.setHealth(maxHealth); // Set the player's health to maximum
            player.getFoodData().setFoodLevel(20); // Fully restore the hunger bar
            player.getFoodData().setSaturation(5.0F); // Set saturation for full hunger
            player.sendMessage(new TextComponent("You have been fully healed!"), Util.NIL_UUID); // Feedback to player
            return 1; // Indicate success
        }
        return 0; // Indicate failure (not executed by a player)
    }

    private static int executeWithPlayerName(CommandContext<CommandSourceStack> command) {
        String playerName = StringArgumentType.getString(command, "playerName");

        // Get the target player
        ServerPlayer targetPlayer = command.getSource().getServer().getPlayerList().getPlayerByName(playerName);

        if (targetPlayer != null) {
            // Heal the target player
            float maxHealth = targetPlayer.getMaxHealth(); // Get the player's maximum health
            targetPlayer.setHealth(maxHealth); // Set the player's health to maximum
            targetPlayer.getFoodData().setFoodLevel(20); // Fully restore the hunger bar
            targetPlayer.getFoodData().setSaturation(5.0F); // Set saturation for full hunger

            // Send feedback to the target player
            targetPlayer.sendMessage(new TextComponent("You have been healed by " + command.getSource().getDisplayName().getString() + "!"), Util.NIL_UUID);

            // Send feedback to the command source
            command.getSource().sendSuccess(new TextComponent("Healed " + targetPlayer.getName().getString()), true);

            return 1; // Indicate success
        } else {
            // Target player not found
            command.getSource().sendFailure(new TextComponent("Player not found: " + playerName));
            return 0; // Indicate failure
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
