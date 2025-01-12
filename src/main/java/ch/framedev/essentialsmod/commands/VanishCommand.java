package ch.framedev.essentialsmod.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashSet;
import java.util.Set;

public class VanishCommand {

    public static final Set<String> vanishList = new HashSet<>(); // Store vanished player names

    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("vanish")
                .requires(source -> source.hasPermission(3)) // Restrict to operators (level 2 or higher)
                .executes(VanishCommand::execute); // Executes when the command is provided
    }

    private static int execute(CommandContext<CommandSourceStack> command) {
        if (command.getSource().getEntity() instanceof ServerPlayer player) {
            String playerName = player.getName().getString();

            if (vanishList.contains(playerName)) {
                // Unvanish the player
                vanishList.remove(playerName);
                player.sendMessage(new TextComponent("You are now visible."), Util.NIL_UUID);

                // Notify other players to re-add the vanished player
                for (ServerPlayer otherPlayer : player.getServer().getPlayerList().getPlayers()) {
                    otherPlayer.connection.send(new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.ADD_PLAYER, player));
                    otherPlayer.connection.send(new ClientboundAddEntityPacket(player));
                }
            } else {
                // Vanish the player
                vanishList.add(playerName);
                player.sendMessage(new TextComponent("You are now vanished."), Util.NIL_UUID);

                // Notify other players to remove the vanished player
                for (ServerPlayer otherPlayer : player.getServer().getPlayerList().getPlayers()) {
                    if (otherPlayer != player && !vanishList.contains(otherPlayer.getName().getString()) && !otherPlayer.hasPermissions(2)) {
                        otherPlayer.connection.send(new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.REMOVE_PLAYER, player));
                        otherPlayer.connection.send(new ClientboundRemoveEntitiesPacket(player.getId()));
                    }
                }
            }
            return 1; // Success
        }
        return 0; // Fail if no player entity is found
    }
}