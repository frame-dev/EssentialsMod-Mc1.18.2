package ch.framedev.essentialsmod.commands;

import ch.framedev.essentialsmod.utils.ChatUtils;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;

public class FlyCommand {

    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("fly")
                .requires(commandSourceStack -> commandSourceStack.hasPermission(2)) // Restrict command to operators or permission level 2+
                .then(Commands.argument("playerName", StringArgumentType.word())
                        .suggests(PLAYER_SUGGESTION)
                        .executes(FlyCommand::executeWithPlayerName) // Executes for a specific player
                        .then(Commands.argument("speed", FloatArgumentType.floatArg(0.1f, 10f))
                                .executes(FlyCommand::executeWithPlayerNameSpeed)))
                .executes(FlyCommand::executeDefault); // Executes for the command executor
    }

    private static int executeWithPlayerNameSpeed(CommandContext<CommandSourceStack> command) {
        String playerName = StringArgumentType.getString(command, "playerName");
        float speed = FloatArgumentType.getFloat(command, "speed");
        ServerPlayer player = command.getSource().getServer().getPlayerList().getPlayerByName(playerName);

        if (player == null) {
            command.getSource().sendFailure(ChatUtils.getPrefix().append(Component.nullToEmpty("Player not found!")));
            return 0;
        }

        setFlySpeed(player, speed);
        command.getSource().sendSuccess(ChatUtils.getPrefix().append(Component.nullToEmpty(
                "Set flight speed for " + player.getGameProfile().getName() + " to " + speed)), true);
        return 1;
    }


    private static int executeWithPlayerName(CommandContext<CommandSourceStack> command) {
        String playerName = StringArgumentType.getString(command, "playerName");
        ServerPlayer player = command.getSource().getServer().getPlayerList().getPlayerByName(playerName);
        if (player == null) {
            command.getSource().sendFailure(ChatUtils.getPrefix().append(new TextComponent("Player not found!")));
            return 0;
        }
        if (player.getAbilities().mayfly) {
            // Disable flying
            player.getAbilities().mayfly = false;
            player.getAbilities().flying = false;
            player.onUpdateAbilities();
            player.sendMessage(getTextByStatus(false), player.getUUID());
            command.getSource().sendSuccess(ChatUtils.getPrefix().append(getTextByStatusOther(false, player.getGameProfile().getName())), true);
        } else {
            // Enable flying
            player.getAbilities().mayfly = true;
            player.getAbilities().flying = true;
            player.onUpdateAbilities();
            player.sendMessage(getTextByStatus(true), player.getUUID());
            command.getSource().sendSuccess(ChatUtils.getPrefix().append(getTextByStatusOther(true, player.getGameProfile().getName())), true);
        }
        return 1;
    }

    private static int executeDefault(CommandContext<CommandSourceStack> command) {
        if (!(command.getSource().getEntity() instanceof ServerPlayer player)) {
            command.getSource().sendFailure(ChatUtils.getPrefix().append(new TextComponent("Only Player can execute this command!").withStyle(ChatFormatting.RED)));
            return 0;
        }
        if (player.getAbilities().mayfly) {
            // Disable flying
            player.getAbilities().mayfly = false;
            player.getAbilities().flying = false;
            player.onUpdateAbilities();
            player.sendMessage(ChatUtils.getPrefix().append(getTextByStatus(false)), player.getUUID());
        } else {
            // Enable flying
            player.getAbilities().mayfly = true;
            player.getAbilities().flying = true;
            player.onUpdateAbilities();
            player.sendMessage(ChatUtils.getPrefix().append(getTextByStatus(true)), player.getUUID());
        }
        return 1; // Indicate success
    }

    private static void setFlySpeed(ServerPlayer player, float speed) {
        player.getAbilities().setFlyingSpeed(speed / 10.0f); // Divide by 10 to match Minecraft's flight speed scale
        player.onUpdateAbilities();
        player.sendMessage(new TextComponent("Fly Speed set to " + player.getAbilities().getFlyingSpeed()), player.getUUID());
    }

    private static TextComponent getTextByStatus(boolean active) {
        if (active) {
            return ChatUtils.getTextComponent(new String[]{"Flying", "enabled!"}, new String[]{"§a", "§6"});
        } else {
            return ChatUtils.getTextComponent(new String[]{"Flying", "disabled!"}, new String[]{"§a", "§6"});
        }
    }

    private static TextComponent getTextByStatusOther(boolean active, String playerName) {
        if (active) {
            return ChatUtils.getTextComponent(new String[]{"Flying for", playerName, "is", "enabled!"}, new String[]{"§a", "§6", "§a", "§6"});
        } else {
            return ChatUtils.getTextComponent(new String[]{"Flying for", playerName, "is", "disabled!"}, new String[]{"§a", "§6", "§a", "§6"});
        }
    }

    private static final SuggestionProvider<CommandSourceStack> PLAYER_SUGGESTION = (context, builder) -> {
        for (ServerPlayer player : context.getSource().getServer().getPlayerList().getPlayers()) {
            builder.suggest(player.getGameProfile().getName()); // Add player names to the suggestions
        }
        return builder.buildFuture();
    };

}
