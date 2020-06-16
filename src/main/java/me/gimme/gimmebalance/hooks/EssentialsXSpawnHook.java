package me.gimme.gimmebalance.hooks;

import com.earth2me.essentials.spawn.EssentialsSpawn;
import me.gimme.gimmecore.hook.PluginHook;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.NotNull;

public class EssentialsXSpawnHook extends PluginHook<EssentialsSpawn> {

    private static final String ESSENTIALS_SPAWN_NAME = "EssentialsXSpawn";

    private Plugin plugin;
    private Permission permission;

    public EssentialsXSpawnHook(Plugin plugin) {
        super(ESSENTIALS_SPAWN_NAME, plugin.getServer().getPluginManager());
        this.plugin = plugin;

        setupPermissions();
    }

    private boolean setupPermissions() {
        try {
            RegisteredServiceProvider<Permission> permissionProvider = plugin.getServer().getServicesManager().getRegistration(Permission.class);
            if (permissionProvider != null) {
                permission = permissionProvider.getProvider();
            }
            return (permission != null);
        } catch (NoClassDefFoundError e) {
            //e.printStackTrace();
            return false;
        }
    }

    /**
     * Returns the specified player's spawn location.
     *
     * @param player the player who's spawn location to get
     * @return the specified player's spawn location
     */
    @NotNull
    public Location getSpawn(@NotNull Player player) {
        if (hookedPlugin != null && permission != null)
            return hookedPlugin.getSpawn(permission.getPrimaryGroup(player));

        return getWorldSpawn();
    }

    /**
     * Returns the default spawn location.
     *
     * @return the default spawn location
     */
    @NotNull
    private Location getWorldSpawn() {
        for (World world : plugin.getServer().getWorlds()) {
            if (world.getEnvironment() != World.Environment.NORMAL) continue;
            return world.getSpawnLocation();
        }
        return plugin.getServer().getWorlds().get(0).getSpawnLocation();
    }

}
