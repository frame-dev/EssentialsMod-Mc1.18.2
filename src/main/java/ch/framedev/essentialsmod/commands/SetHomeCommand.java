package ch.framedev.essentialsmod.commands;

import ch.framedev.essentialsmod.utils.ChatUtils;
import ch.framedev.essentialsmod.utils.Location;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.player.Player;

public class SetHomeCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("sethome") // Command base
                .then(Commands.argument("homeName", StringArgumentType.word()) // String argument
                        .executes(SetHomeCommand::executeWithContext)) // Executes when only message is provided
                .executes(SetHomeCommand::execute); // Executes with no args
    }

    private static int executeWithContext(CommandContext<CommandSourceStack> command) {
        String home = StringArgumentType.getString(command, "homeName");
        if (command.getSource().getEntity() instanceof Player player) {
            String playerName = player.getName().getString();
            TextComponent textComponent = ChatUtils.getTextComponent(new String[]{"Home set with name", "\"" + home + "\""}, new String[]{"§a", "§b"});
            player.sendMessage(ChatUtils.getPrefix().append(textComponent), Util.NIL_UUID);
            String dimension = player.level.dimension().location().toString();
            LocationsManager.setHome(playerName, new Location(dimension, player.getBlockX(), player.getBlockY(), player.getBlockZ()), home);
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int execute(CommandContext<CommandSourceStack> command) {
        if (command.getSource().getEntity() instanceof Player player) {
            String playerName = player.getName().getString();
            player.sendMessage(ChatUtils.getPrefix().append(new TextComponent("Home Set").withStyle(ChatFormatting.GREEN)), Util.NIL_UUID);
            String dimension = player.level.dimension().location().toString();
            LocationsManager.setHome(playerName, new Location(dimension, player.getBlockX(), player.getBlockY(), player.getBlockZ()), null);
        }
        return Command.SINGLE_SUCCESS;
    }
}