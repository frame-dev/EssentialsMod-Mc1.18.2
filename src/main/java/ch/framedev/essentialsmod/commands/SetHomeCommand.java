package ch.framedev.essentialsmod.commands;

import ch.framedev.essentialsmod.Config;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
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
        if (command.getSource().getEntity() instanceof Player) {
            Player player = (Player) command.getSource().getEntity();
            String playerName = player.getName().getString();
            player.sendMessage(new TextComponent("Home Set with name " + home), Util.NIL_UUID);
            Config config = new Config();
            config.getConfig().set("home." + playerName + "." + home + ".x", player.getBlockX());
            config.getConfig().set("home." + playerName + "." + home + ".y", player.getBlockY());
            config.getConfig().set("home." + playerName + "." + home + ".z", player.getBlockZ());
            config.getConfig().save();
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int execute(CommandContext<CommandSourceStack> command) {
        if (command.getSource().getEntity() instanceof Player) {
            Player player = (Player) command.getSource().getEntity();
            String playerName = player.getName().getString();
            player.sendMessage(new TextComponent("Home Set"), Util.NIL_UUID);
            Config config = new Config();
            config.getConfig().set("home." + playerName + ".home.x", player.getBlockX());
            config.getConfig().set("home." + playerName + ".home.y", player.getBlockY());
            config.getConfig().set("home." + playerName + ".home.z", player.getBlockZ());
            config.getConfig().save();
        }
        return Command.SINGLE_SUCCESS;
    }
}