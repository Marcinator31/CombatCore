package de.combatcore.commands;

import de.combatcore.CombatCore;
import de.combatcore.config.ConfigManager;
import de.combatcore.regions.Region;
import de.combatcore.regions.RegionManager;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RegionCommand implements CommandExecutor, TabCompleter {

    private final CombatCore plugin;
    private final RegionManager regions;
    private final ConfigManager config;

    public RegionCommand(CombatCore plugin, RegionManager regions, ConfigManager config) {
        this.plugin = plugin;
        this.regions = regions;
        this.config = config;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd,
                             @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("combatcore.admin")) {
            sender.sendMessage(config.prefixed("<red>No permission.</red>"));
            return true;
        }
        if (!(sender instanceof Player player)) {
            sender.sendMessage(config.prefixed("<red>Players only.</red>"));
            return true;
        }
        if (args.length == 0) {
            sender.sendMessage(config.prefixed("<gray>/combatregion <pos1|pos2|create <name>|delete <name>|list></gray>"));
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "pos1" -> {
                Location l = player.getLocation();
                regions.setPos1(player, l);
                player.sendMessage(config.prefixed("<green>Pos1 set: "
                        + l.getBlockX() + "," + l.getBlockY() + "," + l.getBlockZ() + "</green>"));
            }
            case "pos2" -> {
                Location l = player.getLocation();
                regions.setPos2(player, l);
                player.sendMessage(config.prefixed("<green>Pos2 set: "
                        + l.getBlockX() + "," + l.getBlockY() + "," + l.getBlockZ() + "</green>"));
            }
            case "create" -> {
                if (args.length < 2) {
                    player.sendMessage(config.prefixed("<red>Usage: /combatregion create <name></red>"));
                    return true;
                }
                Location a = regions.getPos1(player);
                Location b = regions.getPos2(player);
                if (a == null || b == null) {
                    player.sendMessage(config.prefixed("<red>Set pos1 and pos2 first.</red>"));
                    return true;
                }
                if (regions.get(args[1]) != null) {
                    player.sendMessage(config.prefixed("<red>A region with that name already exists.</red>"));
                    return true;
                }
                if (regions.create(args[1], a, b)) {
                    player.sendMessage(config.prefixed("<green>Region '" + args[1] + "' created.</green>"));
                } else {
                    player.sendMessage(config.prefixed("<red>Could not create region.</red>"));
                }
            }
            case "delete", "remove" -> {
                if (args.length < 2) {
                    player.sendMessage(config.prefixed("<red>Usage: /combatregion delete <name></red>"));
                    return true;
                }
                if (regions.delete(args[1])) {
                    player.sendMessage(config.prefixed("<green>Region '" + args[1] + "' deleted.</green>"));
                } else {
                    player.sendMessage(config.prefixed("<red>Region not found.</red>"));
                }
            }
            case "list" -> {
                if (regions.all().isEmpty()) {
                    player.sendMessage(config.prefixed("<gray>No regions defined.</gray>"));
                    return true;
                }
                player.sendMessage(config.prefixed("<yellow>Regions:</yellow>"));
                for (Region r : regions.all()) {
                    player.sendMessage(config.component("<gray> - <white>" + r.getName()
                            + "</white> <dark_gray>(" + r.getWorldName() + ")</dark_gray></gray>"));
                }
            }
            default -> player.sendMessage(config.prefixed("<red>Unknown subcommand.</red>"));
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd,
                                      @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            List<String> base = new ArrayList<>(Arrays.asList("pos1", "pos2", "create", "delete", "list"));
            base.removeIf(s -> !s.startsWith(args[0].toLowerCase()));
            return base;
        }
        if (args.length == 2 && (args[0].equalsIgnoreCase("delete") || args[0].equalsIgnoreCase("remove"))) {
            List<String> names = new ArrayList<>();
            regions.all().forEach(r -> names.add(r.getName()));
            return names;
        }
        return new ArrayList<>();
    }
}
