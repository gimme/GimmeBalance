package me.gimme.gimmebalance.hooks;

import me.gimme.gimmecore.GimmeCore;
import me.gimme.gimmecore.hook.PluginHook;
import me.gimme.gimmecore.manager.WarmupActionManager;
import org.bukkit.plugin.PluginManager;
import org.jetbrains.annotations.NotNull;

public class GimmeCoreHook extends PluginHook<GimmeCore> {

    public GimmeCoreHook(@NotNull PluginManager pluginManager) {
        super(GimmeCore.PLUGIN_NAME, pluginManager);
    }

    public WarmupActionManager getWarmupActionManager() {
        return hookedPlugin.getWarmupActionManager();
    }

}
