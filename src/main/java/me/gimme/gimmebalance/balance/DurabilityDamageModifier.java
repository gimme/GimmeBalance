package me.gimme.gimmebalance.balance;

import me.gimme.gimmebalance.config.Config;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemDamageEvent;

import java.util.Random;

public class DurabilityDamageModifier implements Listener {

    private FileConfiguration config;
    private Random random = new Random();

    public DurabilityDamageModifier(FileConfiguration config) {
        this.config = config;
    }

    /**
     * Modifies durability damage on certain tools and armor.
     */
    @EventHandler(priority = EventPriority.LOW)
    private void onPlayerItemDamage(PlayerItemDamageEvent event) {
        double multiplier;
        String itemNamespaceId = event.getItem().getType().getKey().getKey();

        ConfigurationSection items = config.getConfigurationSection(Config.DURABILITY_DAMAGE_MULTIPLIER_ITEM.getPath());
        if (items != null && items.contains(itemNamespaceId)) {
            multiplier = items.getDouble(itemNamespaceId);
        } else {
            multiplier = config.getDouble(Config.DURABILITY_DAMAGE_MULTIPLIER_DEFAULT.getPath());
        }

        double totalDamage = event.getDamage() * multiplier;
        int evenDamage = (int) Math.floor(totalDamage);
        double remainder = totalDamage - evenDamage;

        double roll = random.nextDouble();
        if (roll <= remainder) evenDamage++;

        event.setDamage(evenDamage);
    }

}
