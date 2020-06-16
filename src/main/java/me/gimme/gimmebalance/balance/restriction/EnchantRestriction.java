package me.gimme.gimmebalance.balance.restriction;

import me.gimme.gimmebalance.actionbar.ActionBarSendingUtil;
import me.gimme.gimmebalance.config.Config;
import me.gimme.gimmebalance.language.LanguageManager;
import me.gimme.gimmebalance.language.Message;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class EnchantRestriction implements Listener {

    private FileConfiguration config;
    private LanguageManager languageManager;

    public EnchantRestriction(FileConfiguration config, LanguageManager languageManager) {
        this.config = config;
        this.languageManager = languageManager;
    }

    /**
     * Prevents enchantments from being any higher than the limit set in the config when using an enchantment table.
     */
    @EventHandler(priority = EventPriority.LOWEST)
    private void onEnchant(EnchantItemEvent event) {
        if (event.isCancelled()) return;
        Map<Enchantment, Integer> enchantsToAdd = event.getEnchantsToAdd();

        ConfigurationSection enchantmentLimits = config.getConfigurationSection(Config.ENCHANTMENT_LIMITS.getPath());
        if (enchantmentLimits == null) return;

        boolean restricted = false;
        for (Map.Entry<Enchantment, Integer> entry : enchantsToAdd.entrySet()) {
            String enchantmentName = entry.getKey().getKey().getKey();
            if (!enchantmentLimits.contains(enchantmentName)) continue;
            int limit = enchantmentLimits.getInt(enchantmentName);

            if (limit <= 0) {
                enchantsToAdd.remove(entry.getKey());
                restricted = true;
            } else if (limit < entry.getValue()) {
                entry.setValue(limit);
                restricted = true;
            }
        }

        if (restricted) ActionBarSendingUtil.sendActionBar(event.getEnchanter(),
                languageManager.get(Message.ENCHANTMENT_RESTRICTED).toString());
    }

    /**
     * Prevents the result of combining enchantments in anvils from resulting in a higher level than the limit set in the config.
     */
    @EventHandler(priority = EventPriority.LOWEST)
    private void onEnchantmentCombine(PrepareAnvilEvent event) {
        ItemStack result = event.getResult();
        if (result == null) return;
        Map<Enchantment, Integer> enchantments = result.getEnchantments();

        ConfigurationSection enchantmentLimits = config.getConfigurationSection(Config.ENCHANTMENT_LIMITS.getPath());
        if (enchantmentLimits == null) return;

        for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
            String enchantmentName = entry.getKey().getKey().getKey();
            if (!enchantmentLimits.contains(enchantmentName)) continue;
            int limit = enchantmentLimits.getInt(enchantmentName);

            if (limit <= 0) result.removeEnchantment(entry.getKey());
            else if (limit < entry.getValue()) result.addEnchantment(entry.getKey(), entry.getValue());
        }
    }

}
