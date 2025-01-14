package ch.framedev.essentialsmod.events;

import ch.framedev.essentialsmod.commands.VanishCommand;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "essentials") // Replace with your mod's ID
public class PlayerJoinEvent {

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player) {
            if (player.getServer() == null) return;
            if (VanishCommand.vanishList.contains(player.getName().getString()))
                for (ServerPlayer otherPlayer : player.getServer().getPlayerList().getPlayers()) {
                    if (otherPlayer != player && !VanishCommand.vanishList.contains(otherPlayer.getName().getString()) && !otherPlayer.hasPermissions(2)) {
                        otherPlayer.connection.send(new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.REMOVE_PLAYER, player));
                        otherPlayer.connection.send(new ClientboundRemoveEntitiesPacket(player.getId()));
                    }
                }
            else {
                for (ServerPlayer otherPlayer : player.getServer().getPlayerList().getPlayers()) {
                    if (otherPlayer != player && VanishCommand.vanishList.contains(otherPlayer.getName().getString()) && !player.hasPermissions(2)) {
                        player.connection.send(new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.REMOVE_PLAYER, otherPlayer));
                        player.connection.send(new ClientboundRemoveEntitiesPacket(otherPlayer.getId()));
                    }
                }
            }
        }
    }
}
