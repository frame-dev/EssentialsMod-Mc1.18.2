package ch.framedev.essentialsmod.commands;

import ch.framedev.essentialsmod.utils.Config;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;

public class SetWarpCommand {

    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("setwarp")
                .requires(source -> source.hasPermission(2)) // Restrict to operators or permission level 2+
                .then(Commands.argument("warpName", StringArgumentType.word())
                        .executes(SetWarpCommand::execute));
    }

    private static int execute(CommandContext<CommandSourceStack> command) {
        String warpName = command.getArgument("warpName", String.class);
        Config config = new Config();

        if (command.getSource().getEntity() instanceof ServerPlayer serverPlayer) {
            String warpPath = "warp." + warpName;

            // Save the warp location in the config
            config.getConfig().set(warpPath + ".x", serverPlayer.getBlockX());
            config.getConfig().set(warpPath + ".y", serverPlayer.getBlockY());
            config.getConfig().set(warpPath + ".z", serverPlayer.getBlockZ());
            config.getConfig().save();

            // Feedback to the player
            serverPlayer.sendMessage(new TextComponent("Warp \"" + warpName + "\" successfully set at your current location!"), Util.NIL_UUID);
            return 1; // Indicate success
        }

        return 0; // Indicate failure
    }
}