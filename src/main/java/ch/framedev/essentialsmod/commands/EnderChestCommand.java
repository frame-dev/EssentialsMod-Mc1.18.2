package ch.framedev.essentialsmod.commands;

import ch.framedev.essentialsmod.utils.ChatUtils;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;

public class EnderChestCommand implements ICommand {

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("enderchest") // Command base
                .requires(source -> source.hasPermission(3)) // Restrict to operators (level 3 or higher)
                .then(Commands.argument("playerName", StringArgumentType.word()) // String argument
                        .suggests(PLAYER_SUGGESTION)
                        .executes(this::executeWithContext)); // Executes when command is provided
    }

    private final SuggestionProvider<CommandSourceStack> PLAYER_SUGGESTION = (context, builder) -> {
        for (ServerPlayer player : context.getSource().getServer().getPlayerList().getPlayers()) {
            builder.suggest(player.getGameProfile().getName());
        }
        return builder.buildFuture();
    };

    private int executeWithContext(CommandContext<CommandSourceStack> context) {
        String playerName = StringArgumentType.getString(context, "playerName");
        CommandSourceStack source = context.getSource();

        if (source.getEntity() instanceof ServerPlayer currentPlayer) {
            MinecraftServer server = source.getServer();
            ServerPlayer targetPlayer = server.getPlayerList().getPlayerByName(playerName);

            if (targetPlayer != null) {
                source.sendSuccess(ChatUtils.getPrefix().append(
                        new TextComponent("Opening Ender Chest from " + targetPlayer.getName().getString())
                               .withStyle(ChatFormatting.YELLOW)), true);
                // Online player
                currentPlayer.openMenu(new SimpleMenuProvider(
                        (id, playerInventory, playerEntity) ->
                                new ChestMenu(MenuType.GENERIC_9x3, id, playerInventory, targetPlayer.getEnderChestInventory(), 3),
                        Component.nullToEmpty(targetPlayer.getName().getString() + "'s Ender Chest")
                ));
            } else {
                currentPlayer.sendMessage(new TextComponent("This Player is not online").withStyle(ChatFormatting.RED), Util.NIL_UUID);
            }
        }

        return 1;
    }
}