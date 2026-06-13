package de.combatcore.listeners;

import de.combatcore.CombatCore;
import de.combatcore.combat.CombatManager;
import de.combatcore.config.ConfigManager;
import de.combatcore.regions.BarrierManager;
import de.combatcore.regions.Region;
import de.combatcore.regions.RegionManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

/**
 * Prevents combat-tagged players from entering a protected region.
 * The player runs into a client-side wall of BARRIER blocks at the border;
 * if they somehow slip a step in, their movement into the region is cancelled.
 */
public class RegionListener implements Listener {

    private final CombatCore plugin;
    private final CombatManager combat;
    private final RegionManager regions;
    private final ConfigManager config;
    private final BarrierManager barriers;

    // How close (blocks) to a region before we render its barrier wall.
    private static final int SHOW_DISTANCE = 6;

    public RegionListener(CombatCore plugin, CombatManager combat,
                          RegionManager regions, ConfigManager config,
                          BarrierManager barriers) {
        this.plugin = plugin;
        this.combat = combat;
        this.regions = regions;
        this.config = config;
        this.barriers = barriers;
    }

    @EventHandler(ignoreCancelled = true)
    public void onMove(PlayerMoveEvent e) {
        Location to = e.getTo();
        if (to == null) return;
        Location from = e.getFrom();
        if (from.getBlockX() == to.getBlockX()
                && from.getBlockY() == to.getBlockY()
                && from.getBlockZ() == to.getBlockZ()) return;

        Player player = e.getPlayer();

        if (!combat.isInCombat(player) || player.hasPermission("combatcore.bypass.regions")) {
            // Make sure any leftover wall is cleared once they're safe.
            barriers.clear(player);
            return;
        }

        // If they would step into a region they weren't already in, cancel it.
        Region target = regions.regionAt(to);
        if (target != null && regions.regionAt(from) == null) {
            e.setTo(from.clone());
            player.sendMessage(config.msgRegionBlocked());
        }

        // Render / refresh the barrier wall for the nearest region in range.
        Region near = nearestRegionInRange(to);
        if (near != null) {
            barriers.update(player, near);
        } else {
            barriers.clear(player);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent e) {
        Location to = e.getTo();
        if (to == null) return;
        Player player = e.getPlayer();
        if (!combat.isInCombat(player)) return;
        if (player.hasPermission("combatcore.bypass.regions")) return;

        if (regions.regionAt(to) != null && regions.regionAt(e.getFrom()) == null) {
            e.setCancelled(true);
            player.sendMessage(config.msgRegionBlocked());
        }
    }

    /** Finds a region whose border is within SHOW_DISTANCE of the location. */
    private Region nearestRegionInRange(Location loc) {
        for (Region r : regions.all()) {
            if (loc.getWorld() == null) continue;
            if (!r.getWorldName().equals(loc.getWorld().getName())) continue;
            int x = loc.getBlockX(), y = loc.getBlockY(), z = loc.getBlockZ();
            if (x >= r.getMinX() - SHOW_DISTANCE && x <= r.getMaxX() + SHOW_DISTANCE
                    && y >= r.getMinY() - SHOW_DISTANCE && y <= r.getMaxY() + SHOW_DISTANCE
                    && z >= r.getMinZ() - SHOW_DISTANCE && z <= r.getMaxZ() + SHOW_DISTANCE) {
                return r;
            }
        }
        return null;
    }
}
