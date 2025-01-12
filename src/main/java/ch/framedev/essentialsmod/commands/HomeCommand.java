package ch.framedev.essentialsmod.commands;

import ch.framedev.essentialsmod.utils.Config;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.player.Player;

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
        if (context.getSource().getEntity() instanceof Player player) {
            String homeName = StringArgumentType.getString(context, "homeName");
            if (teleportToHome(player, homeName)) {
                player.sendMessage(new TextComponent("Teleported to home: " + homeName), Util.NIL_UUID);
            } else {
                player.sendMessage(new TextComponent("Home not found: " + homeName), Util.NIL_UUID);
            }
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int executeDefault(CommandContext<CommandSourceStack> context) {
        if (context.getSource().getEntity() instanceof Player player) {
            if (teleportToHome(player, "home")) {
                player.sendMessage(new TextComponent("Teleported to your default home!"), Util.NIL_UUID);
            } else {
                player.sendMessage(new TextComponent("No default home set!"), Util.NIL_UUID);
            }
        }
        return Command.SINGLE_SUCCESS;
    }

    private static boolean teleportToHome(Player player, String homeName) {
        Config config = new Config();
        String playerKey = "home." + player.getName().getString() + "." + homeName;
        if (!config.getConfig().containsKey(playerKey + ".x")) {
            return false; // Home not found
        }
        int x = config.getConfig().getInt(playerKey + ".x");
        int y = config.getConfig().getInt(playerKey + ".y");
        int z = config.getConfig().getInt(playerKey + ".z");
        player.teleportTo(x, y, z);
        return true; // Successfully teleported
    }

    private static List<String> getAllHomes(Player player) {
        Config config = new Config();
        Map<String, Object> defaultConfiguration = config.getConfig().getMap("home");
        if(defaultConfiguration == null)
            return new ArrayList<>();
        if(!defaultConfiguration.containsKey(player.getName().getString()))
            return new ArrayList<>();
        Map<String, Object> configuration = (Map<String, Object>) defaultConfiguration.get(player.getName().getString());
        if(configuration == null) {
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