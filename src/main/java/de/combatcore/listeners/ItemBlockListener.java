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
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

/**
 * Blocks configured items from being used in combat, plus optional
 * dedicated toggles for elytra gliding and ender pearls.
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

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent e) {
        if (e.getHand() != EquipmentSlot.HAND && e.getHand() != EquipmentSlot.OFF_HAND) return;
        Player player = e.getPlayer();
        if (!combat.isInCombat(player)) return;
        if (player.hasPermission("combatcore.bypass.items")) return;

        ItemStack item = e.getItem();
        if (item == null) return;
        Material type = item.getType();

        boolean isUse = e.getAction() == Action.RIGHT_CLICK_AIR
                || e.getAction() == Action.RIGHT_CLICK_BLOCK;
        if (!isUse) return;

        // Dedicated ender pearl toggle
        if (config.isBlockEnderPearl() && type == Material.ENDER_PEARL) {
            e.setCancelled(true);
            player.sendMessage(config.msgItemBlocked());
            return;
        }

        // Generic configured item list (e.g. TRIDENT, CHORUS_FRUIT)
        if (config.getBlockedItems().contains(type.name())) {
            e.setCancelled(true);
            player.sendMessage(config.msgItemBlocked());
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onGlide(EntityToggleGlideEvent e) {
        if (!config.isBlockElytra()) return;
        if (!(e.getEntity() instanceof Player player)) return;
        if (!e.isGliding()) return; // only block starting to glide
        if (player.getGameMode() == GameMode.CREATIVE) return;
        if (!combat.isInCombat(player)) return;
        if (player.hasPermission("combatcore.bypass.items")) return;

        e.setCancelled(true);
        player.sendMessage(config.msgItemBlocked());
    }
}
