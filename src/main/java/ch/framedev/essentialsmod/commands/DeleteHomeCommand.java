package ch.framedev.essentialsmod.commands;

import ch.framedev.essentialsmod.utils.ChatUtils;
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
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DeleteHomeCommand implements ICommand {


    @Override
    public LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("delhome")
                .then(Commands.argument("homeName", StringArgumentType.word())
                        .suggests(HOME_SUGGESTION)
                        .executes(this::executeWithHome)) // Executes with a specific home name
                .executes(this::executeDefault); // Executes with no arguments
    }

    private int executeWithHome(CommandContext<CommandSourceStack> context) {
        if (context.getSource().getEntity() instanceof Player player) {
            String homeName = StringArgumentType.getString(context, "homeName");
            if (deleteHome(player, homeName)) {
                TextComponent textComponent = ChatUtils.getTextComponent(new String[]{"Home:",homeName,"Deleted!"},
                        new String[]{"§a","§b","§a"});
                player.sendMessage(ChatUtils.getPrefix().append(textComponent), Util.NIL_UUID);
            } else {
                player.sendMessage(new TextComponent("Home not found: " + homeName).withStyle(ChatFormatting.RED), Util.NIL_UUID);
            }
        }
        return Command.SINGLE_SUCCESS;
    }

    private int executeDefault(CommandContext<CommandSourceStack> context) {
        if (context.getSource().getEntity() instanceof Player player) {
            if (deleteHome(player, "home")) {
                TextComponent textComponent = ChatUtils.getTextComponent(new String[]{"Default", "home Deleted!"},
                        new String[]{"§b","§a"});
                player.sendMessage(ChatUtils.getPrefix().append(textComponent), Util.NIL_UUID);
            } else {
                player.sendMessage(new TextComponent("No default home set!").withStyle(ChatFormatting.RED), Util.NIL_UUID);
            }
        }
        return Command.SINGLE_SUCCESS;
    }

    private boolean deleteHome(Player player, String homeName) {
        Config config = new Config();
        String playerKey = "home." + player.getName().getString() + "." + homeName;
        if (!config.getConfig().containsKey(playerKey + ".x")) {
            return false; // Home not found
        }
        Map<String, Object> homeMap = config.getConfig().getMap("home." + player.getName().getString());
        if(homeMap != null) {
            homeMap.remove(homeName);
        }
        config.getConfig().set("home." + player.getName().getString(), homeMap);
        config.getConfig().save();
        return true; // Successfully teleported
    }

    private List<String> getAllHomes(Player player) {
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