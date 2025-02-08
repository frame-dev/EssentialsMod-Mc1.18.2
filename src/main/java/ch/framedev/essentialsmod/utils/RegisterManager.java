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
import net.minecraft.commands.Commands;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

@Mod.EventBusSubscriber(modid = "essentials") // Replace with your mod's ID
public class RegisterManager {

    public void onRegisterCommands(RegisterCommandsEvent event) {
        Set<ICommand> commandSet = getICommandsSet();

        if (EssentialsConfig.enableBackPack.get()) commandSet.add(new BackpackCommand());
        if (EssentialsConfig.useBack.get()) commandSet.add(new BackCommand());

        if (EssentialsConfig.enableWarps.get()) {
            commandSet.add(new WarpCommand());
            commandSet.add(new DeleteWarpCommand());
            commandSet.add(new SetWarpCommand());
        }


        commandSet.forEach(command -> event.getDispatcher().register(command.register()));


        event.getDispatcher().register(Commands.literal("day").requires(source -> source.hasPermission(2)) // Restrict to operators (level 2 or higher)
                .executes(context -> DayNightCommand.setDay(context.getSource())));

        event.getDispatcher().register(Commands.literal("night").requires(source -> source.hasPermission(2)) // Restrict to operators (level 2 or higher)
                .executes(context -> DayNightCommand.setNight(context.getSource())));
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
