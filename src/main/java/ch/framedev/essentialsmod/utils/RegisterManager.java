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

import java.util.HashSet;
import java.util.Set;

@Mod.EventBusSubscriber(modid = "essentials") // Replace with your mod's ID
public class RegisterManager {

    public void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(SetHomeCommand.register());
        event.getDispatcher().register(HomeCommand.register());
        event.getDispatcher().register(DeleteHomeCommand.register());

        event.getDispatcher().register(InvseeCommand.register());
        event.getDispatcher().register(EnderChestCommand.register());

        event.getDispatcher().register(TpaHereCommand.register());
        event.getDispatcher().register(TpaCommand.register());

        event.getDispatcher().register(SpawnCommand.register());
        event.getDispatcher().register(SetSpawnCommand.register());

        event.getDispatcher().register(RepairCommand.register());

        event.getDispatcher().register(FlyCommand.register());

        if (EssentialsConfig.enableWarps.get()) {
            event.getDispatcher().register(WarpCommand.register());
            event.getDispatcher().register(DeleteWarpCommand.register());
            event.getDispatcher().register(SetWarpCommand.register());
        }

        Set<ICommand> commandSet = new HashSet<>(
                Set.of(
                        new GodCommand(),
                        new MuteOtherPlayerCommand(),
                        new VanishCommand(),
                        new AdminSwordCommand(),
                        new NewGameModeCommand(),
                        new MuteCommand(),
                        new TempBanCommand(),
                        new MaintenanceCommand(),
                        new FeedCommand(),
                        new HealCommand()
                ));

        if (EssentialsConfig.enableBackPack.get()) commandSet.add(new BackpackCommand());
        if (EssentialsConfig.useBack.get()) commandSet.add(new BackCommand());

        commandSet.forEach(command -> event.getDispatcher().register(command.register()));


        event.getDispatcher().register(Commands.literal("day").requires(source -> source.hasPermission(2)) // Restrict to operators (level 2 or higher)
                .executes(context -> DayNightCommand.setDay(context.getSource())));

        event.getDispatcher().register(Commands.literal("night").requires(source -> source.hasPermission(2)) // Restrict to operators (level 2 or higher)
                .executes(context -> DayNightCommand.setNight(context.getSource())));
    }
}
