package ch.framedev.essentialsmod.commands;

import ch.framedev.essentialsmod.utils.Config;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public class SetSpawnCommand {

    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("setspawn") // Command base
                .requires(source -> source.hasPermission(3)) // Restrict to operators (level 3 or higher)
                .executes(SetSpawnCommand::execute); // Executes when command is provided
    }

    private static int execute(CommandContext<CommandSourceStack> command) {
        if (command.getSource().getEntity() instanceof ServerPlayer player) {
            if (!player.hasPermissions(3))
                return 0;

            try {
                ServerLevel world = command.getSource().getLevel(); // Get the current world
                Config config = new Config();

                // Save spawn data to the config
                config.getConfig().set("spawn.dimension", player.level.dimension().location().toString());
                config.getConfig().set("spawn.x", player.getBlockX());
                config.getConfig().set("spawn.y", player.getBlockY());
                config.getConfig().set("spawn.z", player.getBlockZ());
                config.getConfig().save();

                // Set the world spawn
                BlockPos playerPos = new BlockPos(player.getBlockX(), player.getBlockY(), player.getBlockZ());
                world.setDefaultSpawnPos(playerPos, 0); // Set spawn and yaw (default 0)

                // Notify the player
                player.sendMessage(new TextComponent("World spawn set to your current position: " +
                        player.getBlockX() + ", " + player.getBlockY() + ", " + player.getBlockZ()), Util.NIL_UUID);

                return 1; // Indicate success
            } catch (Exception ex) {
                ex.printStackTrace();
                player.sendMessage(new TextComponent("An error occurred while setting the world spawn."), Util.NIL_UUID);
                return 0; // Indicate failure
            }
        }
        return 0; // Command was not executed by a player
    }
}
