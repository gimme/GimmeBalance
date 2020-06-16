package me.gimme.gimmebalance.balance;

import me.gimme.gimmebalance.config.Config;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.Random;

/**
 * Modifies the item drop rates and XP yields of entity deaths.
 */
public class EntityDeathYieldModifier implements Listener {

    private static final String METADATA_KEY_SPAWN_REASON_SPAWNER = "spawn_reason_spawner";

    private Plugin plugin;
    private FileConfiguration config;
    private Random random = new Random();

    public EntityDeathYieldModifier(Plugin plugin, FileConfiguration config) {
        this.plugin = plugin;
        this.config = config;
    }

    /**
     * Marks spawned creatures if they were spawned by a spawner or not.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    private void onCreatureSpawn(CreatureSpawnEvent event) {
        event.getEntity().setMetadata(METADATA_KEY_SPAWN_REASON_SPAWNER, new FixedMetadataValue(plugin,
                event.getSpawnReason().equals(CreatureSpawnEvent.SpawnReason.SPAWNER)));
    }

    /**
     * Reduces entity item drop rates.
     */
    @EventHandler(priority = EventPriority.HIGH)
    private void onEntityDeathItemDrop(EntityDeathEvent event) {
        String entityNamespaceId = event.getEntityType().getKey().getKey();
        double dropRate;
        List<MetadataValue> spawnReasonMetaData = event.getEntity().getMetadata(METADATA_KEY_SPAWN_REASON_SPAWNER);
        boolean spawner = spawnReasonMetaData.size() > 0 && spawnReasonMetaData.get(0).asBoolean();

        if (spawner) {
            ConfigurationSection spawnerEntities = config.getConfigurationSection(Config.ITEM_DROP_RATE_MULTIPLIER_SPAWNER_ENTITY.getPath());
            if (spawnerEntities != null && spawnerEntities.contains(entityNamespaceId)) {
                dropRate = spawnerEntities.getDouble(entityNamespaceId);
            } else {
                dropRate = config.getDouble(Config.ITEM_DROP_RATE_MULTIPLIER_SPAWNER_DEFAULT.getPath());
            }
        } else {
            ConfigurationSection entities = config.getConfigurationSection(Config.ITEM_DROP_RATE_MULTIPLIER_ENTITY.getPath());
            if (entities != null && entities.contains(entityNamespaceId)) {
                dropRate = entities.getDouble(entityNamespaceId);
            } else {
                dropRate = config.getDouble(Config.ITEM_DROP_RATE_MULTIPLIER_DEFAULT.getPath());
            }
        }

        double roll = random.nextDouble();
        if (roll <= dropRate) return;

        event.getDrops().clear();
    }

    /**
     * Affects how much XP entities drop.
     */
    @EventHandler(priority = EventPriority.HIGH)
    private void onEntityDeathXPDrop(EntityDeathEvent event) {
        double multiplier;
        String entityNamespaceId = event.getEntityType().getKey().getKey();
        List<MetadataValue> spawnReasonMetaData = event.getEntity().getMetadata(METADATA_KEY_SPAWN_REASON_SPAWNER);
        boolean spawner = spawnReasonMetaData.size() > 0 && spawnReasonMetaData.get(0).asBoolean();

        if (spawner) {
            ConfigurationSection spawnerEntities = config.getConfigurationSection(Config.XP_DROP_MULTIPLIER_SPAWNER_ENTITY.getPath());
            if (spawnerEntities != null && spawnerEntities.contains(entityNamespaceId)) {
                multiplier = spawnerEntities.getDouble(entityNamespaceId);
            } else {
                multiplier = config.getDouble(Config.XP_DROP_MULTIPLIER_SPAWNER_DEFAULT.getPath());
            }
        } else {
            ConfigurationSection entities = config.getConfigurationSection(Config.XP_DROP_MULTIPLIER_ENTITY.getPath());
            if (entities != null && entities.contains(entityNamespaceId)) {
                multiplier = entities.getDouble(entityNamespaceId);
            } else {
                multiplier = config.getDouble(Config.XP_DROP_MULTIPLIER_DEFAULT.getPath());
            }
        }

        event.setDroppedExp((int) Math.round(event.getDroppedExp() * multiplier));
    }

}
