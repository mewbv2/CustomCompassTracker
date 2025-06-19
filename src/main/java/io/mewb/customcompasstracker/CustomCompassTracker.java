package io.mewb.customcompasstracker;

import io.mewb.customcompasstracker.command.CommandManager;
import io.mewb.customcompasstracker.command.TabCompletionManager;
import io.mewb.customcompasstracker.manager.TargetManager;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.List;

public class CustomCompassTracker extends JavaPlugin {

    private TargetManager targetManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        
        int maxTargets = getConfig().getInt("max-targets", 5);

        this.targetManager = new TargetManager(maxTargets);
        CommandManager commandManager = new CommandManager(this, targetManager);
        TabCompletionManager tabCompletionManager = new TabCompletionManager(targetManager);

        List<String> commandsToRegister = Arrays.asList("settarget", "listtargets", "tracktarget", "removetarget");
        for (String commandName : commandsToRegister) {
            PluginCommand command = getCommand(commandName);
            if (command != null) {
                command.setExecutor(commandManager);
                // Only tracktarget and removetarget need tab completion for target names
                if (commandName.equals("tracktarget") || commandName.equals("removetarget")) {
                    command.setTabCompleter(tabCompletionManager);
                }
            } else {
                getLogger().warning("Could not find command '" + commandName + "' in plugin.yml!");
            }
        }

        getLogger().info("CustomCompassTracker has been enabled!");
    }

    @Override
    public void onDisable() {
        // Clear data from memory to prevent memory leaks
        if (targetManager != null) {
            targetManager.clearAllTargets();
        }
        getLogger().info("CustomCompassTracker has been disabled!");
    }
}
