package me.gimme.gimmebalance.config;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum Config {

    COMBAT("combat"),
    COMBAT_TIMER("timer", COMBAT),
    COMBAT_COMMAND_WHITELIST("command-whitelist", COMBAT),
    COMBAT_COMMAND_BLACKLIST("command-blacklist", COMBAT),
    COMBAT_BLOCK_BREAK_DISABLED("block-break-disabled", COMBAT),
    COMBAT_BLOCK_PLACE_DISABLED("block-place-disabled", COMBAT),
    COMBAT_PORTAL_END_DISABLED("portal-end-disabled", COMBAT),
    COMBAT_PORTAL_NETHER_DISABLED("portal-nether-disabled", COMBAT),
    COMBAT_LOG("combat-log", COMBAT),
    COMBAT_LOG_NPC("npc", COMBAT_LOG),
    COMBAT_LOG_NPC_ENABLED("enabled", COMBAT_LOG_NPC),
    COMBAT_LOG_NPC_DURATION("duration", COMBAT_LOG_NPC),
    COMBAT_LOG_NPC_TYPE("entity-type", COMBAT_LOG_NPC),
    COMBAT_LOG_NPC_COPY_PLAYER_HEALTH("copy-player-health", COMBAT_LOG_NPC),
    COMBAT_LOG_NPC_HEALTH("health", COMBAT_LOG_NPC),
    COMBAT_LOG_NPC_KILLED_PLAYERS_SAVE_PERIOD("killed-players-save-period", COMBAT_LOG_NPC),
    COMBAT_LOG_NPC_KILLED_PLAYERS_TIMEOUT("killed-players-timeout", COMBAT_LOG_NPC),

    ITEM_COOLDOWNS_ENABLED("item-cooldowns-enabled"),
    ITEM_COOLDOWN("item-cooldown"),
    ITEM_COOLDOWN_SECTION_COOLDOWN("cooldown"),
    ITEM_COOLDOWN_SECTION_TYPE("type"),
    ITEM_COOLDOWN_SECTION_TITLE("title"),

    POTION_DURATION("potion-duration"),
    POTION_REGEN_TICK_SUCCESS_RATE("potion-health-regen-tick-success-rate"),
    POTION_INSTANT_HEALTH_MODIFIER("potion-instant-health-modifier"),
    POTION_INSTANT_HEALTH_MAX("potion-instant-health-max"),

    ITEM_DROP_RATE_MULTIPLIER("item-drop-rate-multiplier"),
    ITEM_DROP_RATE_MULTIPLIER_DEFAULT("default", ITEM_DROP_RATE_MULTIPLIER),
    ITEM_DROP_RATE_MULTIPLIER_ENTITY("entity", ITEM_DROP_RATE_MULTIPLIER),
    ITEM_DROP_RATE_MULTIPLIER_SPAWNER_DEFAULT("spawner-default", ITEM_DROP_RATE_MULTIPLIER),
    ITEM_DROP_RATE_MULTIPLIER_SPAWNER_ENTITY("spawner-entity", ITEM_DROP_RATE_MULTIPLIER),

    XP_DROP_MULTIPLIER("xp-drop-multiplier"),
    XP_DROP_MULTIPLIER_DEFAULT("default", XP_DROP_MULTIPLIER),
    XP_DROP_MULTIPLIER_ENTITY("entity", XP_DROP_MULTIPLIER),
    XP_DROP_MULTIPLIER_SPAWNER_DEFAULT("spawner-default", XP_DROP_MULTIPLIER),
    XP_DROP_MULTIPLIER_SPAWNER_ENTITY("spawner-entity", XP_DROP_MULTIPLIER),

    MINING_XP_DROP_MULTIPLIER("mining-xp-drop-multiplier"),
    MINING_XP_DROP_MULTIPLIER_DEFAULT("default", MINING_XP_DROP_MULTIPLIER),
    MINING_XP_DROP_MULTIPLIER_BLOCK("block", MINING_XP_DROP_MULTIPLIER),

    OTHER_XP_MULTIPLIERS("other-xp-multipliers"),
    OTHER_XP_MULTIPLIERS_BREEDING("breeding", OTHER_XP_MULTIPLIERS),
    OTHER_XP_MULTIPLIERS_FISHING("fishing", OTHER_XP_MULTIPLIERS),
    OTHER_XP_MULTIPLIERS_FURNACE("furnace", OTHER_XP_MULTIPLIERS),

    DURABILITY_DAMAGE_MULTIPLIER("durability-damage-multiplier"),
    DURABILITY_DAMAGE_MULTIPLIER_DEFAULT("default", DURABILITY_DAMAGE_MULTIPLIER),
    DURABILITY_DAMAGE_MULTIPLIER_ITEM("item", DURABILITY_DAMAGE_MULTIPLIER),

    PEARL_THOUGH_GATE("pearl-through-gate"),
    PEARL_DAMAGE_DISABLED("pearl-damage-disabled"),
    BREWING_BLAZE_POWDER_FUEL_POWER("brewing-blaze-powder-fuel-power"),

    UNCRAFTABLE("uncraftable"),
    UNBREWABLE("unbrewable"),
    ENCHANTMENT_LIMITS("enchantment-limits"),

    USE_SCOREBOARD("use-scoreboard"),
    SCOREBOARD_SCORE("scoreboard-score"),
    SCOREBOARD_SCORE_COMBAT_TIMER("combat-timer", SCOREBOARD_SCORE),
    SCOREBOARD_SCORE_ITEM_COOLDOWNS("item-cooldowns", SCOREBOARD_SCORE);


    private final String key;
    private final Config parent;

    Config(@NotNull String key) {
        this(key, null);
    }

    Config(@NotNull String key, @Nullable Config parent) {
        this.key = key;
        this.parent = parent;
    }

    @NotNull
    public String getPath() {
        return (parent == null ? "" : (parent.getPath() + ".")) + key;
    }

    @NotNull
    @Override
    public String toString() {
        return getPath();
    }

}
