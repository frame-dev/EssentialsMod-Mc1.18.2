package ch.framedev.essentialsmod.commands;

import ch.framedev.essentialsmod.utils.ChatUtils;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;

import java.util.UUID;

import static net.minecraft.world.entity.EquipmentSlot.MAINHAND;

public class AdminSwordCommand implements ICommand {

    public LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("adminsword")
                .requires(commandSourceStack -> commandSourceStack.hasPermission(3))
                .then(Commands.argument("looting", IntegerArgumentType.integer())
                        .executes(this::executeDefaultLooting))
                .then(Commands.argument("playerName", StringArgumentType.word())
                        .suggests(PLAYER_SUGGESTION)
                        .then(Commands.argument("looting", IntegerArgumentType.integer())
                                .executes(this::executeWithPlayerNameLooting))
                        .executes(this::executeWithPlayerName))
                .executes(this::executeDefault); // Executes with no arguments
    }

    private int executeDefaultLooting(CommandContext<CommandSourceStack> command) {
        if (command.getSource().getEntity() instanceof Player player) {
            boolean looting = IntegerArgumentType.getInteger(command, "looting") == 1;
            ItemStack itemStack = new ItemStack(Items.NETHERITE_SWORD);
            UUID uuid = UUID.randomUUID();

            // Create the AttributeModifier
            AttributeModifier modifier = new AttributeModifier(uuid, "Attack Damage Modifier", 300, AttributeModifier.Operation.ADDITION);

            // Add the attribute modifier to the item
            itemStack.addAttributeModifier(Attributes.ATTACK_DAMAGE, modifier, MAINHAND);

            itemStack.enchant(Enchantments.SHARPNESS, 126);
            if (looting)
                itemStack.enchant(Enchantments.MOB_LOOTING, 100);
            player.getInventory().add(itemStack);
            command.getSource().sendSuccess(ChatUtils.getPrefix().append(
                    new TextComponent("You successfully got the Admin Sword!").withStyle(ChatFormatting.GREEN)), true);
            return 1;
        }
        return 0;
    }

    private int executeWithPlayerNameLooting(CommandContext<CommandSourceStack> command) {
        String playerName = StringArgumentType.getString(command, "playerName");
        boolean looting = IntegerArgumentType.getInteger(command, "looting") == 1;
        ServerPlayer player = command.getSource().getServer().getPlayerList().getPlayerByName(playerName);
        if (player != null) {
            ItemStack itemStack = new ItemStack(Items.NETHERITE_SWORD);
            UUID uuid = UUID.randomUUID();

            // Create the AttributeModifier
            AttributeModifier modifier = new AttributeModifier(uuid, "Attack Damage Modifier", 300, AttributeModifier.Operation.ADDITION);

            // Add the attribute modifier to the item
            itemStack.addAttributeModifier(Attributes.ATTACK_DAMAGE, modifier, MAINHAND);

            itemStack.enchant(Enchantments.SHARPNESS, 126);
            if (looting)
                itemStack.enchant(Enchantments.MOB_LOOTING, 100);
            player.getInventory().add(itemStack);
            player.sendMessage(ChatUtils.getPrefix().append(
                            new TextComponent("You successfully got the Admin Sword!")),
                    player.getUUID());
            command.getSource().sendSuccess(new TextComponent(playerName +
                    " has now the Adminsword!"), true);
            return 1;
        } else {
            command.getSource().sendFailure(
                    ChatUtils.getPrefix().append(new TextComponent("Player not found!")
                            .withStyle(ChatFormatting.RED)));
            return 0;
        }
    }

    private int executeWithPlayerName(CommandContext<CommandSourceStack> command) {
        String playerName = StringArgumentType.getString(command, "playerName");
        ServerPlayer player = command.getSource().getServer().getPlayerList().getPlayerByName(playerName);
        if (player != null) {
            ItemStack itemStack = new ItemStack(Items.NETHERITE_SWORD);
            UUID uuid = UUID.randomUUID();

            // Create the AttributeModifier
            AttributeModifier modifier = new AttributeModifier(uuid, "Attack Damage Modifier", 300, AttributeModifier.Operation.ADDITION);

            // Add the attribute modifier to the item
            itemStack.addAttributeModifier(Attributes.ATTACK_DAMAGE, modifier, MAINHAND);

            itemStack.enchant(Enchantments.SHARPNESS, 126);
            player.getInventory().add(itemStack);
            player.sendMessage(ChatUtils.getPrefix().append(
                            new TextComponent("You successfully got the Admin Sword!")),
                    player.getUUID());
            command.getSource().sendSuccess(new TextComponent(playerName +
                    " has now the Adminsword!"), true);
            return 1;
        } else {
            command.getSource().sendFailure(
                    ChatUtils.getPrefix().append(new TextComponent("Player not found!")
                            .withStyle(ChatFormatting.RED)));
            return 0;
        }
    }

    private int executeDefault(CommandContext<CommandSourceStack> command) {
        if (command.getSource().getEntity() instanceof Player player) {
            ItemStack itemStack = new ItemStack(Items.NETHERITE_SWORD);
            UUID uuid = UUID.randomUUID();

            // Create the AttributeModifier
            AttributeModifier modifier = new AttributeModifier(uuid, "Attack Damage Modifier", 300, AttributeModifier.Operation.ADDITION);

            // Add the attribute modifier to the item
            itemStack.addAttributeModifier(Attributes.ATTACK_DAMAGE, modifier, MAINHAND);

            itemStack.enchant(Enchantments.SHARPNESS, 126);
            player.getInventory().add(itemStack);
            command.getSource().sendSuccess(ChatUtils.getPrefix().append(
                    new TextComponent("You successfully got the Admin Sword!").withStyle(ChatFormatting.GREEN)), true);
            return 1;
        }
        return 0;
    }

    private final SuggestionProvider<CommandSourceStack> PLAYER_SUGGESTION = (context, builder) -> {
        for (ServerPlayer player : context.getSource().getServer().getPlayerList().getPlayers()) {
            builder.suggest(player.getGameProfile().getName()); // Add player names to the suggestions
        }
        return builder.buildFuture();
    };
}
