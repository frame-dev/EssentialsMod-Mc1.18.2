package ch.framedev.essentialsmod.events;

/*
 * ch.framedev.essentialsmod.events
 * =============================================
 * This File was Created by FrameDev
 * Please do not change anything without my consent!
 * =============================================
 * This Class was created at 30.01.2025 19:17
 */

import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber
public class SignEventHandler {

    @SubscribeEvent
    public static void onSignClickRight(PlayerInteractEvent.RightClickBlock event) {
        // Ensure the world is not client-side
        if (event.getWorld().isClientSide()) {
            return;
        }

        // Check if the clicked block is a sign
        if (event.getWorld().getBlockEntity(event.getPos()) instanceof SignBlockEntity sign && event.getEntity() instanceof ServerPlayer serverPlayer) {
            handleSignFreeEvent(sign, serverPlayer);
        }
    }

    private static void handleSignFreeEvent(SignBlockEntity sign, ServerPlayer player) {
        // Get the first two lines of the sign
        Component line1 = sign.getMessage(0, false);
        if (!line1.getString().equalsIgnoreCase("[FREE]")) return;
        Component line2 = sign.getMessage(1, false);

        String itemName = line2.getString().trim();
        if (itemName.isEmpty()) return; // Prevent errors with empty lines

        // Determine item amount based on shift-click
        int amount = player.isShiftKeyDown() ? 64 : 1;

        // Retrieve the item
        ItemStack itemStack = ItemUtils.getItemFromString(itemName, amount);

        if (itemStack.isEmpty() || itemStack.getItem() == Items.AIR) return;

        // Check if the first line of the sign is "[FREE]"
        player.addItem(itemStack);
        player.sendMessage(new TextComponent("You received " + amount + " " + itemName + "!"), player.getUUID());
    }

    public static class ItemUtils {

        public static ItemStack getItemFromString(String itemName, int amount) {
            // Automatically add "minecraft:" if no namespace is provided
            if (!itemName.contains(":")) {
                itemName = "minecraft:" + itemName;
            }

            // Convert string to ResourceLocation
            ResourceLocation itemID = new ResourceLocation(itemName);

            // Get the item from ForgeRegistries
            Item item = ForgeRegistries.ITEMS.getValue(itemID);

            // Check if the item exists
            if (item == null) {
                return ItemStack.EMPTY; // Return empty if item is invalid
            }

            return new ItemStack(item, amount);
        }
    }
}