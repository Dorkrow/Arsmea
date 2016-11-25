package me.pablete1234.arsmea.modules;

import me.pablete1234.arsmea.Arsmea;
import me.pablete1234.arsmea.ListenerModule;
import me.pablete1234.arsmea.util.Config;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Skull;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.util.BlockVector;

import java.util.HashMap;
import java.util.Map;

public class HeadDrop implements ListenerModule {

    private Map<BlockVector, BlockState> removedBlocks = new HashMap<>();

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (!Config.dropHeadsOnDeath) return;
        Player p = event.getEntity();
        if (Config.placeHeadOnDeath) {
            Location loc = p.getLocation().add(0, 0.25, 0);
            Block block = loc.getBlock();
            removedBlocks.put(toBlockVector(loc), block.getState());
            block.setType(Material.SKULL);
            Skull state = (Skull) block.getState();
            state.setSkullType(SkullType.PLAYER);
            state.setOwningPlayer(p);
            ((org.bukkit.material.Skull) state.getData()).setFacingDirection(BlockFace.UP);
            state.update();
        } else {
            ItemStack skullItem = new ItemStack(Material.SKULL_ITEM, 1, (short) SkullType.PLAYER.ordinal());
            SkullMeta meta = (SkullMeta) skullItem.getItemMeta();
            meta.setDisplayName(p.getName());
            meta.setOwner(p.getName());
            skullItem.setItemMeta(meta);
            event.getDrops().add(skullItem);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerBreakBlock(BlockBreakEvent event) {
        BlockVector loc = toBlockVector(event.getBlock().getLocation());
        if (removedBlocks.containsKey(loc)) {
            BlockState oldState = removedBlocks.get(loc);
            Block block = event.getBlock();
            removedBlocks.remove(loc);
            Bukkit.getScheduler().scheduleSyncDelayedTask(Arsmea.instance(), () -> {
                block.setType(oldState.getType());
                BlockState newState = block.getState();
                newState.setData(oldState.getData());
                newState.update();
            });
        }
    }

    private static BlockVector toBlockVector(Location location) {
        return new BlockVector(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

}
