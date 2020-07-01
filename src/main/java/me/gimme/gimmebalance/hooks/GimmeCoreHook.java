package me.gimme.gimmebalance.hooks;

import me.gimme.gimmecore.GimmeCore;
import me.gimme.gimmecore.hook.PluginHook;
import me.gimme.gimmecore.manager.WarmupActionManager;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class GimmeCoreHook extends PluginHook<GimmeCore> {
    private WarmupActionManager warmupActionManager;

    public GimmeCoreHook(@NotNull Plugin plugin) {
        super(GimmeCore.PLUGIN_NAME, plugin.getServer().getPluginManager());

        if (hookedPlugin != null) {
            this.warmupActionManager = hookedPlugin.getWarmupActionManager();
        } else {
            this.warmupActionManager = new WarmupActionManager(plugin);
        }
    }

    public WarmupActionManager getWarmupActionManager() {
        return warmupActionManager;
    }
}
