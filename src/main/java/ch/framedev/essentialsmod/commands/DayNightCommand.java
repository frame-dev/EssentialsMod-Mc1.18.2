package ch.framedev.essentialsmod.commands;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerLevel;

public class DayNightCommand {

    public static int setDay(CommandSourceStack source) {
        ServerLevel world = source.getLevel();
        world.setDayTime(1000); // Set the time to day
        source.sendSuccess(new TextComponent("Time set to day!"), true);
        return 1; // Success
    }

    public static int setNight(CommandSourceStack source) {
        ServerLevel world = source.getLevel();
        world.setDayTime(13000); // Set the time to night
        source.sendSuccess(new TextComponent("Time set to night!"), true);
        return 1; // Success
    }
}
