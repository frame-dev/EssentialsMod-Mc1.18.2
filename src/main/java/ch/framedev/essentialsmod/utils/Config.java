package ch.framedev.essentialsmod.utils;

import ch.framedev.essentialsmod.EssentialsMod;
import ch.framedev.yamlutils.FileConfiguration;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class Config {

    private final FileConfiguration config;

    public Config() {
        if (!EssentialsMod.configFile.exists()) {
            if (!EssentialsMod.configFile.getParentFile().mkdirs())
                throw new RuntimeException("Failed to create directory for config.");
            try {
                if (!EssentialsMod.configFile.createNewFile())
                    throw new RuntimeException("Failed to create config file.");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        this.config = new FileConfiguration(EssentialsMod.configFile);
        this.config.load();
        if (!config.containsKey("back")) {
            this.config.set("back", true);
            this.config.save();
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

    public boolean containsKey(String key) {
        return config.containsKey(key);
    }

    public boolean containsValue(Object value) {
        return config.containsValue(value);
    }

    public void save() {
        config.save();
    }

    public FileConfiguration getConfig() {
        return config;
    }
}
