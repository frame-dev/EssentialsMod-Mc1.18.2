package ch.framedev.essentialsmod.commands;

import ch.framedev.essentialsmod.Config;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;

public class SetSpawnCommand {

    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("setspawn") // Command base
                .requires(source -> source.hasPermission(3)) // Restrict to operators (level 3 or higher)
                .executes(SetSpawnCommand::execute); // Executes when command is provided
    }

    private static int execute(CommandContext<CommandSourceStack> command) {
        if(command.getSource().getEntity() instanceof Player player) {
            if(!player.hasPermissions(3))
                return 0;
            try {
                ServerLevel world = command.getSource().getLevel(); // Get the current world
                Config config = new Config();
                config.getConfig().set("spawn.x", player.getBlockX());
                config.getConfig().set("spawn.y", player.getBlockY());
                config.getConfig().set("spawn.z", player.getBlockZ());
                config.getConfig().save();
                player.sendMessage(new TextComponent("World spawn set to your current position: " +
                        player.getX() + ", " + player.getY() + ", " + player.getZ()), Util.NIL_UUID);
                BlockPos playerPos = new BlockPos(player.getBlockX(), player.getBlockY(), player.getBlockZ());
                world.setDefaultSpawnPos(playerPos, 0); // Set spawn and yaw (default 0)
                return 1;
            } catch (Exception ex) {
                ex.printStackTrace();
                ServerLevel world = command.getSource().getLevel(); // Get the current world

                // Set the default spawn position to the player's current position
                BlockPos playerPos = new BlockPos(player.getBlockX(), player.getBlockY(), player.getBlockZ());
                world.setDefaultSpawnPos(playerPos, 0); // Set spawn and yaw (default 0)

                // Notify the player
                player.sendMessage(new TextComponent("World spawn set to your current position: " +
                        playerPos.getX() + ", " + playerPos.getY() + ", " + playerPos.getZ()), Util.NIL_UUID);
                return 1;
            }
        }
        return 0;
    }
}
