package ch.framedev.essentialsmod.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;

import java.util.UUID;

import static net.minecraft.world.entity.EquipmentSlot.MAINHAND;

public class AdminSwordCommand {

    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("adminsword")
                .requires(commandSourceStack -> commandSourceStack.hasPermission(3))
                .executes(AdminSwordCommand::executeDefault); // Executes with no arguments
    }

    private static int executeDefault(CommandContext<CommandSourceStack> command) {
        if(command.getSource().getEntity() instanceof Player player) {
            if(!player.hasPermissions(2))
                return 0;
            ItemStack itemStack = new ItemStack(Items.NETHERITE_SWORD);
            UUID uuid = UUID.randomUUID();

            // Create the AttributeModifier
            AttributeModifier modifier = new AttributeModifier(uuid, "Attack Damage Modifier", 300, AttributeModifier.Operation.ADDITION);

            // Add the attribute modifier to the item
            itemStack.addAttributeModifier(Attributes.ATTACK_DAMAGE, modifier, MAINHAND);

            itemStack.enchant(Enchantments.SHARPNESS, 126);
            player.getInventory().add(itemStack);
            return 1;
        }
        return 0;
    }
}
