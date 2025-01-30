package ch.framedev.essentialsmod.commands;

import com.mojang.brigadier.builder.*;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

import java.util.List;

public abstract class AbstractCommand implements ICommand {

    private final String commandName;
    private final int permission;
    private final List<CommandParameter<?>> parameters;

    public AbstractCommand(String commandName, int permission, List<CommandParameter<?>> parameters) {
        this.commandName = commandName;
        this.permission = permission;
        this.parameters = parameters;
    }

    public AbstractCommand(String commandName, int permission) {
        this(commandName, permission, List.of());
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> register() {
        LiteralArgumentBuilder<CommandSourceStack> commandBuilder = Commands.literal(commandName).requires(source -> source.hasPermission(permission)).executes(this::execute);

        if (!parameters.isEmpty()) {
            addArguments(commandBuilder, parameters, 0);
        }

        return commandBuilder;
    }

    private void addArguments(ArgumentBuilder<CommandSourceStack, ?> commandBuilder, List<CommandParameter<?>> params, int index) {
        if (index >= params.size()) {
            return;
        }

        CommandParameter<?> param = params.get(index);
        RequiredArgumentBuilder<CommandSourceStack, ?> argument = Commands.argument(param.name(), param.type());

        argument.executes(context -> executeWithParams(context, index + 1));

        commandBuilder.then(argument);
        addArguments(argument, params, index + 1);
    }

    public abstract int execute(CommandContext<CommandSourceStack> context);

    public int executeWithParams(CommandContext<CommandSourceStack> context, int paramCount) {
        return 0;
    }

    public String getCommandName() {
        return commandName;
    }

    public int getPermissionLevel() {
        return permission;
    }

    public List<CommandParameter<?>> getParameters() {
        return parameters;
    }
}