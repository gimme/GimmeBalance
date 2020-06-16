package me.gimme.gimmebalance.event;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a player uses a potion (both thrown and consumed).
 */
public class PlayerUsePotionEvent extends PlayerUseItemEvent {

    private PotionMeta potionMeta;

    public PlayerUsePotionEvent(@NotNull Player player, @NotNull ItemStack item, @NotNull Type type, @NotNull PotionMeta potionMeta) {
        super(player, item, type);
        this.potionMeta = potionMeta;
    }

    /**
     * @return the potion meta of the used potion.
     */
    @NotNull
    public PotionMeta getPotionMeta() {
        return potionMeta;
    }

}
