package ch.framedev.essentialsmod.commands;

import ch.framedev.essentialsmod.Config;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;

public class BackCommand {

    public static Map<ServerPlayer, Vec3> backMap = new HashMap<>();

    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("back")
                .executes(BackCommand::execute);
    }

    private static int execute(CommandContext<CommandSourceStack> command) {
        if (command.getSource().getEntity() instanceof ServerPlayer serverPlayer) {
            Config config = new Config();
            if (config.getConfig().containsKey("back") && config.getConfig().getBoolean("back")) {
                if (backMap.containsKey(serverPlayer)) {
                    Vec3 vec3 = backMap.get(serverPlayer);
                    serverPlayer.teleportTo(vec3.x, vec3.y, vec3.z);
                    serverPlayer.sendMessage(new TextComponent("You have been teleported back to your Death Location!").withStyle(ChatFormatting.GREEN), Util.NIL_UUID);
                    return 1;
                } else {
                    serverPlayer.sendMessage(new TextComponent("Your Death Location can't be found!").withStyle(ChatFormatting.RED), Util.NIL_UUID);
                    return 0;
                }
            } else {
                if (serverPlayer.hasPermissions(2))
                    serverPlayer.sendMessage(new TextComponent("Teleportation back is disabled in the config!").withStyle(ChatFormatting.RED), Util.NIL_UUID);
                return 0;
            }
        }
        return 0;
    }
}
