package obscure.main;

import obscure.command.QueueCommand;
import obscure.manager.QueueManager;
import obscure.provider.RtpProvider;
import obscure.provider.ObscureTeleportsProvider;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.CompletableFuture;
import org.bukkit.Location;

public final class ObscureRTPQ extends JavaPlugin implements Listener {

    private QueueManager queueManager;

    @Override
    public void onEnable() {
        // Save config.yml if it doesn't exist
        saveDefaultConfig();

        RtpProvider provider;
        if (Bukkit.getPluginManager().isPluginEnabled("ObscureTeleport")) {
            getLogger().info("Found ObscureTeleport! Activating main plugin integration.");
            provider = new ObscureTeleportsProvider();
        } else {
            getLogger().warning("ObscureTeleport not found! Using standalone fallback generator.");
            provider = fallbackProvider();
        }

        this.queueManager = new QueueManager(this, provider);
        getCommand("rtpq").setExecutor(new QueueCommand(queueManager));
        getServer().getPluginManager().registerEvents(this, this);

        getLogger().info("ObscureRTPQ extension fully initialized.");
    }

    @Override
    public void onDisable() {
        getLogger().info("ObscureRTPQ has been shut down cleanly.");
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        queueManager.removeFromQueue(event.getPlayer().getUniqueId());
    }

    // Helper method to pull sounds out of the config file safely
    public void playConfigSound(Player player, String configPath) {
        String soundData = getConfig().getString("sounds." + configPath);
        if (soundData == null || soundData.isEmpty()) return;

        try {
            String[] parts = soundData.split(",");
            Sound sound = Sound.valueOf(parts[0].trim().toUpperCase());
            float volume = parts.length > 1 ? Float.parseFloat(parts[1].trim()) : 1.0f;
            float pitch = parts.length > 2 ? Float.parseFloat(parts[2].trim()) : 1.0f;

            player.playSound(player.getLocation(), sound, volume, pitch);
        } catch (IllegalArgumentException e) {
            getLogger().warning("Invalid sound configured at sounds." + configPath + ": " + soundData);
        }
    }

    private RtpProvider fallbackProvider() {
        return world -> CompletableFuture.supplyAsync(() -> {
            double x = (Math.random() * 1000) - 500;
            double z = (Math.random() * 1000) - 500;
            return new Location(world, x, world.getHighestBlockYAt((int)x, (int)z) + 1, z);
        });
    }
}