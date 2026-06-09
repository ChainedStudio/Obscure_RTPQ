package obscure.command;

import obscure.manager.QueueConfigMenu;
import obscure.manager.QueueManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class QueueCommand implements CommandExecutor {

    private final QueueManager queueManager;
    private final QueueConfigMenu configMenu;

    // Updated constructor accepting the configuration menu engine
    public QueueCommand(QueueManager queueManager, QueueConfigMenu configMenu) {
        this.queueManager = queueManager;
        this.configMenu = configMenu;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use the matchmaking queue.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length > 0) {
            // Check for subcommand: /rtpq debug
            if (args[0].equalsIgnoreCase("debug")) {
                if (!player.hasPermission("obscurertpq.admin")) {
                    player.sendMessage(ChatColor.RED + "You do not have permission to use debug mode.");
                    return true;
                }
                queueManager.toggleDebugMode(player);
                return true;
            }

            // Check for new subcommand: /rtpq config
            if (args[0].equalsIgnoreCase("config")) {
                if (!player.hasPermission("obscurertpq.admin")) {
                    player.sendMessage(ChatColor.RED + "You do not have permission to configure worlds.");
                    return true;
                }
                configMenu.openMenu(player);
                return true;
            }
        }

        // Standard command behavior: /rtpq or /queue
        queueManager.toggleQueue(player);
        return true;
    }
}