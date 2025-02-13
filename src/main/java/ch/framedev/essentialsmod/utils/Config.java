package ch.framedev.essentialsmod.utils;

import ch.framedev.essentialsmod.EssentialsMod;
import ch.framedev.yamlutils.FileConfiguration;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class Config {

    private final FileConfiguration config;

    public Config() {
        // Create the config.yml file if it doesn't exist yet
        if (!EssentialsMod.configFile.exists()) {
            if (!EssentialsMod.configFile.getParentFile().mkdirs())
                EssentialsMod.getLOGGER().warn("Could not create config Directory");
            try {
                if (!EssentialsMod.configFile.createNewFile())
                    EssentialsMod.getLOGGER().warn("Could not create config file.");
            } catch (IOException e) {
                EssentialsMod.getLOGGER().error("Could not create config file.", e);
            }
        }
        // initialize the config.yml file
        this.config = new FileConfiguration(EssentialsMod.configFile);
        this.config.load();
        if (!config.containsKey("back")) {
            this.config.set("back", true);
            this.config.save();
            EssentialsMod.getLOGGER().info("Back enabled by default in config.");
        }
        if(!config.containsKey("useConfigForHomes")) {
            this.config.set("useConfigForHomes", true);
            this.config.save();
            EssentialsMod.getLOGGER().info("useConfigForHomes enabled by default in config.");
        }
    }

    public void set(String key, Object value) {
        config.set(key, value);
    }

    public Object get(String key) {
        return config.get(key);
    }

    public Object getOrDefault(String key, Object defaultValue) {
        if (config.containsKey(key))
            return config.get(key);
        return defaultValue;
    }

    public String getString(String key) {
        return config.getString(key);
    }

    public boolean getBoolean(String key) {
        return config.getBoolean(key);
    }

    public int getInt(String key) {
        return config.getInt(key);
    }

    public double getDouble(String key) {
        return config.getDouble(key);
    }

    public Map<String, Object> getMap(String key) {
        return config.getMap(key);
    }

    public void setMap(String key, Map<String, Object> value) {
        config.set(key, value);
    }

    public FileConfiguration getConfigurationSection(String section) {
        return config.getConfigurationsSection(section);
    }

    public List<String> getKeys(String key) {
        return config.getKeys(key);
    }

    public List<?> getList(String key, List<?> defaultValue) {
        if(config.getData().containsKey(key))
            return (List<?>) config.getData().get(key);
        return defaultValue;
    }

    public boolean containsKey(String key) {
        return config.getData().containsKey(key);
    }

    public boolean containsValue(Object value) {
        return config.getData().containsValue(value);
    }

    public void save() {
        EssentialsMod.getLOGGER().info("Saving configuration");
        config.save();
        EssentialsMod.getLOGGER().info("Configuration saved");
    }

    public Map<String, Object> getWarpConfig() {
        return getConfig().getMap("warp");
    }

    @SuppressWarnings("unchecked")
    public Map<String,Object> getHomeConfig(String playerName) {
        Map<String, Object> defaultConfiguration = getConfig().getMap("home");
        if (defaultConfiguration == null)
            return null;
        if (!defaultConfiguration.containsKey(playerName))
            return null;
        return (Map<String, Object>) defaultConfiguration.get(playerName);
    }

    /**
     * Returns the FileConfiguration instance used for reading and writing configuration data.
     *
     * @return The FileConfiguration instance representing the configuration file.
     *
     * @see FileConfiguration
     */
    public FileConfiguration getConfig() {
        return this.config;
    }
}
