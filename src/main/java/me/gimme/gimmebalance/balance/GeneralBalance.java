package me.gimme.gimmebalance.balance;

import me.gimme.gimmebalance.config.Config;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Gate;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.inventory.BrewingStandFuelEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GeneralBalance implements Listener {

    private static final Set<Material> FENCE_GATES = Stream.of(
            Material.ACACIA_FENCE_GATE,
            Material.BIRCH_FENCE_GATE,
            Material.JUNGLE_FENCE_GATE,
            Material.OAK_FENCE,
            Material.SPRUCE_FENCE_GATE,
            Material.DARK_OAK_FENCE_GATE)
            .collect(Collectors.toCollection(HashSet::new));

    private FileConfiguration config;

    public GeneralBalance(FileConfiguration config) {
        this.config = config;
    }

    /**
     * Makes ender pearls teleport the thrower to the other side of open fence gates.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    private void onPearlHit(ProjectileHitEvent event) {
        if (!event.getEntity().getType().equals(EntityType.ENDER_PEARL)) return;
        if (!config.getBoolean(Config.PEARL_THOUGH_GATE.getPath())) return;

        ProjectileSource source = event.getEntity().getShooter();
        if (!(source instanceof Player)) return;
        Player thrower = (Player) event.getEntity().getShooter();
        Block blockHit = event.getHitBlock();
        if (blockHit != null && event.getHitBlockFace() != null && FENCE_GATES.contains(blockHit.getType())) {
            if (!((Gate) blockHit.getBlockData()).isOpen()) return;

            Location newLoc = blockHit.getRelative(event.getHitBlockFace().getOppositeFace()).getLocation();
            Vector direction = event.getHitBlockFace().getOppositeFace().getDirection().normalize();
            newLoc.setX((newLoc.getX() + 0.5) - direction.getX() * 0.8);
            newLoc.setZ((newLoc.getZ() + 0.5) - direction.getZ() * 0.8);
            newLoc.setYaw(thrower.getLocation().getYaw());
            newLoc.setPitch(thrower.getLocation().getPitch());

            thrower.teleport(newLoc);
        }
    }

    /**
     * Changes how many brewing batches blaze powder can fuel.
     */
    @EventHandler(priority = EventPriority.LOW)
    private void onBrewingStandFuel(BrewingStandFuelEvent event) {
        if (!event.getFuel().getType().equals(Material.BLAZE_POWDER)) return;
        int fuelPower = config.getInt(Config.BREWING_BLAZE_POWDER_FUEL_POWER.getPath());
        if (fuelPower <= 0) return;
        event.setFuelPower(fuelPower);
    }

    /**
     * Disables ender pearl self damage.
     */
    @EventHandler(priority = EventPriority.LOWEST)
    private void onPlayerTeleport(PlayerTeleportEvent event) {
        if (event.isCancelled()) return;
        if (!event.getCause().equals(PlayerTeleportEvent.TeleportCause.ENDER_PEARL)) return;
        if (event.getTo() == null) return;
        if (!config.getBoolean(Config.PEARL_DAMAGE_DISABLED.getPath())) return;

        event.setCancelled(true);
        Player player = event.getPlayer();
        player.setNoDamageTicks(1);
        player.teleport(event.getTo(), PlayerTeleportEvent.TeleportCause.ENDER_PEARL);
    }

}
