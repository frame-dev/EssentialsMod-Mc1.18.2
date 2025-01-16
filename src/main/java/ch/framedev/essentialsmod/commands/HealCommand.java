package ch.framedev.essentialsmod.commands;

import ch.framedev.essentialsmod.utils.ChatUtils;
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
            player.sendMessage(ChatUtils.getPrefix().append(new TextComponent("You have been fully healed!").withStyle(ChatFormatting.GREEN)), Util.NIL_UUID); // Feedback to player
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
            TextComponent otherTextComponent = ChatUtils.getTextComponent(
                    new String[]{
                            "You have been healed by",
                            command.getSource().getDisplayName().getString(),
                            "!"
                    }, new String[]{"§a", "§b", "§a"});
            targetPlayer.sendMessage(ChatUtils.getPrefix().append(otherTextComponent), Util.NIL_UUID);

            // Send feedback to the command source
            TextComponent textComponent = ChatUtils.getTextComponent(new String[]{
                    "Healed",
                    targetPlayer.getGameProfile().getName()
            }, new String[]{"§a", "§b"});
            command.getSource().sendSuccess(ChatUtils.getPrefix().append(textComponent), true);

            return 1; // Indicate success
        } else {
            // Target player not found
            command.getSource().sendFailure(ChatUtils.getPrefix().append(new TextComponent("Player not found: " + playerName).withStyle(ChatFormatting.RED)));
            return 0; // Indicate failure
        }
    }

    private static final SuggestionProvider<CommandSourceStack> PLAYER_SUGGESTION = (context, builder) -> {
        for (ServerPlayer player : context.getSource().getServer().getPlayerList().getPlayers()) {
            builder.suggest(player.getGameProfile().getName()); // Add player names to the suggestions
        }
        return builder.buildFuture();
    };
}
