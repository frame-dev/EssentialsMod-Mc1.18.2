package ch.framedev.essentialsmod.commands;

import com.mojang.brigadier.arguments.ArgumentType;

public record CommandParameter<T>(String name, ArgumentType<T> type) {
}