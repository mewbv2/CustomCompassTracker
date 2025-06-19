package io.mewb.customcompasstracker;

import io.mewb.customcompasstracker.command.CommandManager;
import io.mewb.customcompasstracker.command.TabCompletionManager;
import io.mewb.customcompasstracker.manager.DataManager;
import io.mewb.customcompasstracker.manager.TargetManager;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.List;

public class CustomCompassTracker extends JavaPlugin {

    private TargetManager targetManager;
    private DataManager dataManager;

    @Override
    public void onEnable() {

        saveDefaultConfig();

        this.dataManager = new DataManager(this);

        int maxTargets = getConfig().getInt("max-targets", 5);

        this.targetManager = new TargetManager(dataManager, maxTargets);

        CommandManager commandManager = new CommandManager(this, targetManager);
        TabCompletionManager tabCompletionManager = new TabCompletionManager(targetManager);

        List<String> commandsToRegister = Arrays.asList("settarget", "listtargets", "tracktarget", "removetarget");
        for (String commandName : commandsToRegister) {
            PluginCommand command = getCommand(commandName);
            if (command != null) {
                command.setExecutor(commandManager);
                if (commandName.equals("tracktarget") || commandName.equals("removetarget")) {
                    command.setTabCompleter(tabCompletionManager);
                }
            } else {
                getLogger().warning("Could not find command '" + commandName + "' in plugin.yml! Please check your plugin.yml file.");
            }
        }

        getLogger().info("CustomCompassTracker has been enabled with persistent data storage.");
    }

    @Override
    public void onDisable() {
        if (targetManager != null) {
            targetManager.saveAllData();
            targetManager.clearAllTargets();
        }
        getLogger().info("CustomCompassTracker has been disabled. All targets have been saved.");
    }
}
