package ch.framedev.essentialsmod.commands;

import ch.framedev.essentialsmod.utils.Config;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.levelgen.Heightmap;

public class SpawnCommand {

    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("spawn") // Command base
                .executes(SpawnCommand::execute); // Executes when the command is provided
    }

    private static int execute(CommandContext<CommandSourceStack> command) {
        if (command.getSource().getEntity() instanceof ServerPlayer player) {
            ServerLevel world = command.getSource().getLevel(); // Get the current world

            try {
                // Attempt to fetch spawn coordinates from the config
                Config config = new Config();
                if (!config.containsKey("spawn.dimension")) {
                    player.sendMessage(new TextComponent("The Spawn you set is Invalid. Teleporting to Coordinates in the Overworld!"), Util.NIL_UUID);
                    int x = config.getConfig().getInt("spawn.x");
                    int y = config.getConfig().getInt("spawn.y");
                    int z = config.getConfig().getInt("spawn.z");
                    player.teleportTo(x, y, z); // Center the player on the block
                    player.sendMessage(new TextComponent("Teleported to configured spawn point!"), Util.NIL_UUID);
                    return 1; // Successful teleportation
                }
                String dimension = config.getString("spawn.dimension");
                int x = config.getConfig().getInt("spawn.x");
                int y = config.getConfig().getInt("spawn.y");
                int z = config.getConfig().getInt("spawn.z");
                ResourceKey<Level> dimensionLevel = ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(dimension));
                if (player.getServer() == null) {
                    return 0; // Server not found
                }
                ServerLevel targetLevel = player.getServer().getLevel(dimensionLevel);
                if (targetLevel != null) {
                    // Teleport the player to the configured spawn point
                    player.teleportTo(targetLevel, x, y, z, 0f, 0f); // Center the player on the block
                    player.sendMessage(new TextComponent("Teleported to configured spawn point!"), Util.NIL_UUID);
                } else {
                    // Fallback to the world's shared spawn point
                    BlockPos spawnPos = world.getSharedSpawnPos();
                    BlockPos safeSpawnPos = findSafeSpawn(world, spawnPos);

                    if (safeSpawnPos != null) {
                        player.teleportTo(safeSpawnPos.getX() + 0.5, safeSpawnPos.getY(), safeSpawnPos.getZ() + 0.5);
                        player.sendMessage(new TextComponent("Teleported to the world's safe spawn point!"), Util.NIL_UUID);
                    } else {
                        player.sendMessage(new TextComponent("Unable to find a safe spawn location.").withStyle(ChatFormatting.RED), Util.NIL_UUID);
                    }
                }
                return 1; // Successful teleportation
            } catch (Exception ex) {
                // Fallback to the world's shared spawn point
                BlockPos spawnPos = world.getSharedSpawnPos();
                BlockPos safeSpawnPos = findSafeSpawn(world, spawnPos);

                if (safeSpawnPos != null) {
                    player.teleportTo(safeSpawnPos.getX() + 0.5, safeSpawnPos.getY(), safeSpawnPos.getZ() + 0.5);
                    player.sendMessage(new TextComponent("Teleported to the world's safe spawn point!"), Util.NIL_UUID);
                } else {
                    player.sendMessage(new TextComponent("Unable to find a safe spawn location.").withStyle(ChatFormatting.RED), Util.NIL_UUID);
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