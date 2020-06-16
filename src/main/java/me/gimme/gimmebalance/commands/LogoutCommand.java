package me.gimme.gimmebalance.commands;

import me.gimme.gimmebalance.config.Config;
import me.gimme.gimmebalance.hooks.GimmeCoreHook;
import me.gimme.gimmebalance.language.LanguageManager;
import me.gimme.gimmebalance.language.Message;
import me.gimme.gimmebalance.language.Placeholder;
import me.gimme.gimmebalance.player.CombatTimerManager;
import me.gimme.gimmecore.manager.WarmupActionManager;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class LogoutCommand implements CommandExecutor, Listener {

    public static final String LOGOUT_COMMAND_NAME = "logout";

    private FileConfiguration config;
    private LanguageManager languageManager;
    private CombatTimerManager combatTimerManager;
    private WarmupActionManager warmupActionManager;
    private boolean instant;

    private Set<UUID> safeLoggingOut = new HashSet<>();

    public LogoutCommand(FileConfiguration config, LanguageManager languageManager, CombatTimerManager combatTimerManager,
                         GimmeCoreHook gimmeCoreHook, boolean instant) {
        this.config = config;
        this.languageManager = languageManager;
        this.combatTimerManager = combatTimerManager;
        this.warmupActionManager = gimmeCoreHook.getWarmupActionManager();
        this.instant = instant;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) return false;
        Player player = (Player) sender;

        if (instant) safeKickPlayer(player);
        else logout(player);

        return true;
    }

    /**
     * Returns if the player with the specified ID is safely logging out.
     *
     * @param player the id of the player that is safely logging out
     * @return if the player with the specified ID is safely logging out.
     */
    public boolean isSafeLogging(@NotNull UUID player) {
        return safeLoggingOut.contains(player);
    }

    private void logout(@NotNull Player player) {
        int secondsDelayAfterCombat = config.getInt(Config.COMBAT_LOG_NPC_DURATION.getPath());
        int delay = secondsDelayAfterCombat + Math.round(combatTimerManager.getCombatTicksLeft(player.getUniqueId()) / 20f);

        warmupActionManager.startWarmupAction(player, delay, true, true, true,
                (time) -> languageManager.get(Message.LOGGING_OUT_TITLE)
                        .replace(Placeholder.TIME, String.valueOf(time)).toString(),
                () -> safeKickPlayer(player));
    }

    private void safeKickPlayer(@NotNull Player player) {
        safeLoggingOut.add(player.getUniqueId());
        player.kickPlayer(languageManager.get(Message.SAFELY_LOGGED_OUT).toString());
    }

    /**
     * Remove safe-logging players from the list after log out.
     * Has to happen after any punishment listens to the {@link PlayerQuitEvent}.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    private void onPlayerQuit(PlayerQuitEvent event) {
        safeLoggingOut.remove(event.getPlayer().getUniqueId());
    }

}
