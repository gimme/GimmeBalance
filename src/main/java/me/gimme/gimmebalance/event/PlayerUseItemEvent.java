package me.gimme.gimmebalance.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a player used an item, such as threw an Ender Pearl or ate an Apple.
 */
public class PlayerUseItemEvent extends PlayerEvent implements Cancellable {

    public enum Type {
        INTERACT,
        CONSUME,
        LAUNCH
    }

    private static final HandlerList handlers = new HandlerList();
    private boolean cancel = false;
    private ItemStack item;
    private Type type;

    public PlayerUseItemEvent(@NotNull Player player, @NotNull ItemStack item, @NotNull Type type) {
        super(player);
        this.item = item;
        this.type = type;
    }

    /**
     * @return the item the was used
     */
    @NotNull
    public ItemStack getItem() {
        return item;
    }

    /**
     * @return the type of way in which the item was used
     */
    @NotNull
    public Type getType() {
        return type;
    }

    /**
     * If the event is cancelled, the item will not be used.
     *
     * @return true if this event is cancelled
     */
    @Override
    public boolean isCancelled() {
        return cancel;
    }

    /**
     * If the event is cancelled, the item will not be used.
     *
     * @param cancel true if you wish to cancel this event
     */
    @Override
    public void setCancelled(boolean cancel) {
        this.cancel = cancel;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
