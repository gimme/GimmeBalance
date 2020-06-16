package me.gimme.gimmebalance.language;

import org.bukkit.plugin.Plugin;

public class LanguageManager extends me.gimme.gimmecore.language.LanguageManager {

    public LanguageManager(Plugin plugin) {
        super(plugin);
    }

    public Text get(Message message) {
        return super.get(message);
    }

}
