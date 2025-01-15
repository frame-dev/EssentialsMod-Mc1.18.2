package ch.framedev.essentialsmod.commands;

import ch.framedev.essentialsmod.utils.ChatUtils;
import ch.framedev.essentialsmod.utils.EssentialsConfig;
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
            if (!EssentialsConfig.useBack.get())
                return 0; // Warps are disabled

            if (backMap.containsKey(serverPlayer)) {
                Vec3 vec3 = backMap.get(serverPlayer);
                serverPlayer.teleportTo(vec3.x, vec3.y, vec3.z);
                serverPlayer.sendMessage(ChatUtils.getPrefix().append(new TextComponent("You have been teleported back to your Death Location!").withStyle(ChatFormatting.GREEN)), Util.NIL_UUID);
                return 1;
            } else {
                serverPlayer.sendMessage(ChatUtils.getPrefix().append(new TextComponent("Your Death Location can't be found!").withStyle(ChatFormatting.RED)), Util.NIL_UUID);
                return 0;
            }
        }
        return 0;
    }
}
