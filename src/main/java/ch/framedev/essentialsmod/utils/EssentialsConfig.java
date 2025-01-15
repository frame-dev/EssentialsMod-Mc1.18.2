package ch.framedev.essentialsmod.utils;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;

import java.nio.file.Path;

@Mod.EventBusSubscriber
public class EssentialsConfig {
    public static final ForgeConfigSpec COMMON_CONFIG;
    public static final ForgeConfigSpec.BooleanValue enableWarps;
    public static final ForgeConfigSpec.BooleanValue useBack;
    public static final ForgeConfigSpec.BooleanValue enableLimitedHomes;
    public static final ForgeConfigSpec.IntValue limitForHomes;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.comment("Essentials Mod Configuration").push("general");

        useBack = builder
                .comment("Enable the /back Command.")
                .define("enableBack", true);

        enableWarps = builder
                .comment("Enable or disable warp functionality.")
                .define("enableWarps", true);

        enableLimitedHomes = builder
                .comment("Enable or disable limited home functionality.")
                .define("enableLimitedHomes", false);

        limitForHomes = builder
                .comment("Limit the number of homes per player. Default Home will be ignored.")
               .defineInRange("limitForHomes", 5, 1, Integer.MAX_VALUE);

        builder.pop();

        COMMON_CONFIG = builder.build();
    }

    public static void register(ModLoadingContext context) {
        context.registerConfig(ModConfig.Type.COMMON, COMMON_CONFIG);
    }

    /**
     * Loads the configuration file into the ForgeConfigSpec.
     *
     * @param configSpec The ForgeConfigSpec to set the config data.
     * @param path       The path to the configuration file.
     */
    public static void loadConfig(ForgeConfigSpec configSpec, Path path) {
        final CommentedFileConfig commentedConfig = CommentedFileConfig.builder(path).sync().autosave().build();
        commentedConfig.load();
        configSpec.setConfig(commentedConfig);
    }
}