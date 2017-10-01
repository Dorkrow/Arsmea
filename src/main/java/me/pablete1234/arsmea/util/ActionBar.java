package me.pablete1234.arsmea.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;


public class ActionBar {

    private static final String nms;
    private static final String bukkit;
    
    private static Constructor<?> chatPacketCon;
    private static Constructor<?> textComponentCon;
    private static Object chatMessageType;
    private static Class<?> craftPlayerCl;
    private static Method sendPacketMh;
    
    static {
        String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        nms = "net.minecraft.server." + version + ".";
        bukkit = "org.bukkit.craftbukkit." + version + ".";
        try {
            Class<?> chatMessageTypeCl = Class.forName(nms + "ChatMessageType");
            chatPacketCon = Class.forName(nms + "PacketPlayOutChat").getConstructor(Class.forName(nms + "IChatBaseComponent"), chatMessageTypeCl);
            textComponentCon = Class.forName(nms + "ChatComponentText").getConstructor(String.class);
            chatMessageType = Class.forName(nms + "ChatMessageType").getMethod("a", byte.class).invoke(null, (byte) 2);
            craftPlayerCl = Class.forName(bukkit + "entity.CraftPlayer");
            sendPacketMh = Class.forName(nms + "PlayerConnection").getDeclaredMethod("sendPacket", Class.forName(nms + "Packet"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void sendChatPacket(Player player, String content) {
        if (player == null || content == null) return;
        try {
            Object chatPacket = ActionBar.chatPacketCon.newInstance(textComponentCon.newInstance(content), chatMessageType);
            Object nmsPlayer = craftPlayerCl.getMethod("getHandle").invoke(craftPlayerCl.cast(player));
            Object playerConnection = nmsPlayer.getClass().getField("playerConnection").get(nmsPlayer);

            sendPacketMh.invoke(playerConnection, chatPacket);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}