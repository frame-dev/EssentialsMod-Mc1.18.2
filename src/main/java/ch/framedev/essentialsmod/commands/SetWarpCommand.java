package ch.framedev.essentialsmod.commands;

import ch.framedev.essentialsmod.utils.*;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;

public class SetWarpCommand implements ICommand {

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("setwarp")
                .requires(source -> source.hasPermission(2)) // Restrict to operators or permission level 2+
                .then(Commands.argument("warpName", StringArgumentType.word())
                        .executes(this::execute));
    }

    private int execute(CommandContext<CommandSourceStack> command) {
        if (!EssentialsConfig.enableWarps.get())
            return 0; // Warps are disabled

        String warpName = command.getArgument("warpName", String.class);

        if (command.getSource().getEntity() instanceof ServerPlayer serverPlayer) {
            String dimension = serverPlayer.level.dimension().location().toString();
            LocationsManager.setWarp(warpName, new Location(dimension, serverPlayer.getBlockX(), serverPlayer.getBlockY(), serverPlayer.getBlockZ()));

            // Feedback to the player
            String[] messages = new String[]{
                    "Warp",
                    "\"" + warpName + "\"",
                    "successfully set at your current location!"
            };
            String[] formatting = new String[]{
                    "§a",
                    "§b",
                    "§a",
            };
            TextComponent textComponent = ChatUtils.getTextComponent(messages, formatting);
            serverPlayer.sendMessage(ChatUtils.getPrefix().append(textComponent), Util.NIL_UUID);
            return 1; // Indicate success
        }

        return 0; // Indicate failure
    }
}