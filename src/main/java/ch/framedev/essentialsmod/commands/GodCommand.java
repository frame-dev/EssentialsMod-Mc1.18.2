package ch.framedev.essentialsmod.commands;

import ch.framedev.essentialsmod.utils.ChatUtils;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;

public class GodCommand implements ICommand {
    @Override
    public LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("god") // Command base
                .requires(source -> source.hasPermission(3))
                .then(Commands.argument("playerName", StringArgumentType.word()) // String argument
                        .suggests(PLAYER_SUGGESTION)
                        .executes(this::executeWithContext)) // Executes when only message is provided
                .executes(this::execute); // Executes with no arg
    }

    private int executeWithContext(CommandContext<CommandSourceStack> command) {
        String playerName = StringArgumentType.getString(command, "playerName");
        ServerPlayer target = command.getSource().getServer().getPlayerList().getPlayerByName(playerName);
        if (target != null) {
            if(!target.isInvulnerable()) {
                target.setInvulnerable(true);
                command.getSource().sendSuccess(ChatUtils.getPrefix().append(getTextComponentOther(playerName, true)), true);
                target.sendMessage(ChatUtils.getPrefix().append(getTextComponentSelf(true)), Util.NIL_UUID);
            } else {
                target.setInvulnerable(false);
                command.getSource().sendSuccess(ChatUtils.getPrefix().append(getTextComponentOther(playerName, false)), true);
                target.sendMessage(ChatUtils.getPrefix().append(getTextComponentSelf(false)), Util.NIL_UUID);
            }
            return 1;
        } else {
            command.getSource().sendFailure(new TextComponent("Could not find player " + playerName));
            return 0;
        }
    }

    public int execute(CommandContext<CommandSourceStack> command) {
        if(command.getSource().getEntity() instanceof ServerPlayer serverPlayer) {
            if(!serverPlayer.isInvulnerable()) {
                serverPlayer.setInvulnerable(true);
                command.getSource().sendSuccess(ChatUtils.getPrefix().append(getTextComponentSelf(true)), true);
            } else {
                serverPlayer.setInvulnerable(false);
                command.getSource().sendSuccess(ChatUtils.getPrefix().append(getTextComponentSelf(false)), true);
            }
            return 1;
        } else {
            command.getSource().sendFailure(new TextComponent("Only a player can execute this command!"));
            return 0;
        }
    }

    private TextComponent getTextComponentSelf(boolean enabled) {
        String[] messages = new String[]{
                "God mode",
                enabled? "enabled" : "disabled"
        };
        String[] colorPattern = new String[]{
                "§a",
                "§b"
        };
        return ChatUtils.getTextComponent(messages, colorPattern);
    }

    private TextComponent getTextComponentOther(String playerName,boolean enabled) {
        String[] messages = new String[]{
                "God mode",
                enabled ? "enabled" : "disabled"
        };
        String[] colorPattern = new String[]{
                "§a",
                "§b"
        };
        return ChatUtils.getTextComponent(messages, colorPattern);
    }

    private final SuggestionProvider<CommandSourceStack> PLAYER_SUGGESTION = (context, builder) -> {
        for (ServerPlayer player : context.getSource().getServer().getPlayerList().getPlayers()) {
            builder.suggest(player.getGameProfile().getName());
        }
        return builder.buildFuture();
    };
}
