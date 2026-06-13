package de.combatcore;

import de.combatcore.combat.CombatManager;
import de.combatcore.commands.CombatCoreCommand;
import de.combatcore.commands.RegionCommand;
import de.combatcore.config.ConfigManager;
import de.combatcore.listeners.CombatListener;
import de.combatcore.listeners.CommandBlockListener;
import de.combatcore.listeners.ItemBlockListener;
import de.combatcore.listeners.RegionListener;
import de.combatcore.regions.RegionManager;
import de.combatcore.util.ActionBarTask;
import org.bukkit.plugin.java.JavaPlugin;

public final class CombatCore extends JavaPlugin {

    private ConfigManager configManager;
    private CombatManager combatManager;
    private RegionManager regionManager;
    private ActionBarTask actionBarTask;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        this.configManager = new ConfigManager(this);
        this.regionManager = new RegionManager(this);
        this.combatManager = new CombatManager(this, configManager, regionManager);

        regionManager.load();

        // Listeners
        getServer().getPluginManager().registerEvents(new CombatListener(this, combatManager, configManager), this);
        getServer().getPluginManager().registerEvents(new CommandBlockListener(this, combatManager, configManager), this);
        getServer().getPluginManager().registerEvents(new ItemBlockListener(this, combatManager, configManager), this);
        getServer().getPluginManager().registerEvents(new RegionListener(this, combatManager, regionManager, configManager), this);

        // Commands
        CombatCoreCommand core = new CombatCoreCommand(this, combatManager, configManager);
        getCommand("combatcore").setExecutor(core);
        getCommand("combatcore").setTabCompleter(core);

        RegionCommand region = new RegionCommand(this, regionManager, configManager);
        getCommand("combatregion").setExecutor(region);
        getCommand("combatregion").setTabCompleter(region);

        // Action bar / hotbar timer task
        this.actionBarTask = new ActionBarTask(this, combatManager, configManager);
        actionBarTask.runTaskTimer(this, 0L, 5L); // updates 4x/sec for smooth countdown

        getLogger().info("CombatCore enabled. Combat duration: " + configManager.getCombatDuration() + "s");
    }

    @Override
    public void onDisable() {
        if (combatManager != null) combatManager.clearAll();
        if (regionManager != null) regionManager.save();
        getLogger().info("CombatCore disabled.");
    }

    public ConfigManager getConfigManager() { return configManager; }
    public CombatManager getCombatManager() { return combatManager; }
    public RegionManager getRegionManager() { return regionManager; }

    public void reloadAll() {
        reloadConfig();
        configManager.reload();
        regionManager.load();
    }
}
