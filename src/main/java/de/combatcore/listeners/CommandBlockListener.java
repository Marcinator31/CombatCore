package de.combatcore.listeners;

import de.combatcore.CombatCore;
import de.combatcore.combat.CombatManager;
import de.combatcore.config.ConfigManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.Locale;

/**
 * Blocks configured commands (e.g. rtp, tpa, home) while a player is in combat.
 */
public class CommandBlockListener implements Listener {

    private final CombatCore plugin;
    private final CombatManager combat;
    private final ConfigManager config;

    public CommandBlockListener(CombatCore plugin, CombatManager combat, ConfigManager config) {
        this.plugin = plugin;
        this.combat = combat;
        this.config = config;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onCommand(PlayerCommandPreprocessEvent e) {
        Player player = e.getPlayer();
        if (!combat.isInCombat(player)) return;
        if (player.hasPermission("combatcore.bypass.commands")) return;

        // Extract root command without slash and arguments
        String msg = e.getMessage().substring(1).toLowerCase(Locale.ROOT);
        String root = msg.split(" ")[0];
        // Strip plugin namespace like "essentials:rtp"
        if (root.contains(":")) root = root.substring(root.indexOf(':') + 1);

        if (config.getBlockedCommands().contains(root)) {
            e.setCancelled(true);
            player.sendMessage(config.msgCommandBlocked());
        }
    }
}
