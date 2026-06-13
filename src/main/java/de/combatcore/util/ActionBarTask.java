package de.combatcore.util;

import de.combatcore.CombatCore;
import de.combatcore.combat.CombatManager;
import de.combatcore.config.ConfigManager;
import de.combatcore.regions.BarrierManager;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/**
 * Runs a few times per second. For each player in combat it shows the
 * remaining time above the hotbar (action bar). When a tag expires it
 * fires the "left combat" message exactly once.
 */
public class ActionBarTask extends BukkitRunnable {

    private final CombatCore plugin;
    private final CombatManager combat;
    private final ConfigManager config;
    private final BarrierManager barriers;

    public ActionBarTask(CombatCore plugin, CombatManager combat, ConfigManager config,
                         BarrierManager barriers) {
        this.plugin = plugin;
        this.combat = combat;
        this.config = config;
        this.barriers = barriers;
    }

    @Override
    public void run() {
        Iterator<Map.Entry<UUID, Long>> it = combat.getCombatMap().entrySet().iterator();
        long now = System.currentTimeMillis();

        while (it.hasNext()) {
            Map.Entry<UUID, Long> entry = it.next();
            UUID id = entry.getKey();
            Player player = plugin.getServer().getPlayer(id);

            if (entry.getValue() <= now) {
                // expired
                it.remove();
                if (player != null) {
                    player.sendMessage(config.msgLeftCombat());
                    barriers.clear(player); // remove any barrier wall now they're safe
                }
                continue;
            }

            if (player != null && config.isShowActionBar()) {
                int secondsLeft = combat.getSecondsLeft(id);
                player.sendActionBar(config.actionBar(secondsLeft));
            }
        }
    }
}
