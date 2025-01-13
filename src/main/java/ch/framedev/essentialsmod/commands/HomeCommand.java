package ch.framedev.essentialsmod.commands;

import ch.framedev.essentialsmod.utils.Config;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HomeCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("home")
                .then(Commands.argument("homeName", StringArgumentType.word())
                        .suggests(HOME_SUGGESTION)
                        .executes(HomeCommand::executeWithHome)) // Executes with a specific home name
                .executes(HomeCommand::executeDefault); // Executes with no arguments
    }

    private static int executeWithHome(CommandContext<CommandSourceStack> context) {
        if (context.getSource().getEntity() instanceof ServerPlayer player) {
            String homeName = StringArgumentType.getString(context, "homeName");
            if (teleportToHome(player, homeName)) {
                player.sendMessage(new TextComponent("Teleported to home: " + homeName), Util.NIL_UUID);
            } else {
                player.sendMessage(new TextComponent("Home not found: " + homeName).withStyle(ChatFormatting.RED), Util.NIL_UUID);
            }
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int executeDefault(CommandContext<CommandSourceStack> context) {
        if (context.getSource().getEntity() instanceof ServerPlayer player) {
            if (teleportToHome(player, "home")) {
                player.sendMessage(new TextComponent("Teleported to your default home!"), Util.NIL_UUID);
            } else {
                player.sendMessage(new TextComponent("No default home set!").withStyle(ChatFormatting.RED), Util.NIL_UUID);
            }
        }
        return Command.SINGLE_SUCCESS;
    }

    private static boolean teleportToHome(ServerPlayer player, String homeName) {
        Config config = new Config();
        String playerKey = "home." + player.getName().getString() + "." + homeName;
        if (!config.getConfig().containsKey(playerKey + ".x")) {
            return false; // Home not found
        }
        if (!config.getConfig().containsKey(playerKey + ".dimension")) {
            player.sendMessage(new TextComponent("Please set this Home again."), Util.NIL_UUID);
            int x = config.getConfig().getInt(playerKey + ".x");
            int y = config.getConfig().getInt(playerKey + ".y");
            int z = config.getConfig().getInt(playerKey + ".z");
            player.teleportTo(x, y, z);
            player.sendMessage(new TextComponent("The Home you set is Invalid. Teleporting to Coordinates in the Overworld!"), Util.NIL_UUID);
            return true; // Successfully teleported
        }
        String dimensionName = config.getConfig().getString(playerKey + ".dimension");
        int x = config.getConfig().getInt(playerKey + ".x");
        int y = config.getConfig().getInt(playerKey + ".y");
        int z = config.getConfig().getInt(playerKey + ".z");
        ResourceKey<Level> dimension = ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(dimensionName));
        if (player.getServer() == null) {
            return false; // Server not found
        }
        ServerLevel targetLevel = player.getServer().getLevel(dimension);
        if (targetLevel != null) {
            player.teleportTo(targetLevel, x, y, z, 0, 0);
            return true; // Successfully teleported
        }
        return false;
    }

    private static List<String> getAllHomes(Player player) {
        Config config = new Config();
        Map<String, Object> defaultConfiguration = config.getConfig().getMap("home");
        if (defaultConfiguration == null)
            return new ArrayList<>();
        if (!defaultConfiguration.containsKey(player.getName().getString()))
            return new ArrayList<>();
        Map<String, Object> configuration = (Map<String, Object>) defaultConfiguration.get(player.getName().getString());
        if (configuration == null) {
            return new ArrayList<>();
        }
        List<String> homes = new ArrayList<>();
        for (String home : configuration.keySet()) {
            if (home != null && !"null".equalsIgnoreCase(String.valueOf(configuration.get(home)))) {
                homes.add(home);
            }
        }
        return homes;
    }

    private static final SuggestionProvider<CommandSourceStack> HOME_SUGGESTION = (context, builder) -> {
        if (context.getSource().getEntity() instanceof Player player) {
            List<String> homes = getAllHomes(player);
            if (homes.isEmpty()) {
                builder.suggest("No homes available");
            } else {
                for (String home : homes) {
                    builder.suggest(home);
                }
            }
        }
        return builder.buildFuture();
    };
}