package me.pablete1234.arsmea.modules;

import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import me.pablete1234.arsmea.Arsmea;
import me.pablete1234.arsmea.ListenerModule;
import me.pablete1234.arsmea.util.Config;
import me.pablete1234.arsmea.util.Vectors;
import org.bukkit.Bukkit;
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
import org.bukkit.material.MaterialData;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class HeadDrop implements ListenerModule {

    private static final BlockFace[] FACES = {BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST, BlockFace.UP, BlockFace.DOWN};
    private static final List<Material> nonUsable = Arrays.asList(
            Material.ACACIA_DOOR,
            Material.BIRCH_DOOR,
            Material.DARK_OAK_DOOR,
            Material.JUNGLE_DOOR,
            Material.SPRUCE_DOOR,
            Material.WOODEN_DOOR,
            Material.IRON_DOOR_BLOCK);

    private static final String REMOVED_BLOCKS_FILE = "removedBlocks.json";

    private Map<BlockVector, MaterialData> removedBlocks;

    @Override
    public void load() {
        removedBlocks = unserialize(REMOVED_BLOCKS_FILE, new TypeToken<HashMap<BlockVector, MaterialData>>(){}, new HashMap<>());
    }

    @Override
    public void unload() {
        serialize(REMOVED_BLOCKS_FILE, removedBlocks);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (!Config.dropHeadsOnDeath) return;
        Player p = event.getEntity();
        Block block = null;
        if (Config.placeHeadOnDeath)
            block = findUsableBlock(p.getLocation().getBlock());
        if (block != null) {
            if (!block.getType().equals(Material.AIR))
                removedBlocks.put(Vectors.toBlockVector(block), block.getState().getData());
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
        BlockVector loc = Vectors.toBlockVector(event.getBlock().getLocation());
        if (removedBlocks.containsKey(loc)) {
            MaterialData oldData = removedBlocks.get(loc);
            Block block = event.getBlock();
            removedBlocks.remove(loc);
            Bukkit.getScheduler().scheduleSyncDelayedTask(Arsmea.instance(), () -> {
                block.setType(oldData.getItemType());
                BlockState newState = block.getState();
                newState.setData(oldData);
                newState.update();
            });
        }
    }

    private Block findUsableBlock(Block source) {
        if (isUsable(source)) return source;
        List<Block> nextScan = Lists.newLinkedList();
        List<Vector> alreadyScanned = Lists.newLinkedList();
        nextScan.add(source);
        alreadyScanned.add(source.getLocation().toVector());
        return findUsableBlock(nextScan, alreadyScanned);
    }

    private Block findUsableBlock(List<Block> scan, List<Vector> alreadyScanned) {
        if (scan.size() == 0) return null;
        List<Block> nextScan = new LinkedList<>();
        for (Block scanning : scan) {
            if (alreadyScanned.size() > 1024) {
                Bukkit.getConsoleSender().sendMessage("Maximim scan area (1024 blocks) was reached.");
                return null;
            }
            for (BlockFace face : FACES) {
                Block block = scanning.getRelative(face);
                if (alreadyScanned.contains(block.getLocation().toVector())) continue;
                if (isUsable(block)) {
                    return block;
                } else {
                    nextScan.add(block);
                    alreadyScanned.add(block.getLocation().toVector());
                }
            }
        }
        return findUsableBlock(nextScan, alreadyScanned);
    }

    private boolean isUsable(Block block) {
        return block.getState().getClass().getName().endsWith("CraftBlockState")
                && !nonUsable.contains(block.getType());
    }

}
