package me.gimme.gimmebalance.util;

import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

public abstract class CountdownTimerTask extends BukkitRunnable {

    private Plugin plugin;
    private long seconds;

    protected CountdownTimerTask(Plugin plugin, long seconds) {
        this.plugin = plugin;
        this.seconds = seconds;
    }

    @Override
    public void run() {
        onCount();
        if (seconds-- <= 0) finish();
    }

    public void finish() {
        cancel();
        onFinish();
    }

    protected abstract void onCount();
    protected abstract void onFinish();

    @NotNull
    public CountdownTimerTask start() {
        runTaskTimer(plugin, 0, 20);
        return this;
    }

    public long getSeconds() {
        return seconds;
    }
}
