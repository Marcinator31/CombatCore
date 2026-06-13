package de.combatcore.commands;

import de.combatcore.CombatCore;
import de.combatcore.combat.CombatManager;
import de.combatcore.config.ConfigManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CombatCoreCommand implements org.bukkit.command.CommandExecutor, TabCompleter {

    private final CombatCore plugin;
    private final CombatManager combat;
    private final ConfigManager config;

    public CombatCoreCommand(CombatCore plugin, CombatManager combat, ConfigManager config) {
        this.plugin = plugin;
        this.combat = combat;
        this.config = config;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd,
                             @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            sender.sendMessage(config.prefixed("<gray>CombatCore v" + plugin.getDescription().getVersion()
                    + " — /combatcore <reload|status|tag|untag></gray>"));
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload" -> {
                if (noPerm(sender, "combatcore.admin")) return true;
                plugin.reloadAll();
                sender.sendMessage(config.prefixed("<green>Configuration reloaded.</green>"));
            }
            case "status" -> {
                if (!(sender instanceof Player p)) {
                    sender.sendMessage(config.prefixed("<red>Players only.</red>"));
                    return true;
                }
                if (combat.isInCombat(p)) {
                    sender.sendMessage(config.prefixed("<yellow>In combat for "
                            + combat.getSecondsLeft(p.getUniqueId()) + "s.</yellow>"));
                } else {
                    sender.sendMessage(config.prefixed("<green>Not in combat.</green>"));
                }
            }
            case "tag" -> {
                if (noPerm(sender, "combatcore.admin")) return true;
                Player target = resolveTarget(sender, args);
                if (target == null) return true;
                combat.tag(target);
                sender.sendMessage(config.prefixed("<yellow>Tagged " + target.getName() + ".</yellow>"));
            }
            case "untag" -> {
                if (noPerm(sender, "combatcore.admin")) return true;
                Player target = resolveTarget(sender, args);
                if (target == null) return true;
                combat.clear(target);
                sender.sendMessage(config.prefixed("<green>Untagged " + target.getName() + ".</green>"));
            }
            default -> sender.sendMessage(config.prefixed("<red>Unknown subcommand.</red>"));
        }
        return true;
    }

    private Player resolveTarget(CommandSender sender, String[] args) {
        if (args.length >= 2) {
            Player t = plugin.getServer().getPlayerExact(args[1]);
            if (t == null) sender.sendMessage(config.prefixed("<red>Player not found.</red>"));
            return t;
        }
        if (sender instanceof Player p) return p;
        sender.sendMessage(config.prefixed("<red>Specify a player.</red>"));
        return null;
    }

    private boolean noPerm(CommandSender sender, String perm) {
        if (!sender.hasPermission(perm)) {
            sender.sendMessage(config.prefixed("<red>No permission.</red>"));
            return true;
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd,
                                      @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            List<String> base = new ArrayList<>(Arrays.asList("reload", "status", "tag", "untag"));
            base.removeIf(s -> !s.startsWith(args[0].toLowerCase()));
            return base;
        }
        if (args.length == 2 && (args[0].equalsIgnoreCase("tag") || args[0].equalsIgnoreCase("untag"))) {
            List<String> names = new ArrayList<>();
            plugin.getServer().getOnlinePlayers().forEach(p -> names.add(p.getName()));
            return names;
        }
        return new ArrayList<>();
    }
}
