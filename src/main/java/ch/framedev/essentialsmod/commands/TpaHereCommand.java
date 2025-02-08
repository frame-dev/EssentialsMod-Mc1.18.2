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

public class TpaHereCommand implements ICommand {

    private static final Map<String, String> tpHereMap = new HashMap<>();

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("tpahere") // Base command
                .then(Commands.argument("playerName", StringArgumentType.word())
                        .suggests(PLAYER_SUGGESTION)
                        .executes(this::sendTpaHereRequest)) // Handles sending requests
                .then(Commands.literal("accept")
                        .executes(this::acceptTpaHereRequest)) // Handles accepting requests
                .then(Commands.literal("deny")
                        .executes(this::denyTpaHereRequest)); // Handles denying requests
    }

    private int sendTpaHereRequest(CommandContext<CommandSourceStack> context) {
        String targetName = StringArgumentType.getString(context, "playerName");
        CommandSourceStack source = context.getSource();

        if (source.getEntity() instanceof ServerPlayer currentPlayer) {
            MinecraftServer server = source.getServer();
            ServerPlayer targetPlayer = server.getPlayerList().getPlayerByName(targetName);

            if (targetPlayer != null) {
                if (!tpHereMap.containsKey(targetPlayer.getName().getString())) {
                    tpHereMap.put(targetPlayer.getName().getString(), currentPlayer.getName().getString());
                    source.sendSuccess(new TextComponent("Sent TPAHere request to " + targetPlayer.getName().getString()), true);
                    targetPlayer.sendMessage(new TextComponent("[" + currentPlayer.getName().getString() + "] sent a TPAHere request to you. Use /tpahere accept or /tpahere deny."), Util.NIL_UUID);
                } else {
                    source.sendFailure(new TextComponent("Player already has a pending TPAHere request."));
                }
            } else {
                source.sendFailure(new TextComponent("Player not found.").withStyle(ChatFormatting.RED));
            }
        }
        return 1;
    }

    private int acceptTpaHereRequest(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        if (source.getEntity() instanceof ServerPlayer currentPlayer) {
            String currentName = currentPlayer.getName().getString();

            if (tpHereMap.containsKey(currentName)) {
                String requesterName = tpHereMap.get(currentName);
                MinecraftServer server = source.getServer();
                ServerPlayer requesterPlayer = server.getPlayerList().getPlayerByName(requesterName);

                if (requesterPlayer != null) {
                    currentPlayer.teleportTo(requesterPlayer.getLevel(), requesterPlayer.getX(), requesterPlayer.getY(), requesterPlayer.getZ(), requesterPlayer.getYRot(), requesterPlayer.getXRot());
                    source.sendSuccess(new TextComponent("Accepted TPAHere request from " + requesterPlayer.getName().getString()), true);
                    requesterPlayer.sendMessage(new TextComponent("[" + currentName + "] accepted your TPAHere request."), Util.NIL_UUID);
                    tpHereMap.remove(currentName);
                } else {
                    source.sendFailure(new TextComponent("Requester is no longer online."));
                    tpHereMap.remove(currentName);
                }
            } else {
                source.sendFailure(new TextComponent("You don't have any pending TPAHere requests."));
            }
        }
        return 1;
    }

    private int denyTpaHereRequest(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        if (source.getEntity() instanceof ServerPlayer currentPlayer) {
            String currentName = currentPlayer.getName().getString();

            if (tpHereMap.containsKey(currentName)) {
                String requesterName = tpHereMap.get(currentName);
                MinecraftServer server = source.getServer();
                ServerPlayer requesterPlayer = server.getPlayerList().getPlayerByName(requesterName);

                if (requesterPlayer != null) {
                    requesterPlayer.sendMessage(new TextComponent("[" + currentName + "] denied your TPAHere request."), Util.NIL_UUID);
                }
                source.sendSuccess(new TextComponent("Denied TPAHere request from " + requesterName), true);
                tpHereMap.remove(currentName);
            } else {
                source.sendFailure(new TextComponent("You don't have any pending TPAHere requests."));
            }
        }
        return 1;
    }

    private final SuggestionProvider<CommandSourceStack> PLAYER_SUGGESTION = (context, builder) -> {
        for (ServerPlayer player : context.getSource().getServer().getPlayerList().getPlayers()) {
            if (!VanishCommand.vanishList.contains(player.getGameProfile().getName()))
                builder.suggest(player.getGameProfile().getName()); // Add player names to the suggestions
        }
        return builder.buildFuture();
    };
}