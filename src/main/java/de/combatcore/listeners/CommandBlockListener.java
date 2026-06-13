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
 * Runs at LOWEST priority and does NOT ignore cancelled events, so it blocks
 * the command before any other plugin can act on it.
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

    @EventHandler(priority = EventPriority.LOWEST)
    public void onCommand(PlayerCommandPreprocessEvent e) {
        Player player = e.getPlayer();
        if (!combat.isInCombat(player)) return;

        // Explicit bypass permission. OPs do NOT bypass unless they hold this node,
        // so we check it without the implicit op-grant by also checking isPermissionSet.
        if (player.isPermissionSet("combatcore.bypass.commands")
                && player.hasPermission("combatcore.bypass.commands")) {
            return;
        }

        // Extract root command without slash and arguments
        String msg = e.getMessage().substring(1).toLowerCase(Locale.ROOT);
        String root = msg.split(" ")[0];
        // Strip plugin namespace like "essentials:rtp"
        if (root.contains(":")) root = root.substring(root.indexOf(':') + 1);

        if (config.getBlockedCommands().contains(root)) {
            e.setCancelled(true);
            player.sendMessage(config.msgCommandBlocked());
            if (config.isDebug()) {
                plugin.getLogger().info("[debug] Blocked '/" + root + "' from " + player.getName());
            }
        } else if (config.isDebug()) {
            plugin.getLogger().info("[debug] Allowed '/" + root + "' from " + player.getName()
                    + " (not in blocked list: " + config.getBlockedCommands() + ")");
        }
    }
}
