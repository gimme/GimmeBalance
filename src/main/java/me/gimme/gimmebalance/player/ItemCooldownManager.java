package me.gimme.gimmebalance.player;

import me.gimme.gimmebalance.actionbar.ActionBarSendingUtil;
import me.gimme.gimmebalance.config.Config;
import me.gimme.gimmebalance.event.PlayerUseItemEvent;
import me.gimme.gimmebalance.event.PlayerUsePotionEvent;
import me.gimme.gimmebalance.language.LanguageManager;
import me.gimme.gimmebalance.language.Message;
import me.gimme.gimmebalance.util.PotionMetaUtils;
import me.gimme.gimmecore.scoreboard.TimerScoreboardManager;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.meta.PotionMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ItemCooldownManager implements Listener {

    private FileConfiguration config;
    private LanguageManager languageManager;
    private TimerScoreboardManager timerScoreboardManager;

    private Map<UUID, Set<String>> itemsOnCooldownByPlayer = new HashMap<>();

    public ItemCooldownManager(@NotNull FileConfiguration config, @NotNull LanguageManager languageManager,
                               @NotNull TimerScoreboardManager timerScoreboardManager) {
        this.config = config;
        this.languageManager = languageManager;
        this.timerScoreboardManager = timerScoreboardManager;
    }

    @EventHandler(priority = EventPriority.HIGH)
    private void onPlayerUseItem(PlayerUseItemEvent event) {
        if (event.isCancelled()) return;
        Player player = event.getPlayer();

        CooldownResult cooldownResult = getCooldown(event.getItem().getType(), null, event.getType(),
                player.getWorld());
        if (cooldownResult == null) return;

        if (startCooldown(player, cooldownResult.title, cooldownResult.cooldown)) return;
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH)
    private void onPlayerUsePotion(PlayerUsePotionEvent event) {
        if (event.isCancelled()) return;
        Player player = event.getPlayer();

        CooldownResult cooldownResult = getCooldown(event.getItem().getType(), event.getPotionMeta(), event.getType(),
                player.getWorld());
        if (cooldownResult == null) return;

        if (startCooldown(player, cooldownResult.title, cooldownResult.cooldown)) return;
        event.setCancelled(true);
    }

    private boolean startCooldown(@NotNull Player player, @NotNull String title, int cooldown) {
        if (cooldown < 0) {
            ActionBarSendingUtil.sendActionBar(player, languageManager.get(Message.ITEM_DISABLED).toString());
            return false;
        }
        if (isOnCooldown(player, title)) {
            ActionBarSendingUtil.sendActionBar(player, languageManager.get(Message.ITEM_ON_COOLDOWN).toString());
            return false;
        }
        if (cooldown == 0) {
            return true;
        }

        itemsOnCooldownByPlayer.computeIfAbsent(player.getUniqueId(), k -> new HashSet<>());
        itemsOnCooldownByPlayer.get(player.getUniqueId()).add(title);

        int score = config.getInt(Config.SCOREBOARD_SCORE_ITEM_COOLDOWNS.getPath());
        timerScoreboardManager.startPlayerTimer(player, title, cooldown, score,
                () -> {
                    Set<String> cooldowns = itemsOnCooldownByPlayer.get(player.getUniqueId());
                    cooldowns.remove(title);
                    if (cooldowns.isEmpty()) itemsOnCooldownByPlayer.remove(player.getUniqueId());
                });

        return true;
    }

    private boolean isOnCooldown(@NotNull Player player, @NotNull String title) {
        Set<String> cooldowns = itemsOnCooldownByPlayer.get(player.getUniqueId());
        return cooldowns != null && cooldowns.contains(title);
    }

    @Nullable
    private CooldownResult getCooldown(@NotNull Material item, @Nullable PotionMeta potionMeta,
                                       @NotNull PlayerUseItemEvent.Type type, @NotNull World world) {
        CooldownResult cooldownResult = new CooldownResult();

        String itemNamespacedKey = item.getKey().getKey();

        ConfigurationSection itemCooldowns = config.getConfigurationSection(Config.ITEM_COOLDOWN.getPath());
        if (itemCooldowns == null) return null;

        ConfigurationSection itemSection = null;
        int cooldown = 0;
        if (potionMeta == null) {
            itemSection = itemCooldowns.getConfigurationSection(itemNamespacedKey);
            if (itemSection == null) return null;
            cooldown = getCooldownValueFromSection(itemSection, world);
        } else {
            for (PotionMetaUtils.PotionEffectResult potionEffect : PotionMetaUtils.getPotionEffectTypeNames(potionMeta)) {
                ConfigurationSection cs = itemCooldowns.getConfigurationSection(itemNamespacedKey +
                        "{" + potionEffect.getName() + "," + potionEffect.getExtendedOrUpgraded() + "}");
                if (cs == null) {
                    cs = itemCooldowns.getConfigurationSection(itemNamespacedKey +
                            "{" + potionEffect.getName() + "}");
                }
                if (cs == null) continue;

                int cd = getCooldownValueFromSection(cs, world);
                if (cd < 0 || cd > cooldown) {
                    cooldown = cd;
                    itemSection = cs;
                }
            }
            if (itemSection == null) return null;
        }

        if (!type.name().equalsIgnoreCase(itemSection.getString(Config.ITEM_COOLDOWN_SECTION_TYPE.getPath())))
            return null;

        cooldownResult.title = itemSection.getString(Config.ITEM_COOLDOWN_SECTION_TITLE.getPath());
        cooldownResult.cooldown = cooldown;
        return cooldownResult;
    }

    private int getCooldownValueFromSection(@NotNull ConfigurationSection itemSection, @NotNull World world) {
        ConfigurationSection worldSpecificCooldowns =
                itemSection.getConfigurationSection(Config.ITEM_COOLDOWN_SECTION_COOLDOWN.getPath());
        if (worldSpecificCooldowns == null) {
            return itemSection.getInt(Config.ITEM_COOLDOWN_SECTION_COOLDOWN.getPath());
        } else {
            return worldSpecificCooldowns.getInt(world.getName());
        }
    }

    private static class CooldownResult {
        private int cooldown;
        private String title;
    }

}
