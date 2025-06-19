package io.mewb.customcompasstracker.manager;

import io.mewb.customcompasstracker.CustomCompassTracker;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Manages the loading and saving of all persistent plugin data,
 * specifically player target locations, to a YAML file.
 */
public class DataManager {

    private final CustomCompassTracker plugin;
    private FileConfiguration dataConfig = null;
    private File configFile = null;

    public DataManager(CustomCompassTracker plugin) {
        this.plugin = plugin;
        saveDefaultConfig();
    }

    /**
     * Reloads the data configuration from the disk.
     */
    public void reloadConfig() {
        if (this.configFile == null) {
            this.configFile = new File(this.plugin.getDataFolder(), "playerdata.yml");
        }
        this.dataConfig = YamlConfiguration.loadConfiguration(this.configFile);
    }

    /**
     * Retrieves the data configuration, reloading it if it's null.
     * @return The FileConfiguration for playerdata.yml.
     */
    public FileConfiguration getConfig() {
        if (this.dataConfig == null) {
            reloadConfig();
        }
        return this.dataConfig;
    }

    /**
     * Saves the data configuration to the disk.
     */
    public void saveConfig() {
        if (this.dataConfig == null || this.configFile == null) {
            return;
        }
        try {
            this.getConfig().save(this.configFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save config to " + this.configFile, e);
        }
    }

    /**
     * Saves the default configuration file from the JAR if it doesn't exist.
     */
    public void saveDefaultConfig() {
        if (this.configFile == null) {
            this.configFile = new File(this.plugin.getDataFolder(), "playerdata.yml");
        }
        if (!this.configFile.exists()) {
            this.plugin.saveResource("playerdata.yml", false);
        }
    }

    /**
     * Loads all targets for all players from the playerdata.yml file.
     * This is typically called once on plugin startup.
     * @return A map containing all player UUIDs and their corresponding targets.
     */
    public Map<UUID, Map<String, Location>> loadAllTargets() {
        Map<UUID, Map<String, Location>> allTargets = new HashMap<>();
        ConfigurationSection playersSection = getConfig().getConfigurationSection("players");

        if (playersSection == null) {
            return allTargets;
        }

        for (String uuidString : playersSection.getKeys(false)) {
            UUID playerUUID;
            try {
                playerUUID = UUID.fromString(uuidString);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Found invalid UUID in playerdata.yml: " + uuidString);
                continue;
            }


            ConfigurationSection targetsSection = playersSection.getConfigurationSection(uuidString + ".targets");
            if (targetsSection == null) {
                continue;
            }

            Map<String, Location> playerSpecificTargets = new HashMap<>();
            for (String targetName : targetsSection.getKeys(false)) {
                Location targetLocation = targetsSection.getLocation(targetName);
                if (targetLocation != null) {
                    playerSpecificTargets.put(targetName, targetLocation);
                }
            }
            allTargets.put(playerUUID, playerSpecificTargets);
        }
        return allTargets;
    }

    /**
     * Saves all targets for all players to the playerdata.yml file.
     * This is typically called once on plugin shutdown.
     * @param allTargets The map containing all player targets to save.
     */
    public void saveAllTargets(Map<UUID, Map<String, Location>> allTargets) {
        getConfig().set("players", null);

        for (Map.Entry<UUID, Map<String, Location>> entry : allTargets.entrySet()) {
            String uuidString = entry.getKey().toString();
            if (entry.getValue().isEmpty()) {
                continue;
            }

            for (Map.Entry<String, Location> targetEntry : entry.getValue().entrySet()) {
                getConfig().set("players." + uuidString + ".targets." + targetEntry.getKey(), targetEntry.getValue());
            }
        }
        saveConfig();
    }
}
