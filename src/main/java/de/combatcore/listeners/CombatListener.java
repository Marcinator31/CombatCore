package de.combatcore.listeners;

import de.combatcore.CombatCore;
import de.combatcore.combat.CombatManager;
import de.combatcore.config.ConfigManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.entity.Projectile;

import java.util.UUID;

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

        // Remember the pairing so we can release the survivor if the other
        // player logs out / dies.
        if (attacker != null && victim != null) {
            combat.setOpponents(attacker.getUniqueId(), victim.getUniqueId());
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
    public void onDeath(PlayerDeathEvent e) {
        Player victim = e.getEntity();
        UUID victimId = victim.getUniqueId();

        // Figure out the opponent BEFORE clearing (clearing wipes the pairing).
        UUID opponentId = combat.getOpponent(victimId);

        // The dead player leaves combat (no message — they're dead).
        combat.clearSilent(victimId);

        // Release the killer, or failing that the tracked opponent.
        Player killer = victim.getKiller();
        if (killer != null && !killer.equals(victim)) {
            combat.clear(killer);
        } else if (opponentId != null) {
            releaseOpponent(opponentId);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        if (!combat.isInCombat(player)) return;

        // Grab the opponent before any clearing happens.
        UUID opponentId = combat.getOpponent(player.getUniqueId());

        if (config.isPunishOnLogout()) {
            // Kill the player so their items drop where they logged
            player.setHealth(0.0);
            if (config.isBroadcastLogoutDeath()) {
                plugin.getServer().sendMessage(config.msgLogoutPunish(player.getName()));
            }
        }
        combat.clearSilent(player.getUniqueId());

        // The player they were fighting is no longer in danger — release them.
        if (opponentId != null) {
            releaseOpponent(opponentId);
        }
    }

    /** Ends combat for the opponent if they are still online and tagged. */
    private void releaseOpponent(UUID opponentId) {
        Player opponent = plugin.getServer().getPlayer(opponentId);
        if (opponent != null && combat.isInCombat(opponent)) {
            combat.clear(opponent);
        } else {
            combat.clearSilent(opponentId);
        }
    }
}
