package ch.framedev.essentialsmod.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;

public class TpaCommand {

    private static final Map<String, String> tpaMap = new HashMap<>();

    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("tpa") // Base command
                .then(Commands.argument("playerName", StringArgumentType.word())
                        .suggests(PLAYER_SUGGESTION)
                        .executes(TpaCommand::sendTpaRequest)) // Handles sending requests
                .then(Commands.literal("accept")
                        .executes(TpaCommand::acceptTpaRequest)) // Handles accepting requests
                .then(Commands.literal("deny")
                        .executes(TpaCommand::denyTpaRequest)); // Handles denying requests
    }

    private static int sendTpaRequest(CommandContext<CommandSourceStack> context) {
        String targetName = StringArgumentType.getString(context, "playerName");
        CommandSourceStack source = context.getSource();

        if (source.getEntity() instanceof ServerPlayer currentPlayer) {
            MinecraftServer server = source.getServer();
            ServerPlayer targetPlayer = server.getPlayerList().getPlayerByName(targetName);

            if (targetPlayer != null) {
                if (!tpaMap.containsKey(targetPlayer.getName().getString())) {
                    tpaMap.put(targetPlayer.getName().getString(), currentPlayer.getName().getString());
                    source.sendSuccess(new TextComponent("Sent TPA request to " + targetPlayer.getName().getString()), true);
                    targetPlayer.sendMessage(new TextComponent("[" + currentPlayer.getName().getString() + "] sent a TPA request to you. Use /tpa accept or /tpa deny."), Util.NIL_UUID);
                } else {
                    source.sendFailure(new TextComponent("Player already has a pending TPA request."));
                }
            } else {
                source.sendFailure(new TextComponent("Player not found.").withStyle(ChatFormatting.RED));
            }
        }
        return 1;
    }

    private static int acceptTpaRequest(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        if (source.getEntity() instanceof ServerPlayer currentPlayer) {
            String currentName = currentPlayer.getName().getString();

            if (tpaMap.containsKey(currentName)) {
                String requesterName = tpaMap.get(currentName);
                MinecraftServer server = source.getServer();
                ServerPlayer requesterPlayer = server.getPlayerList().getPlayerByName(requesterName);

                if (requesterPlayer != null) {
                    requesterPlayer.teleportTo(currentPlayer.getLevel(), currentPlayer.getX(), currentPlayer.getY(), currentPlayer.getZ(), currentPlayer.getYRot(), currentPlayer.getXRot());
                    source.sendSuccess(new TextComponent("Accepted TPA request from " + requesterPlayer.getName().getString()), true);
                    requesterPlayer.sendMessage(new TextComponent("[" + currentName + "] accepted your TPA request."), Util.NIL_UUID);
                    tpaMap.remove(currentName);
                } else {
                    source.sendFailure(new TextComponent("Requester is no longer online."));
                    tpaMap.remove(currentName);
                }
            } else {
                source.sendFailure(new TextComponent("You don't have any pending TPA requests."));
            }
        }
        return 1;
    }

    private static int denyTpaRequest(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        if (source.getEntity() instanceof ServerPlayer currentPlayer) {
            String currentName = currentPlayer.getName().getString();

            if (tpaMap.containsKey(currentName)) {
                String requesterName = tpaMap.get(currentName);
                MinecraftServer server = source.getServer();
                ServerPlayer requesterPlayer = server.getPlayerList().getPlayerByName(requesterName);

                if (requesterPlayer != null) {
                    requesterPlayer.sendMessage(new TextComponent("[" + currentName + "] denied your TPA request."), Util.NIL_UUID);
                }
                source.sendSuccess(new TextComponent("Denied TPA request from " + requesterName), true);
                tpaMap.remove(currentName);
            } else {
                source.sendFailure(new TextComponent("You don't have any pending TPA requests."));
            }
        }
        return 1;
    }

    private static final SuggestionProvider<CommandSourceStack> PLAYER_SUGGESTION = (context, builder) -> {
        for (ServerPlayer player : context.getSource().getServer().getPlayerList().getPlayers()) {
            if (!VanishCommand.vanishList.contains(player.getGameProfile().getName()))
                builder.suggest(player.getGameProfile().getName()); // Add player names to the suggestions
        }
        return builder.buildFuture();
    };
}
