package ch.framedev.essentialsmod;

import ch.framedev.essentialsmod.commands.*;
import ch.framedev.essentialsmod.events.BackEvent;
import ch.framedev.essentialsmod.events.ChatEventHandler;
import ch.framedev.essentialsmod.events.InventorySyncHandler;
import ch.framedev.essentialsmod.events.PlayerJoinEvent;
import ch.framedev.essentialsmod.utils.Config;
import com.mojang.logging.LogUtils;
import net.minecraft.commands.Commands;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.loading.FileUtils;
import org.slf4j.Logger;

import java.io.File;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.stream.Collectors;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("essentials")
public class EssentialsMod {
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();
    public static File configFile;

    public EssentialsMod() {
        // Register the setup method for mod loading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        // Register the enqueueIMC method for mod loading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::enqueueIMC);
        // Register the processIMC method for mod loading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::processIMC);
        MinecraftForge.EVENT_BUS.addListener(this::onRegisterCommands);

        MinecraftForge.EVENT_BUS.register(new PlayerJoinEvent());
        MinecraftForge.EVENT_BUS.register(new InventorySyncHandler());
        MinecraftForge.EVENT_BUS.register(new BackEvent());
        MinecraftForge.EVENT_BUS.register(new ChatEventHandler());

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);

        Path path = FileUtils.getOrCreateDirectory(FMLPaths.CONFIGDIR.get().resolve("essentials"), "essentials");
        configFile = new File(path.toFile(), "config.yml");
        Config config = new Config();
        if(config.getConfig().getData().containsKey("muted")) {
            MuteCommand.mutedPlayers = new HashSet<>(config.getConfig().getStringList("muted"));
        }
    }

    private void setup(final FMLCommonSetupEvent event) {
        // some pre init code
        LOGGER.info("HELLO FROM PREINIT");
        LOGGER.info("DIRT BLOCK >> {}", Blocks.DIRT.getRegistryName());
    }

    private void enqueueIMC(final InterModEnqueueEvent event) {
        // Some example code to dispatch IMC to another mod
        InterModComms.sendTo("examplemod", "helloworld", () -> {
            LOGGER.info("Hello world from the MDK");
            return "Hello world";
        });
    }

    private void processIMC(final InterModProcessEvent event) {
        // Some example code to receive and process InterModComms from other mods
        LOGGER.info("Got IMC {}", event.getIMCStream().
                map(m -> m.messageSupplier().get()).
                collect(Collectors.toList()));
    }


    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
        LOGGER.info("HELLO from server starting");
    }

    // You can use EventBusSubscriber to automatically subscribe events on the contained class (this is subscribing to the MOD
    // Event bus for receiving Registry Events)
    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistryEvents {
        @SubscribeEvent
        public static void onBlocksRegistry(final RegistryEvent.Register<Block> blockRegistryEvent) {
            // Register a new block here
            LOGGER.info("HELLO from Register Block");
        }
    }

    private void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(SetHomeCommand.register());
        event.getDispatcher().register(HomeCommand.register());
        event.getDispatcher().register(DeleteHomeCommand.register());
        event.getDispatcher().register(InvseeCommand.register());
        event.getDispatcher().register(EnderChestCommand.register());
        event.getDispatcher().register(TpaCommand.register());
        event.getDispatcher().register(SpawnCommand.register());
        event.getDispatcher().register(SetSpawnCommand.register());
        event.getDispatcher().register(VanishCommand.register());
        event.getDispatcher().register(NewGameModeCommand.register());
        event.getDispatcher().register(AdminSwordCommand.register());
        event.getDispatcher().register(RepairCommand.register());
        event.getDispatcher().register(FlyCommand.register());
        event.getDispatcher().register(HealCommand.register());
        event.getDispatcher().register(FeedCommand.register());
        event.getDispatcher().register(BackCommand.register());
        event.getDispatcher().register(MuteCommand.register());
        event.getDispatcher().register(WarpCommand.register());
        event.getDispatcher().register(DeleteWarpCommand.register());
        event.getDispatcher().register(SetWarpCommand.register());


        event.getDispatcher().register(
                Commands.literal("day")
                        .requires(source -> source.hasPermission(2)) // Restrict to operators (level 2 or higher)
                        .executes(context -> DayNightCommand.setDay(context.getSource()))
        );

        event.getDispatcher().register(
                Commands.literal("night")
                        .requires(source -> source.hasPermission(2)) // Restrict to operators (level 2 or higher)
                        .executes(context -> DayNightCommand.setNight(context.getSource()))
        );
    }

}
