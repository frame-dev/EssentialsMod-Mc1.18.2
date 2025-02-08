package ch.framedev.essentialsmod.commands;

import ch.framedev.essentialsmod.utils.Config;
import ch.framedev.essentialsmod.utils.ChatUtils;
import ch.framedev.essentialsmod.utils.Location;
import ch.framedev.essentialsmod.utils.LocationsManager;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HomeCommand implements ICommand {

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("home")
                .then(Commands.argument("homeName", StringArgumentType.word())
                        .suggests(HOME_SUGGESTION)
                        .executes(this::executeWithHome)) // Executes with a specific home name
                .executes(this::executeDefault); // Executes with no arguments
    }

    private int executeWithHome(CommandContext<CommandSourceStack> context) {
        if (context.getSource().getEntity() instanceof ServerPlayer player) {
            String homeName = StringArgumentType.getString(context, "homeName");
            if (teleportToHome(player, homeName)) {
                TextComponent textComponent = ChatUtils.getTextComponent(new String[]{"Teleported to home:", homeName},
                        new String[]{"§a", "§b"});
                player.sendMessage(ChatUtils.getPrefix().append(textComponent), Util.NIL_UUID);
            } else {
                player.sendMessage(new TextComponent("Home not found: " + homeName).withStyle(ChatFormatting.RED), Util.NIL_UUID);
            }
        }
        return Command.SINGLE_SUCCESS;
    }

    private int executeDefault(CommandContext<CommandSourceStack> context) {
        if (context.getSource().getEntity() instanceof ServerPlayer player) {
            if (teleportToHome(player, "home")) {
                TextComponent textComponent = ChatUtils.getTextComponent(new String[]{"Teleported to your", "default", "home"},
                        new String[]{"§a", "§b", "§a"});
                player.sendMessage(ChatUtils.getPrefix().append(textComponent), Util.NIL_UUID);
            } else {
                player.sendMessage(new TextComponent("No default home set!").withStyle(ChatFormatting.RED), Util.NIL_UUID);
            }
        }
        return Command.SINGLE_SUCCESS;
    }

    private boolean teleportToHome(ServerPlayer player, String homeName) {
        if (!LocationsManager.existsHome(player.getName().getString(), homeName)) {
            return false; // Home doesn't exist
        }
        Location location = LocationsManager.getHome(player.getName().getString(), homeName);
        if (location == null) {
            return false; // Home not found
        }
        if (location.getDimension() != null)
            player.teleportTo(location.getServerLevel(player), location.getX(), location.getY(), location.getZ(), 0, 0);
        else
            player.teleportTo(location.getX(), location.getY(), location.getZ());
        return true;
    }

    private List<String> getAllHomes(Player player) {
        Config config = new Config();
        Map<String, Object> defaultConfiguration = config.getConfig().getMap("home");
        if (defaultConfiguration == null)
            return new ArrayList<>();
        if (!defaultConfiguration.containsKey(player.getName().getString()))
            return new ArrayList<>();
        @SuppressWarnings("unchecked") Map<String, Object> configuration = (Map<String, Object>) defaultConfiguration.get(player.getName().getString());
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

    private final SuggestionProvider<CommandSourceStack> HOME_SUGGESTION = (context, builder) -> {
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