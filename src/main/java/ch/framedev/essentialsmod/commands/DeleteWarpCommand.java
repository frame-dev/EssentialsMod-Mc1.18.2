package ch.framedev.essentialsmod.commands;

import ch.framedev.essentialsmod.utils.Config;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DeleteWarpCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("delwarp")
                .requires(source -> source.hasPermission(2)) // Restrict to operators or permission level 2+
                .then(Commands.argument("warpName", StringArgumentType.word())
                        .suggests(WARP_SUGGESTIONS)
                        .executes(DeleteWarpCommand::execute));
    }

    private static int execute(CommandContext<CommandSourceStack> command) {
        try {
            ServerPlayer player = command.getSource().getPlayerOrException();
            String warpName = command.getArgument("warpName", String.class);

            if (!existsWarp(warpName)) {
                player.sendMessage(new TextComponent("Warp \"" + warpName + "\" not found."), Util.NIL_UUID);
                return 0; // Warp not found
            }

            if (deleteWarp(warpName)) {
                player.sendMessage(new TextComponent("Warp \"" + warpName + "\" was successfully deleted!"), Util.NIL_UUID);
                return 1; // Success
            } else {
                player.sendMessage(new TextComponent("Failed to delete warp \"" + warpName + "\". Please try again."), Util.NIL_UUID);
                return 0; // Failure
            }
        } catch (CommandSyntaxException e) {
            throw new RuntimeException("Failed to execute /delwarp command", e);
        }
    }

    private static boolean existsWarp(String warpName) {
        Config config = new Config();
        return config.getConfig().getMap("warp") != null && config.getConfig().getMap("warp").containsKey(warpName);
    }

    private static boolean deleteWarp(String warpName) {
        Config config = new Config();
        Map<String, Object> warpConfig = config.getConfig().getMap("warp");

        if (warpConfig != null && warpConfig.containsKey(warpName)) {
            warpConfig.remove(warpName);
            config.getConfig().save();
            return true; // Warp successfully deleted
        }

        return false; // Warp not found
    }

    private static List<String> getAllWarps() {
        Config config = new Config();
        Map<String, Object> warpConfig = config.getConfig().getMap("warp");
        List<String> warps = new ArrayList<>();

        if (warpConfig != null) {
            warps.addAll(warpConfig.keySet());
        }

        return warps;
    }

    private static final SuggestionProvider<CommandSourceStack> WARP_SUGGESTIONS = (context, builder) -> {
        List<String> warps = getAllWarps();
        warps.forEach(builder::suggest);
        return builder.buildFuture();
    };
}
