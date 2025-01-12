package ch.framedev.essentialsmod.events;

import ch.framedev.essentialsmod.commands.InvseeCommand;
import net.minecraft.Util;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "essentials") // Replace with your mod's ID
public class InventorySyncHandler {

    @SubscribeEvent
    public static void onContainerClose(PlayerContainerEvent.Close event) {
        if (event.getContainer() instanceof InvseeCommand.VirtualInventoryMenu menu) {
            ServerPlayer currentPlayer = (ServerPlayer) event.getPlayer(); // The player who opened the menu
            ServerPlayer targetPlayer = menu.getTargetPlayer(); // Get the target player (passed when creating the menu)

            if (targetPlayer != null) {
                SimpleContainer virtualInventory = menu.getVirtualInventory();

                // Backup armor slots
                ItemStack[] armorBackup = new ItemStack[targetPlayer.getInventory().armor.size()];
                for (int i = 0; i < armorBackup.length; i++) {
                    armorBackup[i] = targetPlayer.getInventory().armor.get(i).copy();
                }

                // Backup offhand slot
                ItemStack offhandBackup = targetPlayer.getInventory().offhand.get(0).copy();

                // Clear the target player's inventory
                targetPlayer.getInventory().clearContent();

                // Synchronize the virtual inventory back to the target player's inventory
                for (int slot = 0; slot < virtualInventory.getContainerSize(); slot++) {
                    targetPlayer.getInventory().setItem(slot, virtualInventory.getItem(slot));
                }

                // Restore armor slots
                for (int i = 0; i < armorBackup.length; i++) {
                    targetPlayer.getInventory().armor.set(i, armorBackup[i]);
                }

                // Restore offhand slot
                targetPlayer.getInventory().offhand.set(0, offhandBackup);

                currentPlayer.sendMessage(new TextComponent("Inventory changes have been saved to " + targetPlayer.getName().getString() + "'s inventory."), Util.NIL_UUID);
            } else {
                currentPlayer.sendMessage(new TextComponent("Could not sync inventory changes. Target player not found."), Util.NIL_UUID);
            }
        }
    }
}
