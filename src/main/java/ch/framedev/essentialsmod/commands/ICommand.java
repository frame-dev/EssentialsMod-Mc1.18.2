package ch.framedev.essentialsmod.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;

public interface ICommand {

    LiteralArgumentBuilder<CommandSourceStack> register();
}
