package de.combatcore.listeners;

import de.combatcore.CombatCore;
import de.combatcore.combat.CombatManager;
import de.combatcore.config.ConfigManager;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.entity.Projectile;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

/**
 * Blocks configured items from being used in combat, plus dedicated handling
 * for tridents, ender pearls, chorus fruit teleports and elytra gliding.
 *
 * Runs at LOWEST priority and does not ignore cancelled events so it always
 * gets the first say. Bypass only applies when the permission is explicitly set
 * (so plain ops do not silently skip the block).
 */
public class ItemBlockListener implements Listener {

    private final CombatCore plugin;
    private final CombatManager combat;
    private final ConfigManager config;

    public ItemBlockListener(CombatCore plugin, CombatManager combat, ConfigManager config) {
        this.plugin = plugin;
        this.combat = combat;
        this.config = config;
    }

    /** True if this player's use should be blocked right now. */
    private boolean shouldBlock(Player player) {
        if (!combat.isInCombat(player)) return false;
        if (player.isPermissionSet("combatcore.bypass.items")
                && player.hasPermission("combatcore.bypass.items")) {
            return false;
        }
        return true;
    }

    private void deny(Player player, String what) {
        player.sendMessage(config.msgItemBlocked());
        if (config.isDebug()) {
            plugin.getLogger().info("[debug] Blocked item use '" + what + "' from " + player.getName());
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInteract(PlayerInteractEvent e) {
        if (e.getHand() != EquipmentSlot.HAND && e.getHand() != EquipmentSlot.OFF_HAND) return;
        Player player = e.getPlayer();
        if (!shouldBlock(player)) return;

        ItemStack item = e.getItem();
        if (item == null) return;
        Material type = item.getType();

        boolean isUse = e.getAction() == Action.RIGHT_CLICK_AIR
                || e.getAction() == Action.RIGHT_CLICK_BLOCK;
        if (!isUse) return;

        // Dedicated ender pearl toggle
        if (config.isBlockEnderPearl() && type == Material.ENDER_PEARL) {
            e.setCancelled(true);
            deny(player, type.name());
            return;
        }

        // Generic configured item list (e.g. TRIDENT, CHORUS_FRUIT)
        if (config.getBlockedItems().contains(type.name())) {
            e.setCancelled(true);
            deny(player, type.name());
        }
    }

    /**
     * Catches projectile launches that don't always go through onInteract:
     * ender pearls and thrown tridents. Identified by projectile type, with the
     * thrower resolved from the projectile's shooter.
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onLaunch(ProjectileLaunchEvent e) {
        Projectile projectile = e.getEntity();
        ProjectileSource source = projectile.getShooter();
        if (!(source instanceof Player player)) return;
        if (!shouldBlock(player)) return;

        // Map the projectile to the item that produced it.
        Material type = switch (projectile.getType()) {
            case ENDER_PEARL -> Material.ENDER_PEARL;
            case TRIDENT -> Material.TRIDENT;
            default -> null;
        };
        if (type == null) return;

        if (config.isBlockEnderPearl() && type == Material.ENDER_PEARL) {
            e.setCancelled(true);
            deny(player, type.name());
            return;
        }
        if (config.getBlockedItems().contains(type.name())) {
            e.setCancelled(true);
            deny(player, type.name());
        }
    }

    /** Blocks the teleport caused by eating a chorus fruit while in combat. */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onTeleport(PlayerTeleportEvent e) {
        if (e.getCause() != PlayerTeleportEvent.TeleportCause.CHORUS_FRUIT) return;
        Player player = e.getPlayer();
        if (!shouldBlock(player)) return;
        if (!config.getBlockedItems().contains("CHORUS_FRUIT")) return;

        e.setCancelled(true);
        deny(player, "CHORUS_FRUIT");
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onGlide(EntityToggleGlideEvent e) {
        if (!config.isBlockElytra()) return;
        if (!(e.getEntity() instanceof Player player)) return;
        if (!e.isGliding()) return; // only block starting to glide
        if (player.getGameMode() == GameMode.CREATIVE) return;
        if (!shouldBlock(player)) return;

        e.setCancelled(true);
        deny(player, "ELYTRA");
    }
}
