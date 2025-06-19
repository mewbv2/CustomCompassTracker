package io.mewb.customcompasstracker.command;

import io.mewb.customcompasstracker.CustomCompassTracker;
import io.mewb.customcompasstracker.manager.TargetManager;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;


public class CommandManager implements CommandExecutor {

    private final CustomCompassTracker plugin;
    private final TargetManager targetManager;
    private final List<String> ignoredWorlds;
    private final int maxTargets;

    public CommandManager(CustomCompassTracker plugin, TargetManager targetManager) {
        this.plugin = plugin;
        this.targetManager = targetManager;
        // Cache config values for slightly better performance
        this.ignoredWorlds = plugin.getConfig().getStringList("ignored-worlds");
        this.maxTargets = plugin.getConfig().getInt("max-targets", 5);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be run by a player.");
            return true;
        }

        Player player = (Player) sender;

        // Centralized permission and world checks
        if (!player.hasPermission("customcompass.use")) {
            player.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return true;
        }

        if (ignoredWorlds.contains(player.getWorld().getName())) {
            player.sendMessage(ChatColor.RED + "You cannot use the compass tracker in this world.");
            return true;
        }

        // Delegate to specific handler methods based on the command
        switch (command.getName().toLowerCase()) {
            case "settarget":
                return handleSetTarget(player, args);
            case "listtargets":
                return handleListTargets(player);
            case "tracktarget":
                return handleTrackTarget(player, args);
            case "removetarget":
                return handleRemoveTarget(player, args);
        }

        return false;
    }

    private boolean handleSetTarget(Player player, String[] args) {
        if (args.length == 0) {
            player.sendMessage(ChatColor.RED + "Usage: /settarget <name>");
            return true;
        }
        String targetName = args[0];

        if (targetManager.addTarget(player, targetName)) {
            player.sendMessage(ChatColor.GREEN + "Target '" + targetName + "' set to your current location.");
        } else {
            player.sendMessage(ChatColor.RED + "You have reached the maximum number of targets (" + maxTargets + ").");
        }
        return true;
    }

    private boolean handleListTargets(Player player) {
        Map<String, Location> targets = targetManager.getAllTargets(player);

        if (targets.isEmpty()) {
            player.sendMessage(ChatColor.YELLOW + "You have no saved targets.");
            return true;
        }

        player.sendMessage(ChatColor.GOLD + "Your saved targets:");
        for (Map.Entry<String, Location> entry : targets.entrySet()) {
            Location loc = entry.getValue();
            String message = String.format(ChatColor.AQUA + "- %s: " + ChatColor.WHITE + "X:%.1f, Y:%.1f, Z:%.1f in %s",
                    entry.getKey(), loc.getX(), loc.getY(), loc.getZ(), loc.getWorld().getName());
            player.sendMessage(message);
        }
        return true;
    }

    private boolean handleTrackTarget(Player player, String[] args) {
        if (args.length == 0) {
            player.sendMessage(ChatColor.RED + "Usage: /tracktarget <name>");
            return true;
        }
        String targetName = args[0];
        Location targetLocation = targetManager.getTarget(player, targetName);

        if (targetLocation == null) {
            player.sendMessage(ChatColor.RED + "Target '" + targetName + "' not found.");
            return true;
        }

        player.setCompassTarget(targetLocation);

        String message = String.format(ChatColor.GREEN + "Tracking '%s' at X:%.0f Y:%.0f Z:%.0f",
                targetName, targetLocation.getX(), targetLocation.getY(), targetLocation.getZ());

        // Send primary feedback to the action bar for a less intrusive feel
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message));
        // Send a confirmation to chat as well
        player.sendMessage(ChatColor.GREEN + "Compass now pointing to '" + targetName + "'.");

        return true;
    }

    private boolean handleRemoveTarget(Player player, String[] args) {
        if (args.length == 0) {
            player.sendMessage(ChatColor.RED + "Usage: /removetarget <name>");
            return true;
        }
        String targetName = args[0];

        // Check if the target exists before attempting to remove it
        if (targetManager.getTarget(player, targetName) == null) {
            player.sendMessage(ChatColor.RED + "Target '" + targetName + "' not found.");
            return true;
        }

        targetManager.removeTarget(player, targetName);
        player.sendMessage(ChatColor.GREEN + "Target '" + targetName + "' has been removed.");
        return true;
    }
}
