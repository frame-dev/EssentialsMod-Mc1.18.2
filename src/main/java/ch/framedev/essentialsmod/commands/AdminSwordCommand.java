package ch.framedev.essentialsmod.commands;

import ch.framedev.essentialsmod.utils.ChatUtils;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
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
                .then(Commands.argument("playerName", StringArgumentType.word())
                        .suggests(PLAYER_SUGGESTION)
                        .executes(this::executeWithPlayerName))
                .executes(this::executeDefault); // Executes with no arguments
    }

    private int executeWithPlayerName(CommandContext<CommandSourceStack> command) {
        String playerName = StringArgumentType.getString(command, "playerName");
        ServerPlayer player = command.getSource().getServer().getPlayerList().getPlayerByName(playerName);
        if(player != null) {
            if (!player.hasPermissions(2))
                return 0;
            ItemStack itemStack = new ItemStack(Items.NETHERITE_SWORD);
            UUID uuid = UUID.randomUUID();

            // Create the AttributeModifier
            AttributeModifier modifier = new AttributeModifier(uuid, "Attack Damage Modifier", 300, AttributeModifier.Operation.ADDITION);

            // Add the attribute modifier to the item
            itemStack.addAttributeModifier(Attributes.ATTACK_DAMAGE, modifier, MAINHAND);

            itemStack.enchant(Enchantments.SHARPNESS, 126);
            player.getInventory().add(itemStack);
            command.getSource().sendSuccess(ChatUtils.getPrefix().append(new TextComponent("You successfully got the Admin Sword!")), true);
            return 1;
        } else {
            command.getSource().sendFailure(ChatUtils.getPrefix().append(new TextComponent("Player not found!")));
            return 0;
        }
    }

    private int executeDefault(CommandContext<CommandSourceStack> command) {
        if (command.getSource().getEntity() instanceof Player player) {
            if (!player.hasPermissions(2))
                return 0;
            ItemStack itemStack = new ItemStack(Items.NETHERITE_SWORD);
            UUID uuid = UUID.randomUUID();

            // Create the AttributeModifier
            AttributeModifier modifier = new AttributeModifier(uuid, "Attack Damage Modifier", 300, AttributeModifier.Operation.ADDITION);

            // Add the attribute modifier to the item
            itemStack.addAttributeModifier(Attributes.ATTACK_DAMAGE, modifier, MAINHAND);

            itemStack.enchant(Enchantments.SHARPNESS, 126);
            player.getInventory().add(itemStack);
            command.getSource().sendSuccess(ChatUtils.getPrefix().append(new TextComponent("You successfully got the Admin Sword!")), true);
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
