package de.combatcore.regions;

import de.combatcore.CombatCore;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Shows client-side BARRIER blocks along the faces of a region that a
 * combat-tagged player is approaching, so they hit an invisible wall instead
 * of walking in. The real world is never modified — only fake block packets
 * are sent to that one player, and they are reverted when no longer needed.
 */
public class BarrierManager {

    private final CombatCore plugin;

    // Per player: the set of block locations we currently show as barriers.
    private final Map<UUID, Set<Location>> shown = new ConcurrentHashMap<>();

    // How far (in blocks) around the player we render the wall.
    private static final int RADIUS = 4;
    // How high above/below the player's feet we render the wall.
    private static final int HEIGHT = 3;

    public BarrierManager(CombatCore plugin) {
        this.plugin = plugin;
    }

    /**
     * Refreshes the barrier wall for a player based on the nearest region border.
     * Only renders barrier blocks on the boundary faces of the region near the player.
     */
    public void update(Player player, Region region) {
        World world = player.getWorld();
        Location feet = player.getLocation();
        int px = feet.getBlockX();
        int py = feet.getBlockY();
        int pz = feet.getBlockZ();

        Set<Location> newWall = new HashSet<>();

        for (int x = px - RADIUS; x <= px + RADIUS; x++) {
            for (int z = pz - RADIUS; z <= pz + RADIUS; z++) {
                for (int y = py - 1; y <= py + HEIGHT; y++) {
                    Location loc = new Location(world, x, y, z);
                    // A border block is one that is INSIDE the region but adjacent
                    // to the outside — i.e. the shell the player would pass through.
                    if (region.contains(loc) && isBorder(region, x, y, z)) {
                        newWall.add(loc);
                    }
                }
            }
        }

        Set<Location> old = shown.getOrDefault(player.getUniqueId(), new HashSet<>());

        // Revert blocks that are no longer part of the wall.
        for (Location loc : old) {
            if (!newWall.contains(loc)) {
                sendRealBlock(player, loc);
            }
        }

        // Send barrier blocks for the new wall.
        for (Location loc : newWall) {
            player.sendBlockChange(loc, Material.BARRIER.createBlockData());
        }

        shown.put(player.getUniqueId(), newWall);
    }

    /** True if the block sits on the outer shell of the region (1-block thick). */
    private boolean isBorder(Region region, int x, int y, int z) {
        return x == region.getMinX() || x == region.getMaxX()
                || z == region.getMinZ() || z == region.getMaxZ()
                || y == region.getMinY() || y == region.getMaxY();
    }

    /** Removes all fake barriers for a player, restoring the real blocks. */
    public void clear(Player player) {
        Set<Location> old = shown.remove(player.getUniqueId());
        if (old == null) return;
        if (!player.isOnline()) return;
        for (Location loc : old) {
            sendRealBlock(player, loc);
        }
    }

    private void sendRealBlock(Player player, Location loc) {
        if (loc.getWorld() == null) return;
        player.sendBlockChange(loc, loc.getBlock().getBlockData());
    }

    public void clearAll() {
        for (UUID id : shown.keySet()) {
            Player p = plugin.getServer().getPlayer(id);
            if (p != null) clear(p);
        }
        shown.clear();
    }
}
