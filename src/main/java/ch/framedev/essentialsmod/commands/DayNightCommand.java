package ch.framedev.essentialsmod.commands;

import ch.framedev.essentialsmod.utils.ChatUtils;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerLevel;

public class DayNightCommand {

    public static int setDay(CommandSourceStack source) {
        ServerLevel world = source.getLevel();
        world.setDayTime(1000); // Set the time to day
        source.sendSuccess(ChatUtils.getPrefix().append(new TextComponent("Time set to day!").withStyle(ChatFormatting.GREEN)
                .append(new TextComponent(" (1000 Ticks)").withStyle(ChatFormatting.AQUA))), true);
        return 1; // Success
    }

    public static int setNight(CommandSourceStack source) {
        ServerLevel world = source.getLevel();
        world.setDayTime(13000); // Set the time to night
        source.sendSuccess(ChatUtils.getPrefix().append(new TextComponent("Time set to night!").withStyle(ChatFormatting.GREEN)
                .append(new TextComponent(" (13000 Ticks)").withStyle(ChatFormatting.AQUA))), true);
        return 1; // Success
    }

    public static int setTicks(CommandContext<CommandSourceStack> command) {
        ServerLevel world = command.getSource().getLevel();
        int ticks = IntegerArgumentType.getInteger(command, "ticks");
        world.setDayTime(ticks);
        command.getSource().sendSuccess(ChatUtils.getPrefix().append(
                new TextComponent("Time was set to " + ticks + "!").withStyle(ChatFormatting.GREEN)
        ), true);
        return 1; // Success
    }
}
