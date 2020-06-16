package me.gimme.gimmebalance.event.callers;

import me.gimme.gimmebalance.event.PlayerUseItemEvent;
import me.gimme.gimmebalance.event.PlayerUsePotionEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.projectiles.ProjectileSource;
import org.jetbrains.annotations.NotNull;

public class PlayerUseItemEventCaller implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onPlayerInteract(PlayerInteractEvent event) {
        if (event.useItemInHand().equals(Event.Result.DENY)) return;

        ItemStack item = event.getItem();
        if (item == null) return;

        if (callEvent(event.getPlayer(), item, PlayerUseItemEvent.Type.INTERACT)) return;
        event.setUseItemInHand(Event.Result.DENY);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onPlayerItemConsume(PlayerItemConsumeEvent event) {
        if (event.isCancelled()) return;
        if (callEvent(event.getPlayer(), event.getItem(), PlayerUseItemEvent.Type.CONSUME)) return;
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onProjectileLaunch(ProjectileLaunchEvent event) {
        if (event.isCancelled()) return;
        ProjectileSource source = event.getEntity().getShooter();
        if (!(source instanceof Player)) return;

        Player thrower = (Player) event.getEntity().getShooter();
        ItemStack item = thrower.getInventory().getItemInMainHand();

        if (callEvent(thrower, item, PlayerUseItemEvent.Type.LAUNCH)) return;
        event.setCancelled(true);
    }

    private boolean callEvent(@NotNull Player player, @NotNull ItemStack item, @NotNull PlayerUseItemEvent.Type type) {
        PlayerUseItemEvent playerUseItemEvent;
        if (item.getItemMeta() != null && item.getType().equals(Material.POTION) ||
                item.getType().equals(Material.SPLASH_POTION) ||
                item.getType().equals(Material.LINGERING_POTION)) {
            playerUseItemEvent = new PlayerUsePotionEvent(player, item, type, (PotionMeta) item.getItemMeta());
        } else {
            playerUseItemEvent = new PlayerUseItemEvent(player, item, type);
        }

        Bukkit.getPluginManager().callEvent(playerUseItemEvent);
        return !playerUseItemEvent.isCancelled();
    }

}
