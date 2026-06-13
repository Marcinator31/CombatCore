package de.combatcore.combat;

import de.combatcore.CombatCore;
import de.combatcore.config.ConfigManager;
import de.combatcore.regions.BarrierManager;
import de.combatcore.regions.RegionManager;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks combat state per player. A player is "in combat" until their
 * tag expires (System time based, so it survives lag without drift).
 */
public class CombatManager {

    private final CombatCore plugin;
    private final ConfigManager config;
    private final RegionManager regions;
    private BarrierManager barriers; // set after construction to avoid init cycle

    // playerId -> epoch millis when combat ends
    private final Map<UUID, Long> combatUntil = new ConcurrentHashMap<>();

    // playerId -> the last player they exchanged combat with (their opponent)
    private final Map<UUID, UUID> lastOpponent = new ConcurrentHashMap<>();

    public CombatManager(CombatCore plugin, ConfigManager config, RegionManager regions) {
        this.plugin = plugin;
        this.config = config;
        this.regions = regions;
    }

    /** Records that two players are fighting each other (both directions). */
    public void setOpponents(UUID a, UUID b) {
        if (a == null || b == null || a.equals(b)) return;
        lastOpponent.put(a, b);
        lastOpponent.put(b, a);
    }

    public UUID getOpponent(UUID id) {
        return lastOpponent.get(id);
    }

    public void setBarriers(BarrierManager barriers) {
        this.barriers = barriers;
    }

    private void removeWall(UUID id) {
        if (barriers == null) return;
        Player p = plugin.getServer().getPlayer(id);
        if (p != null) barriers.clear(p);
    }

    /** Puts (or refreshes) a player into combat. Sends the enter message only on first tag. */
    public void tag(Player player) {
        if (player == null) return;
        boolean wasInCombat = isInCombat(player.getUniqueId());
        long until = System.currentTimeMillis() + (config.getCombatDuration() * 1000L);
        combatUntil.put(player.getUniqueId(), until);
        if (!wasInCombat) {
            player.sendMessage(config.msgNowInCombat());
        }
    }

    public boolean isInCombat(UUID id) {
        Long until = combatUntil.get(id);
        if (until == null) return false;
        if (System.currentTimeMillis() >= until) {
            combatUntil.remove(id);
            return false;
        }
        return true;
    }

    public boolean isInCombat(Player player) {
        return isInCombat(player.getUniqueId());
    }

    /** Seconds remaining (rounded up), or 0 if not in combat. */
    public int getSecondsLeft(UUID id) {
        Long until = combatUntil.get(id);
        if (until == null) return 0;
        long diff = until - System.currentTimeMillis();
        if (diff <= 0) return 0;
        return (int) Math.ceil(diff / 1000.0);
    }

    /** Removes combat tag and notifies the player. Used when timer naturally ends. */
    public void clear(Player player) {
        if (combatUntil.remove(player.getUniqueId()) != null) {
            player.sendMessage(config.msgLeftCombat());
            removeWall(player.getUniqueId());
        }
        lastOpponent.remove(player.getUniqueId());
    }

    public void clearSilent(UUID id) {
        if (combatUntil.remove(id) != null) {
            removeWall(id);
        }
        lastOpponent.remove(id);
    }

    public void clearAll() {
        combatUntil.clear();
        lastOpponent.clear();
    }

    public Map<UUID, Long> getCombatMap() {
        return combatUntil;
    }
}
