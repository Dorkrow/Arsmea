package me.pablete1234.arsmea.modules;

import java.util.List;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import me.pablete1234.arsmea.Module;
import me.pablete1234.arsmea.util.Config;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

public class DiscordBot implements Module {

    private static DiscordBot instance = null;
    private volatile IDiscordClient client = null;

    @Override
    public void load() {
        if (!Config.discord_enabled) return;
        try {
            client = new ClientBuilder().withToken(Config.discord_token).login();

            instance = this;
        } catch (DiscordException e) {
            Bukkit.getLogger().log(Level.WARNING, "Could not connect to discord, error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void unload() {
        try {
            if (client != null) client.logout();
        } catch (DiscordException e) {
            Bukkit.getLogger().log(Level.WARNING, "Could not disconnect from discord, error: " + e.getMessage());
            e.printStackTrace();
        }
        client = null;
        instance = null;
    }

    public static void sendMessage(String channelName, String message) {
        if (instance == null) return;
        try {
            getChannel(channelName, instance.client.getChannels()).sendMessage(ChatColor.stripColor(message));
        } catch (IllegalArgumentException | MissingPermissionsException | RateLimitException | DiscordException e) {
            Bukkit.getLogger().log(Level.WARNING, "Error sending '" + message + "' to '" + channelName + "' channel: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static IChannel getChannel(String name, List<IChannel> channels) {
        return channels.stream().filter(ch -> ch.getName().equalsIgnoreCase(name))
                .findFirst().orElseThrow(() -> new IllegalArgumentException("There is no channel that goes by the name of " + name));
    }

}
