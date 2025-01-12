package ch.framedev.essentialsmod.events;

import ch.framedev.essentialsmod.commands.MuteCommand;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "essentials") // Replace with your mod's ID
public class ChatEventHandler {

    @SubscribeEvent
    public static void onPlayerChat(ServerChatEvent event) {
        String playerName = event.getPlayer().getName().getString();

        if (MuteCommand.isPlayerMuted(playerName)) {
            // Cancel the chat event and notify the player
            event.setCanceled(true);
            event.getPlayer().sendMessage(new TextComponent("You are muted and cannot send messages."), event.getPlayer().getUUID());
        }
    }
}
