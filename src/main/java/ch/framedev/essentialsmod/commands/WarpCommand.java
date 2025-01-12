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

public class WarpCommand {

    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("warp")
                .then(Commands.argument("warpName", StringArgumentType.word())
                        .suggests(WARP_SUGGESTIONS)
                        .executes(WarpCommand::execute));
    }

    private static int execute(CommandContext<CommandSourceStack> command) {
        try {
            ServerPlayer player = command.getSource().getPlayerOrException();
            String warpName = command.getArgument("warpName", String.class);

            if (teleportToWarp(player, warpName)) {
                player.sendMessage(new TextComponent("Teleported to warp \"" + warpName + "\"."), Util.NIL_UUID);
                return 1; // Success
            } else {
                player.sendMessage(new TextComponent("Warp \"" + warpName + "\" not found."), Util.NIL_UUID);
                return 0; // Failure
            }
        } catch (CommandSyntaxException e) {
            command.getSource().sendFailure(new TextComponent("An error occurred while executing the /warp command."));
            return 0;
        }
    }

    public static boolean teleportToWarp(ServerPlayer player, String warpName) {
        Config config = new Config();
        Map<String, Object> warps = config.getConfig().getMap("warp");

        if (!config.getConfig().containsKey("warp." + warpName + ".x")) {
            return false; // Home not found
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> warpData = (Map<String, Object>) warps.get(warpName);

        int x = (int) warpData.getOrDefault("x", player.getX());
        int y = (int) warpData.getOrDefault("y", player.getY());
        int z = (int) warpData.getOrDefault("z", player.getZ());

        player.teleportTo(x, y, z);
        return true; // Warp successful
    }

    private static List<String> getAllWarps() {
        Config config = new Config();
        Map<String, Object> warps = config.getConfig().getMap("warp");

        if (warps == null) return new ArrayList<>();

        return new ArrayList<>(warps.keySet());
    }

    private static final SuggestionProvider<CommandSourceStack> WARP_SUGGESTIONS = (context, builder) -> {
        List<String> warps = getAllWarps();
        warps.forEach(builder::suggest);
        return builder.buildFuture();
    };
}