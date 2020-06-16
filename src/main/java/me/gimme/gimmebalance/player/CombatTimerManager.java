package me.gimme.gimmebalance.player;

import me.gimme.gimmebalance.actionbar.ActionBarSendingUtil;
import me.gimme.gimmebalance.config.Config;
import me.gimme.gimmebalance.language.LanguageManager;
import me.gimme.gimmebalance.language.Message;
import me.gimme.gimmecore.scoreboard.TimerScoreboardManager;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class CombatTimerManager implements Listener {

    private static double MILLIS_PER_TICK = 1000d / 20;

    private FileConfiguration config;
    private LanguageManager languageManager;
    private TimerScoreboardManager timerScoreboardManager;

    private Map<UUID, Long> inCombat = new HashMap<>();

    public CombatTimerManager(@NotNull FileConfiguration config, @NotNull LanguageManager languageManager,
                              @NotNull TimerScoreboardManager timerScoreboardManager) {
        this.config = config;
        this.languageManager = languageManager;
        this.timerScoreboardManager = timerScoreboardManager;
    }

    /**
     * Returns if a player with the specified id is currently in combat.
     *
     * @param player the player id to check
     * @return if a player with the specified id is currently in combat
     */
    public boolean isInCombat(@NotNull UUID player) {
        return inCombat.containsKey(player);
    }

    /**
     * Returns the amount of ticks left of the specified player's combat timer, or 0 if the player is not in combat.
     *
     * @param player the player id to check
     * @return the amount of ticks left of the specified player's combat timer, or 0 if the player is not in combat
     */
    public int getCombatTicksLeft(@NotNull UUID player) {
        Long combatStartTimeMillis = inCombat.get(player);
        if (combatStartTimeMillis == null) return 0;

        long millisSinceCombatStart = System.currentTimeMillis() - combatStartTimeMillis;
        long ticksSinceCombatStart = Math.round(millisSinceCombatStart / MILLIS_PER_TICK);

        return (int) (getCombatDuration() * 20 - ticksSinceCombatStart);
    }

    private int getCombatDuration() {
        return config.getInt(Config.COMBAT_TIMER.getPath());
    }

    /**
     * Starts combat timer for the specified player.
     *
     * @param player the player now in combat
     */
    private void startCombatTimer(@NotNull Player player) {
        String title = languageManager.get(Message.SCOREBOARD_TIMER_TITLE_COMBAT).toString();
        int duration = getCombatDuration();

        if (title == null) return;
        if (duration <= 0) return;

        if (!isInCombat(player.getUniqueId()))
            ActionBarSendingUtil.sendActionBar(player, languageManager.get(Message.COMBAT_TIMER_START).toString());

        inCombat.put(player.getUniqueId(), System.currentTimeMillis());

        int score = config.getInt(Config.SCOREBOARD_SCORE_COMBAT_TIMER.getPath());
        timerScoreboardManager.startPlayerTimer(player, title, duration, score,
                () -> {
                    inCombat.remove(player.getUniqueId());
                    ActionBarSendingUtil.sendActionBar(player, languageManager.get(Message.COMBAT_TIMER_END).toString());
                });
    }

    /**
     * Starts combat timers for players taking damage from other players.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    private void onPlayerTakeDamage(EntityDamageByEntityEvent event) {
        if (event.isCancelled()) return;
        if (!event.getEntityType().equals(EntityType.PLAYER)) return;
        if (!event.getDamager().getType().equals(EntityType.PLAYER)) return;

        startCombatTimer((Player) event.getEntity());
    }

    /**
     * Allows only whitelisted commands or prevents only blacklisted commands (depending on what is used in the config).
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    private void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String command = event.getMessage();
        if (event.isCancelled()) return;
        if (!isInCombat(player.getUniqueId())) return;

        List<String> whitelist = config.getStringList(Config.COMBAT_COMMAND_WHITELIST.getPath());
        List<String> blacklist = config.getStringList(Config.COMBAT_COMMAND_BLACKLIST.getPath());
        if (whitelist.size() > 0) {
            for (String c : whitelist) {
                if (command.startsWith(c)) {
                    if (command.length() == c.length() || command.charAt(c.length()) == ' ') {
                        return;
                    }
                }
            }
        } else if (blacklist.size() > 0) {
            for (String c : blacklist) {
                if (command.startsWith(c)) {
                    if (command.length() == c.length() || command.charAt(c.length()) == ' ') {
                        player.sendMessage(languageManager.get(Message.ERROR_IN_COMBAT).toString());
                        event.setCancelled(true);
                        return;
                    }
                }
            }
            return;
        }

        player.sendMessage(languageManager.get(Message.ERROR_IN_COMBAT).toString());
        event.setCancelled(true);
    }

    /**
     * Disables breaking blocks while in combat (if turned on in the config).
     */
    @EventHandler(priority = EventPriority.LOWEST)
    private void onBlockBreak(BlockBreakEvent event) {
        if (event.isCancelled()) return;
        if (!config.getBoolean(Config.COMBAT_BLOCK_BREAK_DISABLED.getPath())) return;
        if (!isInCombat(event.getPlayer().getUniqueId())) return;
        event.setCancelled(true);
    }

    /**
     * Disables placing blocks while in combat (if turned on in the config).
     */
    @EventHandler(priority = EventPriority.LOWEST)
    private void onBlockPlace(BlockPlaceEvent event) {
        if (event.isCancelled()) return;
        if (!config.getBoolean(Config.COMBAT_BLOCK_PLACE_DISABLED.getPath())) return;
        if (!isInCombat(event.getPlayer().getUniqueId())) return;
        event.setCancelled(true);
    }

    /**
     * Disables the use of end/nether portals while in combat (if turned on in the config).
     */
    @EventHandler(priority = EventPriority.LOWEST)
    private void onPlayerTeleport(PlayerTeleportEvent event) {
        if (event.isCancelled()) return;
        if (!isInCombat(event.getPlayer().getUniqueId())) return;
        if (event.getCause().equals(PlayerTeleportEvent.TeleportCause.END_PORTAL) &&
                config.getBoolean(Config.COMBAT_PORTAL_END_DISABLED.getPath())) {
            event.setCancelled(true);
        } else if (event.getCause().equals(PlayerTeleportEvent.TeleportCause.NETHER_PORTAL) &&
                config.getBoolean(Config.COMBAT_PORTAL_NETHER_DISABLED.getPath())) {
            event.setCancelled(true);
        }
    }

}
