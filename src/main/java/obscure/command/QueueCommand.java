package obscure.command;

import obscure.manager.QueueManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class QueueCommand implements CommandExecutor {

    private final QueueManager queueManager;

    public QueueCommand(QueueManager queueManager) {
        this.queueManager = queueManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use the matchmaking queue.");
            return true;
        }

        Player player = (Player) sender;

        // Check for subcommands: /rtpq debug
        if (args.length > 0 && args[0].equalsIgnoreCase("debug")) {
            if (!player.hasPermission("obscurertpq.admin")) {
                player.sendMessage(ChatColor.RED + "You do not have permission to use debug mode.");
                return true;
            }

            queueManager.toggleDebugMode(player);
            return true;
        }

        // Standard command behavior: /rtpq or /queue
        queueManager.toggleQueue(player);
        return true;
    }
}