package ch.framedev.essentialsmod.events;

import ch.framedev.essentialsmod.utils.ChatUtils;
import ch.framedev.essentialsmod.commands.BackCommand;
import ch.framedev.essentialsmod.utils.EssentialsConfig;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "essentials") // Replace with your mod's ID
public class BackEventHandler {

    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        // Check if the dying entity is a player
        if (event.getEntity() instanceof ServerPlayer player) {
            if (!EssentialsConfig.useBack.get())
                return; // Back is Disabled no need to check

            Vec3 vec3 = new Vec3(player.getX(), player.getY(), player.getZ());
            BackCommand.backMap.put(player, vec3);
            TextComponent textComponent = ChatUtils.getTextComponent(
                    new String[]{
                            "If you won't Back to your Death Location use",
                            "/back!"
                    },
                    new String[]{"§b", "§a"}
            );
            player.sendMessage(ChatUtils.getPrefix().append(textComponent), player.getUUID());
        }
    }
}
