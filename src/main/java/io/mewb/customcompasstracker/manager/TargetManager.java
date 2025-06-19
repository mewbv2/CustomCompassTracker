package io.mewb.customcompasstracker.manager;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TargetManager {

    private final Map<UUID, Map<String, Location>> playerTargets = new HashMap<>();
    private final int maxTargets;

    public TargetManager(int maxTargets) {
        this.maxTargets = maxTargets;
    }

    /**
     * Adds a new target for a player at their current location.
     * @param player The player setting the target.
     * @param name The name of the target.
     * @return true if the target was added successfully, false if the player has reached their max target limit.
     */
    public boolean addTarget(Player player, String name) {
        Map<String, Location> targets = playerTargets.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>());

        // Do not allow adding a new target if the max limit is reached.
        // Overwriting an existing target is still allowed.
        if (targets.size() >= maxTargets && !targets.containsKey(name)) {
            return false;
        }

        targets.put(name, player.getLocation());
        return true;
    }

    /**
     * Removes a specific target for a player.
     * @param player The player whose target should be removed.
     * @param name The name of the target to remove.
     */
    public void removeTarget(Player player, String name) {
        Map<String, Location> targets = playerTargets.get(player.getUniqueId());
        if (targets != null) {
            targets.remove(name);
        }
    }

    /**
     * Retrieves a specific target location for a player.
     * @param player The player whose target is being requested.
     * @param name The name of the target.
     * @return The Location of the target, or null if it doesn't exist.
     */
    public Location getTarget(Player player, String name) {
        Map<String, Location> targets = playerTargets.get(player.getUniqueId());
        if (targets != null) {
            return targets.get(name);
        }
        return null;
    }

    /**
     * Retrieves all targets for a specific player.
     * @param player The player whose targets are being requested.
     * @return A map of all target names and their locations. Returns an empty map if none exist.
     */
    public Map<String, Location> getAllTargets(Player player) {
        return playerTargets.getOrDefault(player.getUniqueId(), Collections.emptyMap());
    }

    /**
     * Clears all targets for all players from memory.
     * Called on plugin disable.
     */
    public void clearAllTargets() {
        playerTargets.clear();
    }
}
