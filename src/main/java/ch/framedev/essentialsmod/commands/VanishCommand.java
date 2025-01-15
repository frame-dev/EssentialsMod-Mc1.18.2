package ch.framedev.essentialsmod.commands;

import ch.framedev.essentialsmod.utils.ChatUtils;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashSet;
import java.util.Set;

public class VanishCommand implements ICommand {

    public static final Set<String> vanishList = new HashSet<>(); // Store vanished player names

    public LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("vanish")
                .requires(source -> source.hasPermission(3)) // Restrict to operators (level 2 or higher)
                .executes(this::execute) // Executes when the command is provided
                .then(Commands.literal("v") // Alias for vanish
                        .executes(this::execute));
    }

    private int execute(CommandContext<CommandSourceStack> command) {
        if (command.getSource().getEntity() instanceof ServerPlayer player) {
            String playerName = player.getName().getString();

            if (vanishList.contains(playerName)) {
                // Unvanish the player
                vanishList.remove(playerName);
                player.sendMessage(new TextComponent("You are now visible."), Util.NIL_UUID);
                player.sendMessage(new TextComponent("Please Rejoin to be fully visible."), Util.NIL_UUID);

                MinecraftServer server = player.getServer();
                if (server == null) {
                    player.sendMessage(new TextComponent("Server not found."), Util.NIL_UUID);
                    return 0; // Fail if the server is null
                }

                // Notify other players to re-add the vanished player
                for (ServerPlayer otherPlayer : server.getPlayerList().getPlayers()) {
                    otherPlayer.connection.send(new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.ADD_PLAYER, player));
                    otherPlayer.connection.send(new ClientboundAddEntityPacket(player));
                }
                // Reset visibility for the player
                player.setInvisible(false);
                player.connection.teleport(player.position().x, player.position().y, player.position().z, 0f, 0f);
            } else {
                // Vanish the player
                vanishList.add(playerName);
                player.sendMessage(new TextComponent("You are now vanished."), Util.NIL_UUID);

                MinecraftServer server = player.getServer();
                if (server == null) {
                    player.sendMessage(new TextComponent("Server not found."), Util.NIL_UUID);
                    return 0; // Fail if the server is null
                }

                TextComponent otherComponent = ChatUtils.getTextComponent(
                        new String[]{"§a", player.getGameProfile().getName(), "is now vanished."},
                        new String[]{"§a","§b","§a"}
                );

                // TODO:
                //  - Require Debugging
                // Notify other players to remove the vanished player
                for (ServerPlayer otherPlayer : server.getPlayerList().getPlayers()) {
                    if(otherPlayer != player) {
                        if (!vanishList.contains(otherPlayer.getName().getString()) && !otherPlayer.hasPermissions(2)) {
                            otherPlayer.connection.send(new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.REMOVE_PLAYER, player));
                            otherPlayer.connection.send(new ClientboundRemoveEntitiesPacket(player.getId()));
                        } else if (otherPlayer.hasPermissions(2))
                            otherPlayer.sendMessage(ChatUtils.getPrefix().append(otherComponent), otherPlayer.getUUID());
                    }
                }
                player.setInvisible(true);
            }
            return 1; // Success
        }
        return 0; // Fail if no player entity is found
    }
}