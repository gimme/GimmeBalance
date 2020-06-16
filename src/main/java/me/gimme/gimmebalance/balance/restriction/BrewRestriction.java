package me.gimme.gimmebalance.balance.restriction;

import me.gimme.gimmebalance.config.Config;
import me.gimme.gimmebalance.util.PotionMetaUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.BrewEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionData;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class BrewRestriction implements Listener {

    private Plugin plugin;
    private FileConfiguration config;

    public BrewRestriction(@NotNull Plugin plugin, @NotNull FileConfiguration config) {
        this.plugin = plugin;
        this.config = config;
    }

    /**
     * Prevents brewing unbrewable potions from the config.
     */
    @EventHandler(priority = EventPriority.LOW)
    private void onBrew(BrewEvent event) {
        List<String> unbrewable = config.getStringList(Config.UNBREWABLE.getPath());

        ItemStack[] potionStates = Arrays.copyOfRange(event.getContents().getContents(), 0, 3);
        for (int i = 0; i <= 2; i++) {
            ItemStack potion = potionStates[i];
            if (potion != null) potionStates[i] = potion.clone();
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                for (int i = 0; i <= 2; i++) {
                    ItemStack item = event.getContents().getItem(i);
                    if (item == null || item.getItemMeta() == null || !(item.getItemMeta() instanceof PotionMeta)) continue;

                    PotionData potionData = ((PotionMeta) item.getItemMeta()).getBasePotionData();
                    PotionMetaUtils.PotionEffectResult potionEffect = new PotionMetaUtils.PotionEffectResult(potionData);
                    boolean isSplash = item.getType().equals(Material.SPLASH_POTION);
                    boolean isLingering = item.getType().equals(Material.LINGERING_POTION);

                    if (unbrewable.contains(potionEffect.getName())
                            || unbrewable.contains(potionEffect.getName() + "{" + potionEffect.getExtendedOrUpgraded() + "}")
                            || (isSplash && unbrewable.contains(potionEffect.getName() + "{SPLASH}"))
                            || (isLingering && unbrewable.contains(potionEffect.getName() + "{LINGERING}"))) {
                        event.getContents().setItem(i, potionStates[i]);
                    }
                }
            }
        }.runTaskLater(plugin, 0L);
    }

}
