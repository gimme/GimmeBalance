package me.gimme.gimmebalance.hooks;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLib;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import me.gimme.gimmecore.hook.PluginHook;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;

public class ProtocolLibHook extends PluginHook<ProtocolLib> {

    private static final String PLUGIN_NAME = "ProtocolLib";

    public ProtocolLibHook(@NotNull PluginManager pluginManager) {
        super(PLUGIN_NAME, pluginManager);
    }

    public boolean sendActionBar(@NotNull Player player, @NotNull String message) {
        if (hookedPlugin == null) return false;

        PacketContainer chat = new PacketContainer(PacketType.Play.Server.CHAT);
        chat.getChatTypes().write(0, EnumWrappers.ChatType.GAME_INFO);
        chat.getChatComponents().write(0, WrappedChatComponent.fromText(message));
        try {
            ProtocolLibrary.getProtocolManager().sendServerPacket(player, chat);
            return true;
        } catch (InvocationTargetException e) {
            return false;
        }
    }

}
