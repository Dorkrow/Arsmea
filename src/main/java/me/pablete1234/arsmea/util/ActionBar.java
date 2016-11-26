package me.pablete1234.arsmea.util;

import org.bukkit.entity.Player;

import java.lang.reflect.Method;


public class ActionBar {

    private static final String nms = "net.minecraft.server.v1_11_R1.";
    private static final String bukkit = "org.bukkit.craftbukkit.v1_11_R1.";

    private static Class<?> iChatBaseCl;
    private static Class<?> chatPacketCl;
    private static Class<?> textComponentCl;
    private static Class<?> craftPlayerCl;
    private static Method sendPacketMh;

    static {
        try {
            iChatBaseCl = Class.forName(nms + "IChatBaseComponent");
            chatPacketCl = Class.forName(nms + "PacketPlayOutChat");
            textComponentCl = Class.forName(nms + "ChatComponentText");
            craftPlayerCl = Class.forName(bukkit + "entity.CraftPlayer");

            sendPacketMh = Class.forName(nms + "PlayerConnection").getDeclaredMethod("sendPacket", Class.forName(nms + "Packet"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void sendChatPacket(Player player, String content) {
        if (player == null || content == null) return;
        try {
            Object chatPacket = ActionBar.chatPacketCl.getConstructor(iChatBaseCl, byte.class)
                    .newInstance(textComponentCl.getConstructor(String.class).newInstance(content), (byte) 2);
            Object craftPlayer = craftPlayerCl.cast(player);
            Object nmsPlayer = craftPlayer.getClass().getMethod("getHandle").invoke(craftPlayer);
            Object playerConnection = nmsPlayer.getClass().getField("playerConnection").get(nmsPlayer);

            sendPacketMh.invoke(playerConnection, chatPacket);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}