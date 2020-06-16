package me.gimme.gimmebalance.player;

import me.gimme.gimmebalance.commands.LogoutCommand;
import me.gimme.gimmebalance.config.Config;
import me.gimme.gimmebalance.hooks.EssentialsXSpawnHook;
import me.gimme.gimmebalance.language.LanguageManager;
import me.gimme.gimmebalance.language.Message;
import me.gimme.gimmecore.util.ConfigUtils;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.*;

public class CombatLogManager implements Listener {

    public interface AllowedCondition {
        boolean isCombatLogAllowed(@NotNull Player player);
    }

    private static final String OFFLINE_KILLED_PLAYERS_PATH = "data/offline_killed_players.json";
    private static final String PERSISTENT_DATA_TAG = "combat_log_player";

    private Plugin plugin;
    private FileConfiguration config;
    private LanguageManager languageManager;
    private CombatTimerManager combatTimerManager;
    private LogoutCommand logoutCommand;
    private EssentialsXSpawnHook essentialsXSpawnHook;

    private List<AllowedCondition> allowedConditions = new ArrayList<>();
    private Map<UUID, RemoveNpcTask> taskByPlayer = new HashMap<>();
    private Map<UUID, Long> offlineKilledPlayers;

    public CombatLogManager(Plugin plugin, LanguageManager languageManager, CombatTimerManager combatTimerManager,
                            LogoutCommand logoutCommand, EssentialsXSpawnHook essentialsXSpawnHook) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
        this.languageManager = languageManager;
        this.combatTimerManager = combatTimerManager;
        this.logoutCommand = logoutCommand;
        this.essentialsXSpawnHook = essentialsXSpawnHook;

