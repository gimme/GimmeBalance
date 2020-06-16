package me.gimme.gimmebalance.balance.restriction;

import me.gimme.gimmebalance.config.Config;
import org.bukkit.NamespacedKey;
import org.bukkit.Server;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;

public final class CraftRestriction {
    private CraftRestriction() {}

    /**
     * Removes the recipes for uncraftable items from the config.
     */
    public static void applyRestrictions(@NotNull Server server, @NotNull FileConfiguration config) {
        List<String> uncraftable = config.getStringList(Config.UNCRAFTABLE.getPath());

        for (String recipeKey : uncraftable) {
            server.removeRecipe(NamespacedKey.minecraft(recipeKey));
        }
    }
}
