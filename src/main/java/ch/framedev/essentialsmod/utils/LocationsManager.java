package ch.framedev.essentialsmod.utils;



/*
 * ch.framedev.essentialsmod.commands
 * =============================================
 * This File was Created by FrameDev
 * Please do not change anything without my consent!
 * =============================================
 * This Class was created at 13.01.2025 23:18
 */

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LocationsManager {

    public static boolean existsSpawn() {
        Config config = new Config();
        return config.getConfig().containsKey("spawn.dimension") && config.getConfig().containsKey("spawn.x") && config.getConfig().containsKey("spawn.y") && config.getConfig().containsKey("spawn.z");
    }

    public static void setSpawn(@NotNull Location location) {
        // Save the spawn location in the config
        Config config = new Config();
        config.getConfig().set("spawn.dimension", location.getDimension());
        config.getConfig().set("spawn.x", location.getX());
        config.getConfig().set("spawn.y", location.getY());
        config.getConfig().set("spawn.z", location.getZ());
        config.getConfig().save();
    }

    public static @Nullable Location getSpawn() {
        if (!existsSpawn())
            return null;

        Config config = new Config();
        String dimension = config.getConfig().getString("spawn.dimension");
        int x = config.getConfig().getInt("spawn.x");
        int y = config.getConfig().getInt("spawn.y");
        int z = config.getConfig().getInt("spawn.z");

        return new Location(dimension, x, y, z);
    }

    public static boolean existsWarp(@NotNull String warpName) {
        Config config = new Config();
        return config.getConfig().containsKey("warp." + warpName + ".x");
    }

    public static void setWarp(@NotNull String warpName, @NotNull Location location) {
        // Save the warp location in the config
        Config config = new Config();
        config.getConfig().set("warp." + warpName + ".dimension", location.getDimension());
        config.getConfig().set("warp." + warpName + ".x", location.getX());
        config.getConfig().set("warp." + warpName + ".y", location.getY());
        config.getConfig().set("warp." + warpName + ".z", location.getZ());
        config.getConfig().save();
    }

    public static @Nullable Location getWarp(@NotNull String warpName) {
        if (!existsWarp(warpName))
            return null;

        Config config = new Config();
        String dimension = config.getConfig().getString("warp." + warpName + ".dimension");
        int x = config.getConfig().getInt("warp." + warpName + ".x");
        int y = config.getConfig().getInt("warp." + warpName + ".y");
        int z = config.getConfig().getInt("warp." + warpName + ".z");

        return new Location(dimension, x, y, z);
    }

    public static boolean existsHome(@NotNull String playerName, String home) {
        String saveKey = "home." + playerName + ".";
        if (home == null)
            home = "home";

        Config config = new Config();
        return config.getConfig().containsKey(saveKey + home + ".x");
    }

    public static void setHome(@NotNull String playerName, @NotNull Location location, String home) {
        String saveKey = "home." + playerName + ".";
        if (home == null)
            home = "home";

        // Save the home location in the config
        Config config = new Config();
        config.getConfig().set(saveKey + home + ".dimension", location.getDimension());
        config.getConfig().set(saveKey + home + ".x", location.getX());
        config.getConfig().set(saveKey + home + ".y", location.getY());
        config.getConfig().set(saveKey + home + ".z", location.getZ());
        config.getConfig().save();
    }

    public static @Nullable Location getHome(@NotNull String playerName, String home) {
        String saveKey = "home." + playerName + ".";
        if (home == null)
            home = "home";

        if (!existsHome(playerName, home))
            return null;

        Config config = new Config();
        if(!config.containsKey(saveKey + home + ".dimension")) {
            String dimension = config.getConfig().getString(saveKey + home + ".dimension");
            int x = config.getConfig().getInt(saveKey + home + ".x");
            int y = config.getConfig().getInt(saveKey + home + ".y");
            int z = config.getConfig().getInt(saveKey + home + ".z");
            return new Location(dimension, x, y, z);
        } else {
            int x = config.getConfig().getInt(saveKey + home + ".x");
            int y = config.getConfig().getInt(saveKey + home + ".y");
            int z = config.getConfig().getInt(saveKey + home + ".z");
            return new Location(null, x, y,z);
        }
    }

    public static List<String> getWarps() {
        Config config = new Config();
        Map<String, Object> warps = config.getConfig().getMap("warp");
        if (warps == null) return new ArrayList<>();
        return new ArrayList<>(warps.keySet());
    }

    @SuppressWarnings("unchecked")
    public static List<String> getHomes(String playerName, boolean ignoreDefault) {
        Config config = new Config();
        Map<String, Object> defaultConfiguration = config.getConfig().getMap("home");
        if (defaultConfiguration == null)
            return new ArrayList<>();
        if (!defaultConfiguration.containsKey(playerName))
            return new ArrayList<>();
        Map<String, Object> configuration = (Map<String, Object>) defaultConfiguration.get(playerName);
        if (configuration == null) {
            return new ArrayList<>();
        }
        List<String> homes = new ArrayList<>();
        for (String home : configuration.keySet()) {
            if (home != null && !"null".equalsIgnoreCase(String.valueOf(configuration.get(home)))) {
                if (!(ignoreDefault && "home".equalsIgnoreCase(home))) {
                    homes.add(home);
                }
            }
        }
        return homes;
    }
}