        loadKilledPlayers();
    }

    /**
     * Adds a condition for when combat logging is allowed.
     *
     * @param condition the condition to add
     */
    public void addAllowedCondition(AllowedCondition condition) {
        allowedConditions.add(condition);
    }

    /**
     * Unloads all NPC entities from the world. This is important when the server stops since the {@link RemoveNpcTask}s
     * will be removed.
     */
    public void unloadNpcs() {
        taskByPlayer.values().forEach(task -> task.npc.remove());
    }

    /**
     * Saves all players that have been killed while offline to the data folder.
     */
    public void saveKilledPlayers() {
        double daysTimeout = config.getDouble(Config.COMBAT_LOG_NPC_KILLED_PLAYERS_TIMEOUT.getPath());
        if (daysTimeout > 0.000001) { // Remove timed out entries
            long millisecondsPerDay = 1000 * 60 * 60 * 24;
            long currentTimeMillis = System.currentTimeMillis();

            offlineKilledPlayers.values().removeIf((value) -> currentTimeMillis - value > daysTimeout * millisecondsPerDay);
        }

        try {
            ConfigUtils.saveToJson(plugin.getDataFolder(), OFFLINE_KILLED_PLAYERS_PATH, offlineKilledPlayers);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadKilledPlayers() {
        try {
            this.offlineKilledPlayers = ConfigUtils.loadFromJson(plugin.getDataFolder(), OFFLINE_KILLED_PLAYERS_PATH);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (offlineKilledPlayers == null) offlineKilledPlayers = new HashMap<>();
    }

    /**
     * Spawns a dummy npc if the player is combat logging. Has to happen before the player is removed from the
     * safe-logging list in the logout command.
     */
    @EventHandler(priority = EventPriority.NORMAL)
    private void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode().equals(GameMode.CREATIVE) || player.getGameMode().equals(GameMode.SPECTATOR)) return;
        if (logoutCommand.isSafeLogging(player.getUniqueId())) return;
        for (AllowedCondition condition : allowedConditions) {
            if (condition.isCombatLogAllowed(player)) return;
        }

        Entity npc = spawnNpc(player);

        int secondsDelay = config.getInt(Config.COMBAT_LOG_NPC_DURATION.getPath());
        new RemoveNpcTask(player, npc).start(secondsDelay * 20 + combatTimerManager.getCombatTicksLeft(player.getUniqueId()));
    }

    /**
     * Simulates the death of a player when their combat-log-npc dies.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    private void onNpcDeath(EntityDeathEvent event) {
        if (!event.getEntityType().equals(EntityType.valueOf(config.getString(Config.COMBAT_LOG_NPC_TYPE.getPath()))))
            return;
        Entity entity = event.getEntity();

        PersistentDataContainer persistentData = entity.getPersistentDataContainer();
        @Nullable String playerIdString = persistentData.get(new NamespacedKey(plugin, PERSISTENT_DATA_TAG), PersistentDataType.STRING);
        if (playerIdString == null) return;
        UUID player = UUID.fromString(playerIdString);

        RemoveNpcTask task = taskByPlayer.get(player);
        if (task == null) return;

        event.setDroppedExp(task.xp);
        event.getDrops().clear();
        for (ItemStack item : task.inventory) {
            if (item == null) continue;
            entity.getWorld().dropItemNaturally(entity.getLocation(), item);
        }

        task.cancel();
        offlineKilledPlayers.put(player, System.currentTimeMillis());

        Bukkit.getPluginManager().callEvent(new PlayerDeathEvent(task.player, new ArrayList<>(), 0,
                null)); //TODO check if this works
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        RemoveNpcTask task = taskByPlayer.get(player.getUniqueId());
        if (task != null) task.finish();

        if (!offlineKilledPlayers.containsKey(player.getUniqueId())) return;
        offlineKilledPlayers.remove(player.getUniqueId());

        player.getInventory().clear();
        player.setLevel(0);
        player.setExp(0);
        player.setHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
        player.setFoodLevel(20);
        player.setSaturation(5);
        player.setFireTicks(0);
        player.setTicksLived(1);
        player.getActivePotionEffects().forEach(potionEffect -> player.removePotionEffect(potionEffect.getType()));

        player.teleport(essentialsXSpawnHook.getSpawn(player));

        player.sendMessage(languageManager.get(Message.KILLED_WHILE_OFFLINE).toString());
    }

    private Entity spawnNpc(@NotNull Player player) {
        LivingEntity npc = (LivingEntity) player.getWorld().spawnEntity(player.getLocation(),
                EntityType.valueOf(config.getString(Config.COMBAT_LOG_NPC_TYPE.getPath())));
        // Copy the player's movement. Doesn't work if LivingEntity#setAi(false) is used.
        //npc.setVelocity(player.getVelocity());
        //npc.setFallDistance(player.getFallDistance());

        EntityEquipment equipment = npc.getEquipment();
        EntityEquipment playerEquipment = player.getEquipment();
        if (equipment != null && playerEquipment != null) {
            equipment.setArmorContents(playerEquipment.getArmorContents());
            equipment.setItemInMainHand(player.getInventory().getItemInMainHand());
            equipment.setItemInOffHand(player.getInventory().getItemInOffHand());

            equipment.setItemInMainHandDropChance(0);
            equipment.setItemInOffHandDropChance(0);
            equipment.setHelmetDropChance(0);
            equipment.setChestplateDropChance(0);
            equipment.setLeggingsDropChance(0);
            equipment.setBootsDropChance(0);
        }

        npc.setCustomName(ChatColor.DARK_RED + player.getDisplayName());
        npc.setCustomNameVisible(true);

        npc.setSilent(true);
        npc.setAI(false);

        double health = player.getHealth();
        if (!config.getBoolean(Config.COMBAT_LOG_NPC_COPY_PLAYER_HEALTH.getPath())) {
            health = Math.max(config.getInt(Config.COMBAT_LOG_NPC_HEALTH.getPath()), 1);
        }
        AttributeInstance maxHealthAttribute = npc.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        assert maxHealthAttribute != null;
        maxHealthAttribute.setBaseValue(health);
        npc.setHealth(health);

        //AttributeInstance speedAttribute = npc.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
        //assert speedAttribute != null;
        //speedAttribute.setBaseValue(0);


        PersistentDataContainer persistentData = npc.getPersistentDataContainer();
        persistentData.set(new NamespacedKey(plugin, PERSISTENT_DATA_TAG), PersistentDataType.STRING, player.getUniqueId().toString());

        return npc;
    }

    private class RemoveNpcTask extends BukkitRunnable {
        private Player player;
        private UUID playerId;
        private Entity npc;
        private ItemStack[] inventory;
        private int xp;

        private RemoveNpcTask(@NotNull Player player, @NotNull Entity npc) {
            this.player = player;
            this.playerId = player.getUniqueId();
            this.npc = npc;
            this.inventory = player.getInventory().getContents();
            this.xp = Math.min(player.getLevel() * 7, 100);
        }

        @Override
        public void run() {
            finish();
        }

        private void finish() {
            npc.remove();
            cancel();
        }

        @Override
        public void cancel() {
            if (!isCancelled()) super.cancel();
            taskByPlayer.remove(playerId);
        }

        private void start(int delay) {
            taskByPlayer.put(playerId, this);
            runTaskLater(plugin, delay);
        }
    }

}
