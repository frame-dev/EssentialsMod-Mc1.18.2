package ch.framedev.essentialsmod;

import ch.framedev.essentialsmod.commands.*;
import ch.framedev.essentialsmod.events.*;
import ch.framedev.essentialsmod.utils.Config;
import ch.framedev.essentialsmod.utils.EssentialsConfig;
import com.mojang.logging.LogUtils;
import net.minecraft.commands.Commands;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.loading.FileUtils;
import org.slf4j.Logger;

import java.io.File;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static ch.framedev.essentialsmod.commands.TempBanCommand.tempBanList;

// The value here should match an entry in the META-INF/mods.toml file

@SuppressWarnings({"InstantiationOfUtilityClass", "unchecked"})
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

        // Register Commands
        MinecraftForge.EVENT_BUS.addListener(this::onRegisterCommands);

        // Register Events
        MinecraftForge.EVENT_BUS.register(new PlayerJoinEvent());
        MinecraftForge.EVENT_BUS.register(new InventorySyncHandler());
        MinecraftForge.EVENT_BUS.register(new BackEventHandler());
        MinecraftForge.EVENT_BUS.register(new ChatEventHandler());
        MinecraftForge.EVENT_BUS.register(new MuteOtherPlayerCommand.ChatEventHandler());
        MinecraftForge.EVENT_BUS.register(new BackpackCommand.BackpackEventHandler());
        MinecraftForge.EVENT_BUS.register(new TempBanCommand.BanListener());

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);

        // Set up the path to the custom directory inside the config folder
        Path essentialsConfigDir = FMLPaths.CONFIGDIR.get().resolve("essentials");

        // Ensure the directory exists
        FileUtils.getOrCreateDirectory(essentialsConfigDir, "essentials");

        // Define the full path for the essentials-common.toml file
        Path configPath = essentialsConfigDir.resolve("essentials-common.toml");
        configFile = new File(essentialsConfigDir.toFile(), "config.yml");

        // Register the config with Forge
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, EssentialsConfig.COMMON_CONFIG, "essentials/essentials-common.toml");

        // Load Config essentials-common.toml
        EssentialsConfig.loadConfig(EssentialsConfig.COMMON_CONFIG, configPath);

        // Create config.yml and update muted List in MuteCommand
        Config config = new Config();
        if (config.getConfig().getData().containsKey("muted")) {
            MuteCommand.mutedPlayers = new HashSet<>(config.getConfig().getStringList("muted"));
        }
        if (config.getConfig().getData().containsKey("tempBan")) {
            Map<String, Object> defaultConfiguration = (Map<String, Object>) config.getConfig().getData().get("tempBan");
            for (String playerName : defaultConfiguration.keySet()) {
                Map<String, Object> data = (Map<String, Object>) defaultConfiguration.get(playerName);
                TempBanCommand.BanDetails banDetails = TempBanCommand.BanDetails.fromMap(data);
                tempBanList.put(UUID.fromString(playerName), banDetails);
            }
        }

        // Register the Config GUI
        if (FMLEnvironment.dist == Dist.CLIENT) {
            EssentialsConfigScreen.EssentialsModClient.registerConfigGui();
        }
    }

    private void setup(final FMLCommonSetupEvent event) {
        // some pre-init code
        LOGGER.info("HELLO FROM PRE INIT");
    }

    private void enqueueIMC(final InterModEnqueueEvent event) {
        // Some example code to dispatch IMC to another mod
        InterModComms.sendTo("essentials", "helloworld", () -> {
            LOGGER.info("Hello world from the MDK");
            return "Hello world";
        });
    }

    private void processIMC(final InterModProcessEvent event) {
        // Some example code to receive and process InterModComms from other mods
        LOGGER.info("Got IMC {}", event.getIMCStream().map(m -> m.messageSupplier().get()).collect(Collectors.toList()));
    }


    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        EssentialsMod.getLOGGER().info("onServerStarting");
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

        event.getDispatcher().register(TpaHereCommand.register());
        event.getDispatcher().register(TpaCommand.register());

        event.getDispatcher().register(SpawnCommand.register());
        event.getDispatcher().register(SetSpawnCommand.register());

        event.getDispatcher().register(RepairCommand.register());

        event.getDispatcher().register(FlyCommand.register());

        event.getDispatcher().register(HealCommand.register());
        event.getDispatcher().register(FeedCommand.register());
        if (EssentialsConfig.useBack.get()) event.getDispatcher().register(BackCommand.register());

        if (EssentialsConfig.enableWarps.get()) {
            event.getDispatcher().register(WarpCommand.register());
            event.getDispatcher().register(DeleteWarpCommand.register());
            event.getDispatcher().register(SetWarpCommand.register());
        }

        Set<ICommand> commandSet = new HashSet<>(
                Set.of(new GodCommand(),
                        new MuteOtherPlayerCommand(),
                        new VanishCommand(),
                        new AdminSwordCommand(),
                        new NewGameModeCommand(),
                        new MuteCommand(),
                        new TempBanCommand()
                ));

        if (EssentialsConfig.enableBackPack.get()) commandSet.add(new BackpackCommand());

        commandSet.forEach(command -> event.getDispatcher().register(command.register()));


        event.getDispatcher().register(Commands.literal("day").requires(source -> source.hasPermission(2)) // Restrict to operators (level 2 or higher)
                .executes(context -> DayNightCommand.setDay(context.getSource())));

        event.getDispatcher().register(Commands.literal("night").requires(source -> source.hasPermission(2)) // Restrict to operators (level 2 or higher)
                .executes(context -> DayNightCommand.setNight(context.getSource())));
    }

    /**
     * This Method returns the Logger instance that will be used to log
     *
     * @return return the Logger instance
     */
    public static Logger getLOGGER() {
        return LOGGER;
    }

}
