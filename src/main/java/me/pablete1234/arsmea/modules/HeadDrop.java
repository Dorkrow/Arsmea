package me.pablete1234.arsmea.modules;

import me.pablete1234.arsmea.ListenerModule;
import me.pablete1234.arsmea.util.Config;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

public class HeadDrop implements ListenerModule {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (!Config.dropHeadsOnDeath) return;
        Player p = event.getEntity();
        ItemStack skullItem = new ItemStack(Material.SKULL_ITEM, 1, (short) SkullType.PLAYER.ordinal());
        SkullMeta meta = (SkullMeta) skullItem.getItemMeta();
        meta.setDisplayName(p.getName());
        meta.setOwner(p.getName());
        skullItem.setItemMeta(meta);
        event.getDrops().add(skullItem);
    }

}
