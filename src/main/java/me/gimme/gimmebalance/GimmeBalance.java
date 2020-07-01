package me.gimme.gimmebalance;

import me.gimme.gimmebalance.actionbar.ActionBarSendingUtil;
import me.gimme.gimmebalance.balance.*;
import me.gimme.gimmebalance.commands.LogoutCommand;
import me.gimme.gimmebalance.config.Config;
import me.gimme.gimmebalance.hooks.EssentialsXSpawnHook;
import me.gimme.gimmebalance.event.callers.PlayerUseItemEventCaller;
import me.gimme.gimmebalance.hooks.GimmeCoreHook;
import me.gimme.gimmebalance.hooks.ProtocolLibHook;
import me.gimme.gimmebalance.language.LanguageManager;
import me.gimme.gimmebalance.language.Message;
import me.gimme.gimmebalance.player.CombatLogManager;
import me.gimme.gimmebalance.player.CombatTimerManager;
import me.gimme.gimmebalance.player.ItemCooldownManager;
import me.gimme.gimmebalance.balance.restriction.BrewRestriction;
import me.gimme.gimmebalance.balance.restriction.CraftRestriction;
import me.gimme.gimmebalance.balance.restriction.EnchantRestriction;
import me.gimme.gimmecore.scoreboard.TimerScoreboardManager;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;

public final class GimmeBalance extends JavaPlugin {

    public static final String PLUGIN_NAME = "GimmeBalance";

    private boolean combatLogNpcEnabled;

    private GimmeCoreHook gimmeCoreHook;
    private EssentialsXSpawnHook essentialsXSpawnHook;
    private LanguageManager languageManager;
    private TimerScoreboardManager timerScoreboardManager;
    private CombatTimerManager combatTimerManager;
    private LogoutCommand logoutCommand;
    private CombatLogManager combatLogManager;

    @Override
    public void onEnable() {
        initConfig();

        combatLogNpcEnabled = getConfig().getBoolean(Config.COMBAT_LOG_NPC_ENABLED.getPath());

        gimmeCoreHook = new GimmeCoreHook(this);
        essentialsXSpawnHook = new EssentialsXSpawnHook(this);
        languageManager = new LanguageManager(this);
        timerScoreboardManager = new TimerScoreboardManager(this, languageManager.get(Message.HEADER_TIMERS_SCOREBOARD).toString());
        combatTimerManager = new CombatTimerManager(getConfig(), languageManager, timerScoreboardManager);
        logoutCommand = new LogoutCommand(getConfig(), languageManager, combatTimerManager, gimmeCoreHook, !combatLogNpcEnabled);
        combatLogManager = new CombatLogManager(this, languageManager, combatTimerManager, logoutCommand, essentialsXSpawnHook);
        ActionBarSendingUtil.protocolLibHook = new ProtocolLibHook(this.getServer().getPluginManager());

        registerListeners();
        registerCommands();

        CraftRestriction.applyRestrictions(getServer(), getConfig());

        if (combatLogNpcEnabled) {
            double savePeriodInMinutes = getConfig().getInt(Config.COMBAT_LOG_NPC_KILLED_PLAYERS_SAVE_PERIOD.getPath());
            long savePeriodInTicks = Math.round(20 * 60 * savePeriodInMinutes);
            new BukkitRunnable() {
                @Override
                public void run() {
                    combatLogManager.saveKilledPlayers();
                }
            }.runTaskTimer(this, savePeriodInTicks, savePeriodInTicks);
        }
    }

    @Override
    public void onDisable() {
        if (combatLogNpcEnabled) {
            combatLogManager.saveKilledPlayers();
            combatLogManager.unloadNpcs();
        }
    }

    private void initConfig() {
        getConfig().options().copyDefaults(true);
        saveDefaultConfig();

        final String RECOMMENDED_CONFIG_PATH = "config(hardcore).yml";
        if (!new File(getDataFolder(), RECOMMENDED_CONFIG_PATH).exists()) saveResource(RECOMMENDED_CONFIG_PATH, false);
    }

    private void registerListeners() {
        boolean itemCooldownsEnabled = getConfig().getBoolean(Config.ITEM_COOLDOWNS_ENABLED.getPath());
        boolean combatTimerEnabled = getConfig().getDouble(Config.COMBAT_TIMER.getPath()) > 0;

        if (itemCooldownsEnabled || combatTimerEnabled) {
            if (getConfig().getBoolean(Config.USE_SCOREBOARD.getPath())) {
                registerListener(timerScoreboardManager);
            }
            if (itemCooldownsEnabled) {
                registerListener(new ItemCooldownManager(getConfig(), languageManager, timerScoreboardManager));
            }
            if (combatTimerEnabled) {
                registerListener(combatTimerManager);
                if (combatLogNpcEnabled) registerListener(combatLogManager);
            }
        }

        registerListener(new BrewRestriction(this, getConfig()));
        registerListener(new EnchantRestriction(getConfig(), languageManager));

        registerListener(new GeneralBalance(getConfig()));
        registerListener(new EntityDeathYieldModifier(this, getConfig()));
        registerListener(new OtherXPYieldModifier(getConfig()));
        registerListener(new DurabilityDamageModifier(getConfig()));
        registerListener(new PotionBalance(this, getConfig()));

        registerListener(new PlayerUseItemEventCaller());
    }

    private void registerListener(Listener listener) {
        getServer().getPluginManager().registerEvents(listener, this);
    }

    private void registerCommands() {
        PluginCommand logoutPluginCommand = getCommand(LogoutCommand.LOGOUT_COMMAND_NAME);
        assert logoutPluginCommand != null;
        logoutPluginCommand.setExecutor(logoutCommand);
    }

    /**
     * Adds a condition for when combat logging should not trigger a punishment.
     * @param condition a condition for when combat logging is be allowed
     */
    public void addAllowedCombatLogCondition(CombatLogManager.AllowedCondition condition) {
        combatLogManager.addAllowedCondition(condition);
    }

    /**
     * @return the manager that keeps track of players' combat timers
     */
    public CombatTimerManager getCombatTimerManager() {
        return combatTimerManager;
    }

}
