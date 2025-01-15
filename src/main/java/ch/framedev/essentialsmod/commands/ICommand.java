package ch.framedev.essentialsmod.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;

public interface ICommand {

    /**
     * Create a new Command and Register it in the onRegisterCommands(RegisterCommandsEvent event) method
     * Returns the command
     * @return the command
     */
    LiteralArgumentBuilder<CommandSourceStack> register();
}
