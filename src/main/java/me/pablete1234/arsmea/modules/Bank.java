package me.pablete1234.arsmea.modules;

import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import ee.ellytr.command.Command;
import ee.ellytr.command.CommandContext;
import ee.ellytr.command.NestedCommands;
import ee.ellytr.command.PlayerCommand;
import ee.ellytr.command.argument.Optional;
import me.pablete1234.arsmea.CommandHandlerModule;
import me.pablete1234.arsmea.ListenerModule;
import me.pablete1234.arsmea.util.ActionBar;
import me.pablete1234.arsmea.util.Config;
import me.pablete1234.arsmea.util.Vectors;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Bank implements ListenerModule, CommandHandlerModule {

    private static final String MONEY_FILE = "money.json";
    private static final String LOCATIONS_FILE = "locations.json";

    private static final String MONEY_NAME = ChatColor.translateAlternateColorCodes('`', Config.moneyName);

    private static List<BlockVector> locations;
    private static Map<UUID, Integer> money;

    private static List<BlockVector> getLocations() {
        return locations;
    }

    @Override
    public void load() {
        locations = unserialize(LOCATIONS_FILE, new TypeToken<List<BlockVector>>(){}, Lists.newArrayList());
        money = unserialize(MONEY_FILE, new TypeToken<Map<UUID, Integer>>(){}, new HashMap<>());
    }

    @Override
    public void unload() {
        serialize(LOCATIONS_FILE, locations);
        serialize(MONEY_FILE, money);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onClickBank(PlayerInteractEvent event) {
        if (!Config.banksEnabled || !event.getHand().equals(EquipmentSlot.HAND)
                || event.getClickedBlock() == null || !locations.contains(Vectors.toBlockVector(event.getClickedBlock()))) return;
        Player player = event.getPlayer();
        if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) putMoney(player);
        else if (event.getAction().equals(Action.LEFT_CLICK_BLOCK)) getMoney(player);
        sendBalance(player);
    }

    private static void putMoney(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        Integer val = getValue(item);
        if (getValue(item) == null) return;
        int original = item.getAmount(), stackRes = player.isSneaking() ? 0 : item.getAmount() - 1;
        if (stackRes <= 0) player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
        else item.setAmount(stackRes);
        addMoney(player.getUniqueId(), val * (original - stackRes));
    }

    private static void getMoney(Player player) {
        int amount = Math.min(player.isSneaking() ? 10 : 1, Math.max(getMoney(player.getUniqueId()), 0));
        if (amount > 0) {
            ItemStack item = getMoney(amount, 1);
            addMoney(player.getUniqueId(), -(amount - player.getInventory().addItem(item).values().stream().mapToInt(ItemStack::getAmount).sum()));
        }
    }

    private static void sendBalance(Player player) {
        ActionBar.sendChatPacket(player, ChatColor.translateAlternateColorCodes('`', Config.bankBalanceDisplayName)
                .replace("%money%", "" + getMoney(player.getUniqueId())));
    }

    private static void check(UUID uuid) {
        if (!money.containsKey(uuid)) {
            money.put(uuid, 0);
        }
    }

    private static void addMoney(UUID uuid, int mon) {
        check(uuid);
        money.put(uuid, money.get(uuid) + mon);
    }

    private static int getMoney(UUID uuid) {
        check(uuid);
        return money.get(uuid);
    }

    private static Integer getValue(ItemStack itemStack) {
        if (itemStack == null || !itemStack.hasItemMeta()
                || !itemStack.getItemMeta().hasLore()) return null;
        String lore = ChatColor.stripColor(itemStack.getItemMeta().getLore().get(0));
        return lore.startsWith("Value: ") ? Integer.parseInt(lore.replace("Value: ", "")) : null;
    }

    private static ItemStack getMoney(int quantity, int value) {
        ItemStack item = new ItemStack(Material.PAPER, quantity);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(MONEY_NAME);
        meta.setLore(Arrays.asList(ChatColor.translateAlternateColorCodes('`', "`r`aValue: `b" + value)));
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        meta.addEnchant(Enchantment.DURABILITY, 1, true);
        item.setItemMeta(meta);
        return item;
    }



    @Command(aliases = "money", description = "Give yourself money", permissions = "arsmea.money")
    @PlayerCommand()
    public static void money(CommandContext cmd, Integer quantity, @Optional(defaultValue = "1") Integer value) {
        ((Player) cmd.getSender()).getInventory().addItem(getMoney(quantity, value));
    }

    @Command(aliases = "bank", description = "Bank managing commands")
    @NestedCommands(BankChildCommands.class)
    public static void bank(CommandContext cmd) {
    }

    public static class BankChildCommands {

        @Command(aliases = {"add", "create"}, description = "Create a new bank where you are standing on", permissions = "arsmea.bank.add")
        @PlayerCommand()
        public static void add(CommandContext cmd) {
            locations.add(Vectors.toBlockVector(((Player) cmd.getSender()).getLocation()));
        }

        @Command(aliases = {"list"}, description = "List the banks", permissions = "arsmea.bank.list")
        @PlayerCommand()
        public static void list(CommandContext cmd, @Optional Integer rad) {
            Player sender = (Player) cmd.getSender();
            Vector loc = sender.getLocation().toVector();
            locations.stream().filter(vec -> rad == null || vec.distance(loc) <= rad).forEach(bank ->
                    sender.sendMessage("Bank:" + bank.getBlockX() + "," + bank.getBlockY() + "," + bank.getBlockZ()));
        }

        @Command(aliases = "remove", description = "Removes the bank you are standing on", permissions = "arsmea.bank.remove")
        @PlayerCommand()
        public static void remove(CommandContext cmd) {
            Player sender = (Player) cmd.getSender();
            BlockVector loc = Vectors.toBlockVector(sender.getLocation());
            locations.removeIf(vec -> vec.equals(loc));
        }

    }

}
