package ch.framedev.essentialsmod.utils;



/*
 * ch.framedev.essentialsmod.utils
 * =============================================
 * This File was Created by FrameDev
 * Please do not change anything without my consent!
 * =============================================
 * This Class was created at 06.02.2025 12:21
 */

import ch.framedev.essentialsmod.commands.*;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.Commands;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

@Mod.EventBusSubscriber(modid = "essentials")
public class RegisterManager {

    /**
     * Registers commands for the Essentials mod based on configuration settings.
     * This method is called when Minecraft is registering commands and sets up various
     * custom commands such as backpack, back, warps, and day/night controls.
     *
     * @param event The RegisterCommandsEvent that provides access to the command dispatcher
     *              for registering new commands.
     */
    public void onRegisterCommands(RegisterCommandsEvent event) {
        Set<ICommand> commandSet = getICommandsSet();

        // Enable or disable the backpack command
        if (EssentialsConfig.enableBackPack.get()) commandSet.add(new BackpackCommand());

        // Enable or disable the /back command
        if (EssentialsConfig.useBack.get()) commandSet.add(new BackCommand());

        // Enable or disable the /warp /setwarp /delwarp command
        if (EssentialsConfig.enableWarps.get()) {
            commandSet.add(new WarpCommand());
            commandSet.add(new DeleteWarpCommand());
            commandSet.add(new SetWarpCommand());
        }


        commandSet.forEach(command -> event.getDispatcher().register(command.register()));


        // Register day/night commands
        event.getDispatcher().register(Commands.literal("day").requires(source -> source.hasPermission(2)) // Restrict to operators (level 2 or higher)
                .executes(context -> DayNightCommand.setDay(context.getSource())));

        event.getDispatcher().register(Commands.literal("night").requires(source -> source.hasPermission(2)) // Restrict to operators (level 2 or higher)
                .executes(context -> DayNightCommand.setNight(context.getSource())));

        event.getDispatcher().register(Commands.literal("setticks").requires(source -> source.hasPermission(2))
                .then(Commands.argument("ticks", IntegerArgumentType.integer())
                        .executes(DayNightCommand::setTicks)));
    }

    /**
     * Creates and returns a set of ICommand objects representing various game commands.
     * This method initializes a set of command instances for different functionalities
     * such as teleportation, spawn management, home management, player interactions,
     * and administrative controls.
     *
     * @return A non-null Set of ICommand objects containing instances of various game commands.
     *         The set includes commands for teleportation (TPA), spawn management,
     *         home management, player inventory viewing, god mode, muting, vanishing,
     *         game mode changes, temporary bans, server maintenance, player healing,
     *         item repair, and flight mode.
     */
    private static @NotNull Set<ICommand> getICommandsSet() {
        return new HashSet<>(
                Set.of(
                        new TpaCommand(),
                        new TpaHereCommand(),

                        new SpawnCommand(),
                        new SetSpawnCommand(),

                        new SetHomeCommand(),
                        new HomeCommand(),
                        new DeleteHomeCommand(),

                        new InvseeCommand(),
                        new EnderChestCommand(),
                        new GodCommand(),
                        new MuteOtherPlayerCommand(),
                        new VanishCommand(),
                        new AdminSwordCommand(),
                        new NewGameModeCommand(),
                        new MuteCommand(),
                        new TempBanCommand(),
                        new MaintenanceCommand(),
                        new FeedCommand(),
                        new HealCommand(),
                        new RepairCommand(),
                        new FlyCommand()
                ));
    }
}
