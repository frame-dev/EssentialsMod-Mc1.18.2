package ch.framedev.essentialsmod.commands;

import ch.framedev.essentialsmod.utils.ChatUtils;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class InvseeCommand implements ICommand {

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("invsee")
                .requires(source -> source.hasPermission(3)) // Restrict to operators (level 3 or higher)
                .then(Commands.argument("playerName", StringArgumentType.word())
                        .suggests(PLAYER_SUGGESTION)
                        .executes(this::executeWithContext));
    }

    private int executeWithContext(CommandContext<CommandSourceStack> context) {
        String playerName = StringArgumentType.getString(context, "playerName");
        CommandSourceStack source = context.getSource();

        if (source.getEntity() instanceof ServerPlayer currentPlayer) {
            MinecraftServer server = source.getServer();
            ServerPlayer targetPlayer = server.getPlayerList().getPlayerByName(playerName);

            if (targetPlayer != null) {
                openPlayerInventory(currentPlayer, targetPlayer);
            } else {
                currentPlayer.sendMessage(
                        ChatUtils.getPrefix().append(new TextComponent("Player \"" + playerName + "\" is not online.")
                                .withStyle(ChatFormatting.RED)),
                        Util.NIL_UUID
                );
            }
        }
        return 1;
    }

    private void openPlayerInventory(ServerPlayer currentPlayer, ServerPlayer targetPlayer) {
        SimpleContainer virtualInventory = new SimpleContainer(36);
        for (int i = 0; i < targetPlayer.getInventory().items.size(); i++) {
            virtualInventory.setItem(i, targetPlayer.getInventory().items.get(i));
        }

        // Open other's Inventory in a VirtualInventory
        currentPlayer.openMenu(new SimpleMenuProvider(
                (id, playerInventory, playerEntity) ->
                        new VirtualInventoryMenu(id, playerInventory, virtualInventory, targetPlayer),
                Component.nullToEmpty(targetPlayer.getName().getString() + "'s Inventory")
        ));
    }

    private final SuggestionProvider<CommandSourceStack> PLAYER_SUGGESTION = (context, builder) -> {
        for (ServerPlayer player : context.getSource().getServer().getPlayerList().getPlayers()) {
            builder.suggest(player.getGameProfile().getName());
        }
        return builder.buildFuture();
    };

    public static class VirtualInventoryMenu extends AbstractContainerMenu {

        private final SimpleContainer virtualInventory;
        private final ServerPlayer targetPlayer;

        public VirtualInventoryMenu(int id, Inventory playerInventory, SimpleContainer virtualInventory, ServerPlayer targetPlayer) {
            super(MenuType.GENERIC_9x4, id);
            this.virtualInventory = virtualInventory;
            this.targetPlayer = targetPlayer;

            // Add virtual inventory slots (9x4 layout)
            for (int row = 0; row < 4; ++row) {
                for (int col = 0; col < 9; ++col) {
                    this.addSlot(new Slot(virtualInventory, col + row * 9, 8 + col * 18, 18 + row * 18));
                }
            }

            // Add player inventory slots
            for (int row = 0; row < 3; ++row) {
                for (int col = 0; col < 9; ++col) {
                    this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
                }
            }

            // Add player hot bar slots
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
            }
        }

        @Override
        public boolean stillValid(@NotNull Player player) {
            return true;
        }

        @Override
        public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
            Slot slot = this.slots.get(index);
            if (slot.hasItem()) {
                ItemStack stack = slot.getItem();
                ItemStack originalStack = stack.copy();

                if (index < this.virtualInventory.getContainerSize()) {
                    // Move from virtual inventory to player inventory
                    if (!this.moveItemStackTo(stack, this.virtualInventory.getContainerSize(), this.slots.size(), true)) {
                        return ItemStack.EMPTY;
                    }
                } else {
                    // Move from player inventory to virtual inventory
                    if (!this.moveItemStackTo(stack, 0, this.virtualInventory.getContainerSize(), false)) {
                        return ItemStack.EMPTY;
                    }
                }

                if (stack.isEmpty()) {
                    slot.set(ItemStack.EMPTY);
                } else {
                    slot.setChanged();
                }

                if (stack.getCount() == originalStack.getCount()) {
                    return ItemStack.EMPTY;
                }

                slot.onTake(player, stack);
                return originalStack;
            }
            return ItemStack.EMPTY;
        }

        public SimpleContainer getVirtualInventory() {
            return virtualInventory;
        }

        public ServerPlayer getTargetPlayer() {
            return targetPlayer;
        }
    }
}