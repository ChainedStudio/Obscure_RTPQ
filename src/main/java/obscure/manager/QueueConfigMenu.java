package obscure.manager;

import obscure.main.ObscureRTPQ;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class QueueConfigMenu implements Listener {

    private final ObscureRTPQ plugin;
    private final String menuTitle = ChatColor.DARK_GRAY + "RTPQ: Toggle Worlds";

    public QueueConfigMenu(ObscureRTPQ plugin) {
        this.plugin = plugin;
    }

    public void openMenu(Player player) {
        List<World> worlds = Bukkit.getWorlds();
        // Create an inventory size large enough to hold all worlds (rounded up to a multiple of 9)
        int size = ((worlds.size() / 9) + 1) * 9;
        if (size > 54) size = 54;

        Inventory inv = Bukkit.createInventory(null, size, menuTitle);
        List<String> enabledWorlds = plugin.getConfig().getStringList("enabled-worlds");

        for (int i = 0; i < worlds.size() && i < 54; i++) {
            World world = worlds.get(i);
            boolean isEnabled = enabledWorlds.contains(world.getName());

            // Choose an item block type based on whether the world is enabled
            ItemStack item = new ItemStack(isEnabled ? Material.GREEN_WOOL : Material.RED_WOOL);
            ItemMeta meta = item.getItemMeta();

            if (meta != null) {
                meta.setDisplayName(ChatColor.GOLD + world.getName());
                List<String> lore = new ArrayList<>();
                lore.add("");
                lore.add(isEnabled ? ChatColor.GREEN + "● Enabled" : ChatColor.RED + "○ Disabled");
                lore.add(ChatColor.GRAY + "Click to toggle this world");
                meta.setLore(lore);
                item.setItemMeta(meta);
            }
            inv.setItem(i, item);
        }

        player.openInventory(inv);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(menuTitle)) return;
        event.setCancelled(true); // Stop players from taking the wool out

        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || !clickedItem.hasItemMeta()) return;

        ItemMeta meta = clickedItem.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return;

        // Extract the world name from the display name
        String worldName = ChatColor.stripColor(meta.getDisplayName());
        List<String> enabledWorlds = plugin.getConfig().getStringList("enabled-worlds");

        if (enabledWorlds.contains(worldName)) {
            enabledWorlds.remove(worldName);
            player.sendMessage(ChatColor.RED + "Disabled queue matching for world: " + worldName);
        } else {
            enabledWorlds.add(worldName);
            player.sendMessage(ChatColor.GREEN + "Enabled queue matching for world: " + worldName);
        }

        // Save back to config.yml memory and file system
        plugin.getConfig().set("enabled-worlds", enabledWorlds);
        plugin.saveConfig();

        // Refresh menu visuals instantly for the player
        openMenu(player);
    }
}