package ch.framedev.essentialsmod.commands;

/*
 * ch.framedev.essentialsmod.commands
 * =============================================
 * This File was Created by FrameDev.
 * Please do not change anything without my consent!
 * =============================================
 * This Class was created at 15.01.2025 17:58
 */

import ch.framedev.essentialsmod.EssentialsMod;
import ch.framedev.essentialsmod.utils.ChatUtils;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * TODO:
 *  - Require Debugging
 */
public class MuteOtherPlayerCommand implements ICommand {

    // Map to store which players have muted others
    public static final Map<String, Set<String>> playerMuteMap = new HashMap<>();

    public LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("muteother")
                .then(Commands.argument("playerName", StringArgumentType.word())
                        .executes(this::execute));
    }

    private int execute(CommandContext<CommandSourceStack> command) {
        if (!(command.getSource().getEntity() instanceof ServerPlayer serverPlayer)) {
            command.getSource().sendFailure(new TextComponent("Only Players can use this command!").withStyle(ChatFormatting.RED));
            return 0;
        }

        String playerName = StringArgumentType.getString(command, "playerName");
        ServerPlayer targetPlayer = command.getSource().getServer().getPlayerList().getPlayerByName(playerName);

        if (targetPlayer == null) {
            serverPlayer.sendMessage(ChatUtils.getPrefix()
                    .append(new TextComponent("Player not found!").withStyle(ChatFormatting.RED)), Util.NIL_UUID);
            return 0;
        }

        String muterName = serverPlayer.getName().getString();
        playerMuteMap.putIfAbsent(muterName, new HashSet<>());

        Set<String> mutedPlayers = playerMuteMap.get(muterName);

        if (mutedPlayers.contains(playerName)) {
            // Unmute the player
            mutedPlayers.remove(playerName);
            serverPlayer.sendMessage(ChatUtils.getPrefix()
                    .append(new TextComponent(playerName + " has been unmuted for you.").withStyle(ChatFormatting.GREEN)), Util.NIL_UUID);
        } else {
            // Mute the player
            mutedPlayers.add(playerName);
            serverPlayer.sendMessage(ChatUtils.getPrefix()
                    .append(new TextComponent(playerName + " has been muted for you.").withStyle(ChatFormatting.RED)), Util.NIL_UUID);
        }

        return 1; // Command executed successfully
    }

    @Mod.EventBusSubscriber(modid = "essentials")
    public static class ChatEventHandler {

        @SubscribeEvent
        public static void onPlayerChat(ServerChatEvent event) {
            ServerPlayer sender = event.getPlayer();
            String senderName = sender.getName().getString();
            MinecraftServer server = sender.getServer();

            if (server == null) {
                return;
            }

            // Iterate through all players on the server
            for (ServerPlayer targetPlayer : server.getPlayerList().getPlayers()) {
                String targetName = targetPlayer.getName().getString();

                // Check if the target player has muted the sender
                if (playerMuteMap.containsKey(targetName) && playerMuteMap.get(targetName).contains(senderName)) {
                    // If muted, skip sending the message to this player
                    continue;
                }

                // Send the message to all other players except the muted ones
                targetPlayer.sendMessage(event.getComponent(), Util.NIL_UUID);
            }

            // Cancel the default chat event to prevent duplicate messages
            EssentialsMod.getLOGGER().info("{}: {}", senderName, event.getMessage());
            event.setCanceled(true);
        }
    }
}
