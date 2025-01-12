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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.levelgen.Heightmap;

public class SpawnCommand {

    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("spawn") // Command base
                .executes(SpawnCommand::execute); // Executes when the command is provided
    }

    private static int execute(CommandContext<CommandSourceStack> command) {
        if (command.getSource().getEntity() instanceof Player player) {
            ServerLevel world = command.getSource().getLevel(); // Get the current world

            try {
                // Attempt to fetch spawn coordinates from the config
                Config config = new Config();
                int x = config.getConfig().getInt("spawn.x");
                int y = config.getConfig().getInt("spawn.y");
                int z = config.getConfig().getInt("spawn.z");

                // Teleport the player to the configured spawn point
                player.teleportTo(x + 0.5, y, z + 0.5); // Center the player on the block
                player.sendMessage(new TextComponent("Teleported to configured spawn point!"), Util.NIL_UUID);
                return 1; // Successful teleportation
            } catch (Exception ex) {
                // Fallback to the world's shared spawn point
                BlockPos spawnPos = world.getSharedSpawnPos();
                BlockPos safeSpawnPos = findSafeSpawn(world, spawnPos);

                if (safeSpawnPos != null) {
                    player.teleportTo(safeSpawnPos.getX() + 0.5, safeSpawnPos.getY(), safeSpawnPos.getZ() + 0.5);
                    player.sendMessage(new TextComponent("Teleported to the world's safe spawn point!"), Util.NIL_UUID);
                } else {
                    player.sendMessage(new TextComponent("Unable to find a safe spawn location."), Util.NIL_UUID);
                }
            }
        }
        return 0; // Failed to teleport
    }

    private static BlockPos findSafeSpawn(LevelAccessor world, BlockPos pos) {
        // Check for the highest safe block at the spawn position
        int safeY = world.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos.getX(), pos.getZ());
        BlockPos safePos = new BlockPos(pos.getX(), safeY, pos.getZ());

        // Ensure there's a solid block below the spawn point and space for the player
        if (world.getBlockState(safePos.below()).isSolidRender(world, safePos.below()) &&
                world.getBlockState(safePos).isAir() &&
                world.getBlockState(safePos.above()).isAir()) {
            return safePos;
        }

        return null; // Return null if no safe spawn location is found
    }
}