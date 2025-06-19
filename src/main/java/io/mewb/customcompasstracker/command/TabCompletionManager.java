package io.mewb.customcompasstracker.command;

import io.mewb.customcompasstracker.manager.TargetManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


public class TabCompletionManager implements TabCompleter {

    private final TargetManager targetManager;

    public TabCompletionManager(TargetManager targetManager) {
        this.targetManager = targetManager;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player)) {
            return Collections.emptyList();
        }
        Player player = (Player) sender;

        // We only complete the first argument, which is the target name
        if (args.length == 1) {
            // Return a filtered list of the player's target names
            return targetManager.getAllTargets(player).keySet().stream()
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }
}
