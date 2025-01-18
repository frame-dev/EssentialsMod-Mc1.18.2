package ch.framedev.essentialsmod.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public abstract class AbstractCommand implements ICommand {

    private final String commandName;
    private final int permission;

    public AbstractCommand(String commandName, int permission) {
        this.commandName = commandName;
        this.permission = permission;
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal(commandName)
                .requires(source -> source.hasPermission(permission)) // Restrict to operators or permission level 2+
                .executes(this::execute);
    }

    public abstract int execute(CommandContext<CommandSourceStack> command);
}
