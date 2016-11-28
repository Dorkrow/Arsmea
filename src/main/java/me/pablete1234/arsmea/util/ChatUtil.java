package me.pablete1234.arsmea.util;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ChatUtil {

    private static String page(int index, int max) {
        return page(index, max, ChatColor.DARK_AQUA, ChatColor.AQUA);
    }

    private static String page(int index, int max, ChatColor chatColor, ChatColor numColor) {
        return chatColor + "(" + numColor + index + chatColor + " of " + numColor + max + chatColor + ")";
    }

    /**
     * Paginates a list of objects and displays them to the sender
     *
     * @param sender     Who to show the paginated result.
     * @param header     The header shown as title.
     * @param index      Page index, what page the sender wants to see.
     * @param streamSize The size of the stream, can't get it from the steam because that would consume it.
     * @param pageSize   The size of each page (usually 8).
     * @param stream     The stream of objects to paginate.
     * @param toString   A function to convert the objects to chat messages.
     */
    public static <T> void paginate(CommandSender sender, String header, int index, int streamSize, int pageSize,
                                    Stream<T> stream, Function<T, String> toString) {
        paginate(sender, header, index, streamSize, pageSize, stream, toString, -1, null);
    }

    public static <T> void paginate(CommandSender sender, String header, int index, int streamSize, int pageSize,
                                    Stream<T> stream, Function<T, String> toString, int mark, ChatColor markColor) {
        int pages = (int) Math.ceil((streamSize + (pageSize - 1)) / pageSize);
        List<String> page;
        try {
            int current = pageSize * (index - 1);
            page = new Indexer().index(paginate(stream, pageSize, index).map(toString), current, mark + 1, markColor).collect(Collectors.toList());
            if (page.size() == 0) throw new IllegalArgumentException();
        } catch (IllegalArgumentException e) {
            sender.sendMessage(ChatColor.RED + "Invalid page number specified! " + ChatColor.AQUA + pages + ChatColor.RED + " total pages.");
            return;
        }
        sender.sendMessage(Align.padMessage(header + " " + page(index, pages)));
        page.forEach(sender::sendMessage);
        sender.sendMessage(Align.getDash());
    }

    public static <T> Stream<T> paginate(Stream<T> stream, int pageSize, int index) {
        return stream.skip(pageSize * (index - 1)).limit(pageSize);
    }

    private static class Indexer {

        private int index;

        private Stream<String> index(Stream<String> stream, int index, int highlight, ChatColor markColor) {
            this.index = index;
            return stream.map(str -> {
                this.index++;
                return str.replace("${index}", "" + (this.index == highlight ? markColor : "") + this.index + "");
            });
        }

    }
}
