package obscure.manager;

import obscure.main.ObscureRTPQ;
import obscure.provider.RtpProvider;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.List;
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

    public void toggleDebugMode(Player player) {
        boolean currentMode = plugin.getConfig().getBoolean("debug-mode", false);
        boolean newMode = !currentMode;

        plugin.getConfig().set("debug-mode", newMode);
        plugin.saveConfig();

        if (newMode) {
            player.sendMessage(ChatColor.GREEN + "RTPQ Debug Mode has been ENABLED. /rtpq will now match you solo.");
        } else {
            player.sendMessage(ChatColor.RED + "RTPQ Debug Mode has been DISABLED. Normal matchmaking resumed.");
        }
    }

    public void toggleQueue(Player player) {
        UUID uuid = player.getUniqueId();

        // 1. Verify if the player's current world is allowed by the configuration registry
        List<String> enabledWorlds = plugin.getConfig().getStringList("enabled-worlds");
        String currentWorldName = player.getWorld().getName();

        if (!enabledWorlds.contains(currentWorldName)) {
            player.sendMessage(ChatColor.RED + "You cannot join the matchmaking queue from this world!");
            return;
        }

        if (queue.contains(uuid)) {
            queue.remove(uuid);
            sendMessage(player, "messages.queue-leave");
            plugin.playConfigSound(player, "queue-leave");
            return;
        }

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

        // Verify both players are still standing inside valid worlds before pairing them
        List<String> enabledWorlds = plugin.getConfig().getStringList("enabled-worlds");
        if (!enabledWorlds.contains(p1.getWorld().getName()) || !enabledWorlds.contains(p2.getWorld().getName())) {
            if (enabledWorlds.contains(p1.getWorld().getName())) queue.add(id1);
            if (enabledWorlds.contains(p2.getWorld().getName())) queue.add(id2);
            return;
        }

        startMatch(p1, p2);
    }

    private void startMatch(Player p1, Player p2) {
        sendMessage(p1, "messages.match-found");
        sendMessage(p2, "messages.match-found");

        plugin.playConfigSound(p1, "match-found");
        plugin.playConfigSound(p2, "match-found");

        // Dynamically execute the teleport location search inside their current world
        World targetWorld = p1.getWorld();

        rtpProvider.getRandomLocation(targetWorld).thenAccept(loc -> {
            Bukkit.getScheduler().runTask(plugin, () -> {
                // Ensure players haven't logged off or switched worlds during the async chunk calculation phase
                if (!p1.isOnline() || !p2.isOnline()) return;

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

        World targetWorld = player.getWorld();

        rtpProvider.getRandomLocation(targetWorld).thenAccept(loc -> {
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (!player.isOnline()) return;

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