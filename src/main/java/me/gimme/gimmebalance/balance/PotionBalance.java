package me.gimme.gimmebalance.balance;

import me.gimme.gimmebalance.config.Config;
import me.gimme.gimmebalance.util.PotionMetaUtils;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class PotionBalance implements Listener {

    private static final Set<Material> potionMaterials = new HashSet<>(Arrays.asList(Material.POTION, Material.SPLASH_POTION, Material.LINGERING_POTION));

    private Plugin plugin;
    private FileConfiguration config;
    private Random random = new Random();

    public PotionBalance(@NotNull Plugin plugin, @NotNull FileConfiguration config) {
        this.plugin = plugin;
        this.config = config;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onPlayerUsePotion(PlayerItemConsumeEvent event) {
        if (event.isCancelled()) return;

        ItemStack item = event.getItem();
        if (!potionMaterials.contains(item.getType())) return;

        PotionMeta potionMeta = (PotionMeta) item.getItemMeta();
        if (potionMeta == null) return;
        if (potionMeta.hasCustomEffects()) return;

        Player player = event.getPlayer();

        ConfigurationSection cs = config.getConfigurationSection(Config.POTION_DURATION.getPath());
        if (cs == null) return;

        PotionMetaUtils.PotionEffectResult potionEffect = PotionMetaUtils.getBasePotionEffectTypeName(potionMeta);
        PotionEffectType potionType = potionEffect.getType();
        if (potionType == null) return;
        if (potionEffect.getName() == null) return;

        String path1 = potionEffect.getName() + "{" + potionEffect.getExtendedOrUpgraded() + "}";
        String path2 = potionEffect.getName();

        int duration;

        if (cs.contains(path1)) {
            duration = cs.getInt(path1);
        } else if (cs.contains(path2)) {
            duration = cs.getInt(path2);
        } else {
            return;
        }

        boolean isUpgraded = potionMeta.getBasePotionData().isUpgraded();
        PotionEffect newEffect = potionType.createEffect(duration * 20, isUpgraded ? 1 : 0);

        PotionEffectType basePotionEffectType = potionMeta.getBasePotionData().getType().getEffectType();

        new BukkitRunnable() {
            @Override
            public void run() {
                if (basePotionEffectType != null) player.removePotionEffect(basePotionEffectType);
                player.addPotionEffect(newEffect);
            }
        }.runTaskLater(plugin, 0);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private void onPotionRegen(EntityRegainHealthEvent event) {
        if (!event.getRegainReason().equals(EntityRegainHealthEvent.RegainReason.MAGIC_REGEN)) return;

        double rate = config.getDouble(Config.POTION_REGEN_TICK_SUCCESS_RATE.getPath());
        if (rate == 1) return;

        int rateFloor = (int) Math.floor(rate);
        double residual = rate - rateFloor;

        double roll = random.nextDouble();
        double extra = roll < residual ? 1 : 0;

        double originalAmount = event.getAmount();
        event.setAmount(originalAmount * (rateFloor + extra));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private void onPotionHeal(EntityRegainHealthEvent event) {
        if (!event.getRegainReason().equals(EntityRegainHealthEvent.RegainReason.MAGIC)) return;

        double max = config.getDouble(Config.POTION_INSTANT_HEALTH_MAX.getPath());
        double modifier = config.getDouble(Config.POTION_INSTANT_HEALTH_MODIFIER.getPath());
        double modifiedValue = event.getAmount() * modifier;

        event.setAmount(Math.min(modifiedValue, max));
    }

}
