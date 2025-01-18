package ch.framedev.essentialsmod.commands;

import ch.framedev.essentialsmod.EssentialsMod;
import ch.framedev.essentialsmod.utils.Config;
import ch.framedev.yamlutils.FileConfiguration;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class BackpackCommand implements ICommand {
    private static final Map<String, SimpleContainer> backpacks = new HashMap<>();

    public static FileConfiguration config = new Config().getConfig();

    public LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("backpack")
                .executes(this::execute);
    }

    private int execute(CommandContext<CommandSourceStack> command) {
        CommandSourceStack source = command.getSource();
        if (source.getEntity() instanceof ServerPlayer player) {
            String uuid = player.getStringUUID();

            // Load backpack if not already loaded
            if (!backpacks.containsKey(uuid)) {
                loadBackpack(uuid);
            }

            // Create a new inventory for the player
            SimpleContainer backpack = backpacks.computeIfAbsent(uuid, k -> new SimpleContainer(27));

            player.openMenu(new SimpleMenuProvider(
                    (id, playerInventory, playerEntity) -> new ChestMenu(MenuType.GENERIC_9x3, id, playerInventory, backpack, 3),
                    new TextComponent(player.getName().getString() + "'s Backpack")
            ));
            return 1;
        }
        source.sendFailure(new TextComponent("Only players can use this command!"));
        return 0;
    }

    private static void loadBackpack(String uuid) {
        if (config.containsKey(uuid)) {
            SimpleContainer backpack = new SimpleContainer(27); // Default size is 3 rows
            for (int i = 0; i < 27; i++) {
                String nbtString = (String) config.get(uuid + ".slot" + i);
                if (nbtString != null) {
                    try {
                        ItemStack item = InventoryUtils.deserializeItemStack(nbtString);
                        backpack.setItem(i, item);
                    } catch (IOException | CommandSyntaxException e) {
                        System.err.println("Failed to load backpack item: " + e.getMessage());
                    }
                }
            }
            backpacks.put(uuid, backpack);
        }
    }

    private static void saveBackpack(String uuid) {
        if (backpacks.containsKey(uuid)) {
            SimpleContainer backpack = backpacks.get(uuid);
            for (int i = 0; i < backpack.getContainerSize(); i++) {
                ItemStack item = backpack.getItem(i);
                String nbtString = InventoryUtils.serializeItemStack(item);
                config.set(uuid + ".slot" + i, nbtString);
            }
            config.save();
            EssentialsMod.getLOGGER().info("Config saved");
        }
    }

    @Mod.EventBusSubscriber(modid = "essentials") // Replace with your mod ID
    public static class BackpackEventHandler {

        @SubscribeEvent
        public static void onContainerClose(PlayerContainerEvent.Close event) {
            if (event.getPlayer() instanceof ServerPlayer player) {
                String uuid = player.getStringUUID();
                if (backpacks.containsKey(uuid)) {
                    saveBackpack(uuid);
                }
            }
        }
    }

    public static class InventoryUtils {
        public static String serializeItemStack(ItemStack item) {
            return item.save(new CompoundTag()).toString();
        }

        public static ItemStack deserializeItemStack(String nbtString) throws IOException, CommandSyntaxException {
            CompoundTag tag = TagParser.parseTag(nbtString);
            return ItemStack.of(tag);
        }
    }
}