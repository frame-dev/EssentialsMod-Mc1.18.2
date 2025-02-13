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

public class FeedCommand implements ICommand {

    // Feed Command
    @Override
    public LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("feed")
                .requires(commandSourceStack -> commandSourceStack.hasPermission(2)) // Restrict command to operators or permission level 2+
                .then(Commands.argument("playerName", StringArgumentType.word())
                        .suggests(PLAYER_SUGGESTION)
                        .executes(this::executeWithPlayerName)) // Executes for a specific player
                .executes(this::executeDefault); // Executes for the command executor
    }

    private int executeDefault(CommandContext<CommandSourceStack> command) {
        if (!(command.getSource().getEntity() instanceof ServerPlayer player)) {
            command.getSource().sendFailure(ChatUtils.getPrefix().append(new TextComponent("Only players can use this command!")));
            return 0;
        }
        // Replenish the player's hunger and saturation
        player.getFoodData().setFoodLevel(20);
        player.getFoodData().setSaturation(5.0F);

        // Send feedback to the player
        TextComponent textComponent = (TextComponent) ChatUtils.getPrefix().append(
                ChatUtils.getColoredTextComponent("Your food bar has been fully replenished!",
                        ChatFormatting.GREEN
                ));
        player.sendMessage(textComponent, player.getUUID());

        return 1; // Indicate success
    }

    private int executeWithPlayerName(CommandContext<CommandSourceStack> command) {
        String playerName = StringArgumentType.getString(command, "playerName");

        // Get the target player
        ServerPlayer targetPlayer = command.getSource().getServer().getPlayerList().getPlayerByName(playerName);

        if (targetPlayer != null) {
            // Replenish the target player's hunger and saturation
            targetPlayer.getFoodData().setFoodLevel(20);
            targetPlayer.getFoodData().setSaturation(5.0F);

            // Send feedback to the target player
            TextComponent textComponent = (TextComponent) ChatUtils.getPrefix().append(
                    ChatUtils.getTextComponent(new String[]{
                                    "Your food bar has been fully replenished by",
                                    command.getSource().getDisplayName().getString(),
                                    "!"
                            },
                            new String[]{"§a", "§b", "§a"}
                    )
            );
            targetPlayer.sendMessage(
                    textComponent, targetPlayer.getUUID());

            // Send feedback to the command source
            TextComponent textSource = (TextComponent) ChatUtils.getPrefix().append(
                    ChatUtils.getTextComponent(new String[]{
                                    "You replenished food for",
                                    targetPlayer.getName().getString(),
                                    "."
                            },
                            new String[]{"§a", "§b", "§a"}
                    )
            );
            command.getSource().sendSuccess(textSource, true);

            return 1; // Indicate success
        } else {
            // Target player not found
            command.getSource().sendFailure(new TextComponent("Player not found: " + playerName).withStyle(ChatFormatting.RED));
            return 0; // Indicate failure
        }
    }

    private final SuggestionProvider<CommandSourceStack> PLAYER_SUGGESTION = (context, builder) -> {
        for (ServerPlayer player : context.getSource().getServer().getPlayerList().getPlayers()) {
            builder.suggest(player.getGameProfile().getName()); // Add player names to the suggestions
        }
        return builder.buildFuture();
    };
}