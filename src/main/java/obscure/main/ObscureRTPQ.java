package obscure.main;

import obscure.command.QueueCommand;
import obscure.manager.QueueConfigMenu; // Import the menu
import obscure.manager.QueueManager;
import obscure.provider.ObscureTeleportsProvider;
import obscure.provider.RtpProvider;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.CompletableFuture;

public final class ObscureRTPQ extends JavaPlugin implements Listener {

    private QueueManager queueManager;
    private QueueConfigMenu configMenu; // Instance holder

    @Override
    public void onEnable() {
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
        this.configMenu = new QueueConfigMenu(this); // Initialize menu

        // Pass the menu instance straight into the command parser
        getCommand("rtpq").setExecutor(new QueueCommand(queueManager, configMenu));

        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(configMenu, this); // Register menu events

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