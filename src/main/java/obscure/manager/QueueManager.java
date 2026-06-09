package obscure.manager;

import obscure.main.ObscureRTPQ;
import obscure.provider.RtpProvider;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

public class QueueManager {

    private final ObscureRTPQ plugin;
    private final Queue<UUID> queue = new ConcurrentLinkedQueue<>();
    private final RtpProvider rtpProvider;

    public QueueManager(ObscureRTPQ plugin, RtpProvider rtpProvider) {
        this.plugin = plugin;
        this.rtpProvider = rtpProvider;
    }

    // New method to handle the live command toggle
    public void toggleDebugMode(Player player) {
        boolean currentMode = plugin.getConfig().getBoolean("debug-mode", false);
        boolean newMode = !currentMode;

        plugin.getConfig().set("debug-mode", newMode);
        plugin.saveConfig(); // Saves changes immediately to config.yml

        if (newMode) {
            player.sendMessage(ChatColor.GREEN + "RTPQ Debug Mode has been ENABLED. /rtpq will now match you solo.");
        } else {
            player.sendMessage(ChatColor.RED + "RTPQ Debug Mode has been DISABLED. Normal matchmaking resumed.");
        }
    }

    public void toggleQueue(Player player) {
        UUID uuid = player.getUniqueId();

        if (queue.contains(uuid)) {
            queue.remove(uuid);
            sendMessage(player, "messages.queue-leave");
            plugin.playConfigSound(player, "queue-leave");
            return;
        }

        // Checks dynamic runtime config state
        if (plugin.getConfig().getBoolean("debug-mode", false)) {
            player.sendMessage(ChatColor.GOLD + "[DEBUG] Solo matchmaking triggered!");
            plugin.playConfigSound(player, "queue-join");
            startDebugMatch(player);
            return;
        }

        queue.add(uuid);
        sendMessage(player, "messages.queue-join");
        plugin.playConfigSound(player, "queue-join");
        tryMatchmaking();
    }

    private void tryMatchmaking() {
        if (queue.size() < 2) return;

        UUID id1 = queue.poll();
        UUID id2 = queue.poll();

        Player p1 = Bukkit.getPlayer(id1);
        Player p2 = Bukkit.getPlayer(id2);

        if (p1 == null || !p1.isOnline()) {
            if (p2 != null && p2.isOnline()) queue.add(id2);
            return;
        }
        if (p2 == null || !p2.isOnline()) {
            queue.add(id1);
            return;
        }

        startMatch(p1, p2);
    }

    private void startMatch(Player p1, Player p2) {
        sendMessage(p1, "messages.match-found");
        sendMessage(p2, "messages.match-found");

        plugin.playConfigSound(p1, "match-found");
        plugin.playConfigSound(p2, "match-found");

        rtpProvider.getRandomLocation(p1.getWorld()).thenAccept(loc -> {
            Bukkit.getScheduler().runTask(plugin, () -> {
                Location p2Loc = loc.clone().add(3, 0, 3);

                p1.teleport(loc);
                p2.teleport(p2Loc);

                p1.sendMessage(formatMsg("messages.match-start").replace("%opponent%", p2.getName()));
                p2.sendMessage(formatMsg("messages.match-start").replace("%opponent%", p1.getName()));
            });
        });
    }

    private void startDebugMatch(Player player) {
        sendMessage(player, "messages.match-found");
        plugin.playConfigSound(player, "match-found");

        rtpProvider.getRandomLocation(player.getWorld()).thenAccept(loc -> {
            Bukkit.getScheduler().runTask(plugin, () -> {
                player.teleport(loc);
                player.sendMessage(formatMsg("messages.match-start").replace("%opponent%", "DummyOpponent"));
            });
        });
    }

    public void removeFromQueue(UUID uuid) {
        queue.remove(uuid);
    }

    private void sendMessage(Player player, String path) {
        player.sendMessage(formatMsg(path));
    }

    private String formatMsg(String path) {
        String msg = plugin.getConfig().getString(path, "");
        return ChatColor.translateAlternateColorCodes('&', msg);
    }
}