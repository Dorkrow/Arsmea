package me.pablete1234.arsmea.util;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.util.BlockVector;

public class Vectors {

    public static BlockVector toBlockVector(Location location) {
        return new BlockVector(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    public static BlockVector toBlockVector(Block block) {
        return toBlockVector(block.getLocation());
    }

}
