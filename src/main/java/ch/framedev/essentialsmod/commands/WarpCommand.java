package ch.framedev.essentialsmod.commands;

import ch.framedev.essentialsmod.utils.Config;
import ch.framedev.essentialsmod.utils.ChatUtils;
import ch.framedev.essentialsmod.utils.EssentialsConfig;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

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
        if (!EssentialsConfig.enableWarps.get())
            return 0; // Warps are disabled

        try {
            ServerPlayer player = command.getSource().getPlayerOrException();
            String warpName = command.getArgument("warpName", String.class);

            if (teleportToWarp(player, warpName)) {
                TextComponent textComponent = ChatUtils.getTextComponent(new String[]{"Teleported to warp ", "\"" + warpName + "\"", "."},
                        new String[]{"§a","§b","§a"});
                player.sendMessage(ChatUtils.getPrefix().append(textComponent), Util.NIL_UUID);
                return 1; // Success
            } else {
                player.sendMessage(ChatUtils.getPrefix().append(new TextComponent("Warp \"" + warpName + "\" not found.").withStyle(ChatFormatting.RED)), Util.NIL_UUID);
                return 0; // Failure
            }
        } catch (CommandSyntaxException e) {
            command.getSource().sendFailure(new TextComponent("An error occurred while executing the /warp command.").withStyle(ChatFormatting.RED));
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
        if(!warpData.containsKey("dimension")) {
            player.sendMessage(new TextComponent("Please set this Warp again."), Util.NIL_UUID);
        }
        ResourceKey<Level> dimension = ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation((String) warpData.get("dimension")));
        if(player.getServer() == null)
            return false; // Server not found
        ServerLevel targetLevel = player.getServer().getLevel(dimension);
        if (targetLevel != null)
            player.teleportTo(targetLevel, (double) x, (double) y, (double) z, 0, 0);
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