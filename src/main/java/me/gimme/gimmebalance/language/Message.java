package me.gimme.gimmebalance.language;

import me.gimme.gimmecore.language.LanguagePath;
import org.jetbrains.annotations.Nullable;

public enum Message implements LanguagePath {

    SCOREBOARD_TIMER_TITLE_COMBAT("scoreboard-timer-title-combat"),

    HEADER_TIMERS_SCOREBOARD("header-scoreboard-timers"),

    ERROR_IN_COMBAT("error-in-combat"),

    KILLED_WHILE_OFFLINE("killed-while-offline"),

    LOGOUT("logout"),
    LOGGING_OUT_TITLE("logging-out-title", LOGOUT),
    LOGGING_OUT("logging-out", LOGOUT),
    LOGOUT_CANCELED_BY_MOVEMENT("canceled-by-movement", LOGOUT),
    LOGOUT_CANCELED_BY_DAMAGE("canceled-by-damage", LOGOUT),
    SAFELY_LOGGED_OUT("safely-logged-out", LOGOUT),

    ACTION_BAR("action-bar"),
    COMBAT_TIMER_START("combat-timer-start", ACTION_BAR),
    COMBAT_TIMER_END("combat-timer-end", ACTION_BAR),
    ITEM_ON_COOLDOWN("item-on-cooldown", ACTION_BAR),
    ITEM_DISABLED("item-disabled", ACTION_BAR),
    ENCHANTMENT_RESTRICTED("enchantment-restricted", ACTION_BAR);

    private final String key;
    private final Message parent;

    Message(String key) {
        this(key, null);
    }

    Message(String key, @Nullable Message parent) {
        this.key = key;
        this.parent = parent;
    }

    public String getPath() {
        return (parent == null ? "" : (parent.getPath() + ".")) + key;
    }

    @Override
    public String toString() {
        return getPath();
    }

}
