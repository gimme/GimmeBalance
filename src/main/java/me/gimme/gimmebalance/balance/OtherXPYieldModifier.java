package me.gimme.gimmebalance.balance;

import me.gimme.gimmebalance.config.Config;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityBreedEvent;
import org.bukkit.event.inventory.FurnaceExtractEvent;
import org.bukkit.event.player.PlayerFishEvent;

/**
 * Modifies the XP yield of mining and breeding.
 */
public class OtherXPYieldModifier implements Listener {

    private FileConfiguration config;

    public OtherXPYieldModifier(FileConfiguration config) {
        this.config = config;
    }

    /**
     * Modifies the XP yield of mining.
     */
    @EventHandler(priority = EventPriority.HIGH)
    private void onBlockBreak(BlockBreakEvent event) {
        double multiplier;
        String materialNamespaceId = event.getBlock().getType().getKey().getKey();

        if (event.getExpToDrop() == 0) return;

        ConfigurationSection blocks = config.getConfigurationSection(Config.MINING_XP_DROP_MULTIPLIER_BLOCK.getPath());
        if (blocks != null && blocks.contains(materialNamespaceId)) {
            multiplier = blocks.getDouble(materialNamespaceId);
        } else {
            multiplier = config.getDouble(Config.MINING_XP_DROP_MULTIPLIER_DEFAULT.getPath());
        }

        event.setExpToDrop((int) Math.round(event.getExpToDrop() * multiplier));
    }

    /**
     * Modifies the XP yield of breeding.
     */
    @EventHandler(priority = EventPriority.HIGH)
    private void onEntityBreed(EntityBreedEvent event) {
        if (event.getExperience() == 0) return;

        double multiplier = config.getDouble(Config.OTHER_XP_MULTIPLIERS_BREEDING.getPath());
        event.setExperience((int) Math.round(event.getExperience() * multiplier));
    }

    /**
     * Modifies the XP yield of fishing.
     */
    @EventHandler(priority = EventPriority.HIGH)
    private void onPlayerFish(PlayerFishEvent event) {
        if (event.getExpToDrop() == 0) return;

        double multiplier = config.getDouble(Config.OTHER_XP_MULTIPLIERS_FISHING.getPath());
        event.setExpToDrop((int) Math.round(event.getExpToDrop() * multiplier));
    }

    /**
     * Modifies the XP yield of smelting/cooking.
     */
    @EventHandler(priority = EventPriority.HIGH)
    private void onFurnaceSmelt(FurnaceExtractEvent event) {
        if (event.getExpToDrop() == 0) return;

        double multiplier = config.getDouble(Config.OTHER_XP_MULTIPLIERS_FURNACE.getPath());
        event.setExpToDrop((int) Math.round(event.getExpToDrop() * multiplier));
    }

}
