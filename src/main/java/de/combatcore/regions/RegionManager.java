package de.combatcore.regions;

import de.combatcore.CombatCore;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Stores regions in regions.yml. Also handles the two-corner wand/pos selection
 * used by /combatregion pos1, pos2, create.
 */
public class RegionManager {

    private final CombatCore plugin;
    private final Map<String, Region> regions = new ConcurrentHashMap<>();

    // selection state per player
    private final Map<UUID, Location> pos1 = new HashMap<>();
    private final Map<UUID, Location> pos2 = new HashMap<>();

    private File file;
    private FileConfiguration data;

    public RegionManager(CombatCore plugin) {
        this.plugin = plugin;
    }

    public void load() {
        regions.clear();
        file = new File(plugin.getDataFolder(), "regions.yml");
        if (!file.exists()) {
            try {
                plugin.getDataFolder().mkdirs();
                file.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().warning("Could not create regions.yml: " + e.getMessage());
            }
        }
        data = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection sec = data.getConfigurationSection("regions");
        if (sec != null) {
            for (String name : sec.getKeys(false)) {
                ConfigurationSection r = sec.getConfigurationSection(name);
                if (r == null) continue;
                Region region = new Region(
                        name,
                        r.getString("world", "world"),
                        r.getInt("x1"), r.getInt("y1"), r.getInt("z1"),
                        r.getInt("x2"), r.getInt("y2"), r.getInt("z2"));
                regions.put(name.toLowerCase(Locale.ROOT), region);
            }
        }
        plugin.getLogger().info("Loaded " + regions.size() + " combat region(s).");
    }

    public void save() {
        if (data == null || file == null) return;
        data.set("regions", null);
        for (Region region : regions.values()) {
            String path = "regions." + region.getName();
            data.set(path + ".world", region.getWorldName());
            data.set(path + ".x1", region.getMinX());
            data.set(path + ".y1", region.getMinY());
            data.set(path + ".z1", region.getMinZ());
            data.set(path + ".x2", region.getMaxX());
            data.set(path + ".y2", region.getMaxY());
            data.set(path + ".z2", region.getMaxZ());
        }
        try {
            data.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Could not save regions.yml: " + e.getMessage());
        }
    }

    public void setPos1(Player p, Location l) { pos1.put(p.getUniqueId(), l); }
    public void setPos2(Player p, Location l) { pos2.put(p.getUniqueId(), l); }
    public Location getPos1(Player p) { return pos1.get(p.getUniqueId()); }
    public Location getPos2(Player p) { return pos2.get(p.getUniqueId()); }

    public boolean create(String name, Location a, Location b) {
        if (a == null || b == null || a.getWorld() == null) return false;
        Region region = new Region(name, a.getWorld().getName(),
                a.getBlockX(), a.getBlockY(), a.getBlockZ(),
                b.getBlockX(), b.getBlockY(), b.getBlockZ());
        regions.put(name.toLowerCase(Locale.ROOT), region);
        save();
        return true;
    }

    public boolean delete(String name) {
        if (regions.remove(name.toLowerCase(Locale.ROOT)) != null) {
            save();
            return true;
        }
        return false;
    }

    public Region get(String name) {
        return regions.get(name.toLowerCase(Locale.ROOT));
    }

    public Collection<Region> all() {
        return regions.values();
    }

    /** Returns the region containing the location, or null. */
    public Region regionAt(Location loc) {
        for (Region r : regions.values()) {
            if (r.contains(loc)) return r;
        }
        return null;
    }
}
