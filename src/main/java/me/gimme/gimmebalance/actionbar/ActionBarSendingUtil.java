package me.gimme.gimmebalance.actionbar;

import com.google.common.base.Strings;
import me.gimme.gimmebalance.hooks.ProtocolLibHook;
import org.bukkit.entity.Player;

public final class ActionBarSendingUtil {

    public static ProtocolLibHook protocolLibHook;

    /**
     * Sends an action bar message to the player.
     *
     * @param player the player to send the message to
     * @param message the message to send
     * @return if the message was sent successfully
     */
    public static void sendActionBar(Player player, String message) {
        if (Strings.isNullOrEmpty(message)) return;
        if (protocolLibHook != null && protocolLibHook.sendActionBar(player, message)) return;

        player.sendMessage(message);
    }

}
