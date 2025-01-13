package ch.framedev.essentialsmod.events;

import ch.framedev.essentialsmod.commands.InvseeCommand;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "essentials") // Replace it with your mod's ID
public class InventorySyncHandler {

    @SubscribeEvent
    public static void onContainerClose(PlayerContainerEvent.Close event) {
        if (event.getContainer() instanceof InvseeCommand.VirtualInventoryMenu menu) {
            ServerPlayer currentPlayer = (ServerPlayer) event.getPlayer(); // The player who opened the menu
            ServerPlayer targetPlayer = menu.getTargetPlayer(); // Get the target player (passed when creating the menu)

            if (targetPlayer != null) {
                SimpleContainer virtualInventory = menu.getVirtualInventory();
                Inventory inventory = targetPlayer.getInventory();

                // Backup armor slots
                ItemStack[] armorBackup = new ItemStack[inventory.armor.size()];
                for (int armorSlot = 0; armorSlot < armorBackup.length; armorSlot++) {
                    armorBackup[armorSlot] = inventory.armor.get(armorSlot).copy();
                }

                // Backup offhand slot
                ItemStack offhandBackup = inventory.offhand.get(0).copy();

                // Synchronize the virtual inventory to the target player's main inventory
                for (int slot = 0; slot < virtualInventory.getContainerSize(); slot++) {
                    if (slot < inventory.items.size()) { // Ensure we only set valid slots
                        inventory.items.set(slot, virtualInventory.getItem(slot).copy());
                    }
                }

                // Restore armor slots
                for (int armorSlot = 0; armorSlot < armorBackup.length; armorSlot++) {
                    inventory.armor.set(armorSlot, armorBackup[armorSlot]);
                }

                // Restore offhand slot
                inventory.offhand.set(0, offhandBackup);

                currentPlayer.sendMessage(new TextComponent("Inventory changes have been saved to " + targetPlayer.getName().getString() + "'s inventory.").withStyle(ChatFormatting.GOLD), Util.NIL_UUID);
            } else {
                currentPlayer.sendMessage(new TextComponent("Could not sync inventory changes. Target player not found.").withStyle(ChatFormatting.RED), Util.NIL_UUID);
            }
        }
    }
}