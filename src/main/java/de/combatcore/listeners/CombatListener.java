package de.combatcore.listeners;

import de.combatcore.CombatCore;
import de.combatcore.combat.CombatManager;
import de.combatcore.config.ConfigManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.entity.Projectile;

/**
 * Applies combat tags when players fight, and punishes combat-logging.
 */
public class CombatListener implements Listener {

    private final CombatCore plugin;
    private final CombatManager combat;
    private final ConfigManager config;

    public CombatListener(CombatCore plugin, CombatManager combat, ConfigManager config) {
        this.plugin = plugin;
        this.combat = combat;
        this.config = config;
    }

    @EventHandler(ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent e) {
        Player victim = e.getEntity() instanceof Player p ? p : null;
        Player attacker = resolveAttacker(e);

        if (config.isTagPvpOnly()) {
            // Only tag when both sides are players
            if (victim == null || attacker == null) return;
        }

        if (attacker != null && config.isTagOnDamageDealt()) {
            combat.tag(attacker);
        }
        if (victim != null && config.isTagOnDamageReceived()) {
            combat.tag(victim);
        }
    }

    private Player resolveAttacker(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Player p) return p;
        if (e.getDamager() instanceof Projectile proj) {
            ProjectileSource src = proj.getShooter();
            if (src instanceof Player p) return p;
        }
        return null;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        if (!combat.isInCombat(player)) return;

        if (config.isPunishOnLogout()) {
            // Kill the player so their items drop where they logged
            player.setHealth(0.0);
            if (config.isBroadcastLogoutDeath()) {
                plugin.getServer().sendMessage(config.msgLogoutPunish(player.getName()));
            }
        }
        combat.clearSilent(player.getUniqueId());
    }
}
