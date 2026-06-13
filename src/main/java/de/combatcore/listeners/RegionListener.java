package de.combatcore.listeners;

import de.combatcore.CombatCore;
import de.combatcore.combat.CombatManager;
import de.combatcore.config.ConfigManager;
import de.combatcore.regions.Region;
import de.combatcore.regions.RegionManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.util.Vector;

/**
 * Prevents combat-tagged players from walking/teleporting into a protected region
 * (so they can't run into a safe zone mid-fight). Pushes them back out.
 */
public class RegionListener implements Listener {

    private final CombatCore plugin;
    private final CombatManager combat;
    private final RegionManager regions;
    private final ConfigManager config;

    public RegionListener(CombatCore plugin, CombatManager combat,
                          RegionManager regions, ConfigManager config) {
        this.plugin = plugin;
        this.combat = combat;
        this.regions = regions;
        this.config = config;
    }

    @EventHandler(ignoreCancelled = true)
    public void onMove(PlayerMoveEvent e) {
        Location to = e.getTo();
        if (to == null) return;
        // Only check when the block position actually changed (perf)
        Location from = e.getFrom();
        if (from.getBlockX() == to.getBlockX()
                && from.getBlockY() == to.getBlockY()
                && from.getBlockZ() == to.getBlockZ()) return;

        Player player = e.getPlayer();
        if (!combat.isInCombat(player)) return;
        if (player.hasPermission("combatcore.bypass.regions")) return;

        Region target = regions.regionAt(to);
        if (target == null) return;
        // Allow if they were already inside (don't trap them); only block entering
        if (regions.regionAt(from) != null) return;

        // Cancel and nudge them back
        player.sendMessage(config.msgRegionBlocked());
        Vector push = from.toVector().subtract(to.toVector()).normalize().multiply(0.6);
        push.setY(0);
        Location back = from.clone();
        e.setTo(back);
        player.setVelocity(push);
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
}
