package ch.framedev.essentialsmod.commands;

import ch.framedev.essentialsmod.Config;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "essentials") // Replace with your mod's ID
public class BackEvent {

    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        // Check if the dying entity is a player
        if (event.getEntity() instanceof ServerPlayer player) {
            Config config = new Config();
            if (config.getConfig().containsKey("back") && config.getConfig().getBoolean("back")) {
                // Send a message to the player (if respawn screen is not disabled)
                Vec3 vec3 = new Vec3(player.getX(), player.getY(), player.getZ());
                BackCommand.backMap.put(player, vec3);
                player.sendMessage(new TextComponent("If you wan't Back to your Death Location use /back!"), player.getUUID());
            } else {
                if (player.hasPermissions(2))
                    player.sendMessage(new TextComponent("Back to Death Location is disabled in your config."), player.getUUID());
            }
        }
    }
}
