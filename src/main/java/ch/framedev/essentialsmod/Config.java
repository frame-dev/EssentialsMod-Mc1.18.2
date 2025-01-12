package ch.framedev.essentialsmod;

import ch.framedev.yamlutils.FileConfiguration;

import java.io.IOException;

public class Config {

    private FileConfiguration config;

    public Config() {
        if(!EssentialsMod.configFile.exists()) {
            EssentialsMod.configFile.getParentFile().mkdirs();
            try {
                EssentialsMod.configFile.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        this.config = new FileConfiguration(EssentialsMod.configFile);
        this.config.load();
        if(!config.containsKey("back")) {
            this.config.set("back", true);
            this.config.save();
        }
    }

    public FileConfiguration getConfig() {
        return config;
    }
}
