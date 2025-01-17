package ch.framedev.essentialsmod.commands;

import java.util.HashMap;
import java.util.Map;

import ch.framedev.essentialsmod.EssentialsMod;
import ch.framedev.essentialsmod.utils.Config;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.snakeyaml.engine.v2.nodes.Tag;

public class BackpackCommand implements ICommand {

    public static final Map<String, SimpleContainer> backPack = new HashMap<>();

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("backpack")
                .executes(this::execute);
    }

    private int execute(CommandContext<CommandSourceStack> command) {
        if (!(command.getSource().getEntity() instanceof ServerPlayer serverPlayer)) {
            command.getSource().sendFailure(new TextComponent("Only players can use this command!"));
            return 0;
        }

        new Config().getConfig().addClassPath(Tag.MAP, CompoundTag.class);
        load();
        // Open a custom backpack inventory for the player
        openBackpack(serverPlayer);
        return 1;
    }

    public static void save() {
        Config config = new Config();
        config.getConfig().set("backpack", null);
        config.getConfig().save();
        for (Map.Entry<String, SimpleContainer> entry : backPack.entrySet()) {
            for (int i = 0; i < entry.getValue().getContainerSize(); i++) {
                ItemStack item = entry.getValue().getItem(i);
                if (!item.isEmpty()) {
                    // Convert CompoundTag to string for YAML storage
                    config.getConfig().set("backpack." + entry.getKey() + ".slot" + i, item.serializeNBT().toString());
                }
            }
        }
        config.getConfig().save();
    }

    public static void load() {
        Config config = new Config();
        if (config.getConfig().getData().containsKey("backpack")) {
            for (String key : config.getConfig().getMap("backpack").keySet()) {
                SimpleContainer backpackInventory = new SimpleContainer(27);
                for (int i = 0; i < 27; i++) {
                    String nbtData = (String) config.getConfig().get("backpack." + key + ".slot" + i);
                    if (nbtData != null) {
                        try {
                            // Parse the string back into a CompoundTag
                            CompoundTag itemTag = TagParser.parseTag(nbtData);
                            ItemStack item = ItemStack.of(itemTag);
                            backpackInventory.setItem(i, item);
                        } catch (Exception e) {
                            System.err.println("Failed to parse NBT data for backpack slot " + i + ": " + e.getMessage());
                        }
                    }
                }
                backPack.put(key, backpackInventory);
            }
        }
    }

    private void openBackpack(ServerPlayer player) {
        // Get or create the player's backpack inventory
        SimpleContainer backpackInventory = backPack.computeIfAbsent(player.getStringUUID(), key -> new SimpleContainer(27));

        // Open a chest-like GUI for the player
        player.openMenu(new SimpleMenuProvider(
                (id, playerInventory, playerEntity) ->
                        new ChestMenu(MenuType.GENERIC_9x3, id, playerInventory, backpackInventory, 3),
                Component.nullToEmpty(player.getName().getString() + "'s BackPack")));
    }

    @Mod.EventBusSubscriber(modid = "essentials") // Replace it with your mod's ID
    public static class InventorySyncHandler {

        @SubscribeEvent
        public static void onContainerClose(PlayerContainerEvent.Close event) {
            if (event.getPlayer() instanceof ServerPlayer serverPlayer) {
                SimpleContainer backpackInventory = backPack.get(serverPlayer.getStringUUID());
                if (backpackInventory != null) {
                    BackpackCommand.save();
                }
            }
        }
    }
}
