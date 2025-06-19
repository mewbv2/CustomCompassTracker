package io.mewb.customcompasstracker.manager;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages all operations related to player targets.
 * This class acts as a cache layer on top of the DataManager.
 */
public class TargetManager {

    private final DataManager dataManager;
    private final Map<UUID, Map<String, Location>> playerTargetsCache;
    private final int maxTargets;

    public TargetManager(DataManager dataManager, int maxTargets) {
        this.dataManager = dataManager;
        this.maxTargets = maxTargets;
        this.playerTargetsCache = this.dataManager.loadAllTargets();
    }

    /**
     * Adds a new target for a player. This updates the cache.
     * The data will be saved to file in bulk on plugin shutdown.
     * @param player The player setting the target.
     * @param name The name of the target.
     * @return true if the target was added successfully, false if the player has reached their max target limit.
     */
    public boolean addTarget(Player player, String name) {
        Map<String, Location> targets = playerTargetsCache.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>());

        if (targets.size() >= maxTargets && !targets.containsKey(name)) {
            return false;
        }

        targets.put(name, player.getLocation());
        return true;
    }

    /**
     * Removes a specific target for a player from the cache.
     * @param player The player whose target should be removed.
     * @param name The name of the target to remove.
     */
    public void removeTarget(Player player, String name) {
        Map<String, Location> targets = playerTargetsCache.get(player.getUniqueId());
        if (targets != null) {
            targets.remove(name);
        }
    }

    /**
     * Retrieves a specific target location for a player from the cache.
     * @param player The player whose target is being requested.
     * @param name The name of the target.
     * @return The Location of the target, or null if it doesn't exist.
     */
    public Location getTarget(Player player, String name) {
        Map<String, Location> targets = playerTargetsCache.get(player.getUniqueId());
        if (targets != null) {
            return targets.get(name);
        }
        return null;
    }

    /**
     * Retrieves all targets for a specific player from the cache.
     * @param player The player whose targets are being requested.
     * @return An immutable map of all target names and their locations. Returns an empty map if none exist.
     */
    public Map<String, Location> getAllTargets(Player player) {
        return playerTargetsCache.getOrDefault(player.getUniqueId(), Collections.emptyMap());
    }

    /**
     * Saves all cached target data to disk via the DataManager.
     * Called on plugin disable.
     */
    public void saveAllData() {
        dataManager.saveAllTargets(playerTargetsCache);
    }

    /**
     * Clears all targets from the in-memory cache.
     * This is useful for plugin reloads or on shutdown after saving data.
     */
    public void clearAllTargets() {
        playerTargetsCache.clear();
    }
}
