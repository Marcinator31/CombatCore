package de.combatcore.config;

import de.combatcore.CombatCore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Central access to every configurable option.
 * Reads values from config.yml and caches them; call reload() after editing.
 */
public class ConfigManager {

    private final CombatCore plugin;
    private final MiniMessage mm = MiniMessage.miniMessage();

    // Combat
    private int combatDuration;
    private boolean tagOnDamageDealt;
    private boolean tagOnDamageReceived;
    private boolean tagPvpOnly;
    private boolean punishOnLogout;
    private boolean broadcastLogoutDeath;

    // Blocking
    private Set<String> blockedCommands;
    private Set<String> blockedItems;
    private boolean blockElytra;
    private boolean blockEnderPearl;

    // Display
    private boolean showActionBar;
    private String actionBarFormat;
    private boolean showBossBar;
    private String bossBarColor;

    // Messages
    private String prefix;
    private String msgNowInCombat;
    private String msgLeftCombat;
    private String msgCommandBlocked;
    private String msgItemBlocked;
    private String msgRegionBlocked;
    private String msgLogoutPunish;

    public ConfigManager(CombatCore plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        FileConfiguration c = plugin.getConfig();

        // Combat
        combatDuration = c.getInt("combat.duration-seconds", 15);
        tagOnDamageDealt = c.getBoolean("combat.tag-on-damage-dealt", true);
        tagOnDamageReceived = c.getBoolean("combat.tag-on-damage-received", true);
        tagPvpOnly = c.getBoolean("combat.pvp-only", true);
        punishOnLogout = c.getBoolean("combat.punish-on-logout", true);
        broadcastLogoutDeath = c.getBoolean("combat.broadcast-logout-death", true);

        // Blocking
        blockedCommands = toLowerSet(c.getStringList("blocked.commands"));
        blockedItems = toUpperSet(c.getStringList("blocked.items"));
        blockElytra = c.getBoolean("blocked.elytra", true);
        blockEnderPearl = c.getBoolean("blocked.ender-pearl", false);

        // Display
        showActionBar = c.getBoolean("display.action-bar.enabled", true);
        actionBarFormat = c.getString("display.action-bar.format",
                "<red>\u2694 Combat: <yellow><time>s</yellow> \u2694</red>");
        showBossBar = c.getBoolean("display.boss-bar.enabled", false);
        bossBarColor = c.getString("display.boss-bar.color", "RED");

        // Messages
        prefix = c.getString("messages.prefix", "<dark_red>[Combat]</dark_red> ");
        msgNowInCombat = c.getString("messages.now-in-combat",
                "<red>You are now in combat! Do not log out.</red>");
        msgLeftCombat = c.getString("messages.left-combat",
                "<green>You are no longer in combat.</green>");
        msgCommandBlocked = c.getString("messages.command-blocked",
                "<red>You cannot use this command while in combat!</red>");
        msgItemBlocked = c.getString("messages.item-blocked",
                "<red>You cannot use this item while in combat!</red>");
        msgRegionBlocked = c.getString("messages.region-blocked",
                "<red>You cannot enter this area while in combat!</red>");
        msgLogoutPunish = c.getString("messages.logout-punish",
                "<dark_red><player> logged out in combat and was slain!</dark_red>");
    }

    private Set<String> toLowerSet(List<String> list) {
        Set<String> s = new HashSet<>();
        for (String e : list) s.add(e.toLowerCase(Locale.ROOT).replace("/", "").trim());
        return s;
    }

    private Set<String> toUpperSet(List<String> list) {
        Set<String> s = new HashSet<>();
        for (String e : list) s.add(e.toUpperCase(Locale.ROOT).trim());
        return s;
    }

    // --- Getters ---
    public int getCombatDuration() { return combatDuration; }
    public boolean isTagOnDamageDealt() { return tagOnDamageDealt; }
    public boolean isTagOnDamageReceived() { return tagOnDamageReceived; }
    public boolean isTagPvpOnly() { return tagPvpOnly; }
    public boolean isPunishOnLogout() { return punishOnLogout; }
    public boolean isBroadcastLogoutDeath() { return broadcastLogoutDeath; }

    public Set<String> getBlockedCommands() { return blockedCommands; }
    public Set<String> getBlockedItems() { return blockedItems; }
    public boolean isBlockElytra() { return blockElytra; }
    public boolean isBlockEnderPearl() { return blockEnderPearl; }

    public boolean isShowActionBar() { return showActionBar; }
    public String getActionBarFormat() { return actionBarFormat; }
    public boolean isShowBossBar() { return showBossBar; }
    public String getBossBarColor() { return bossBarColor; }

    // --- Component builders ---
    public Component prefixed(String miniMessage) {
        return mm.deserialize(prefix + miniMessage);
    }
    public Component component(String miniMessage) {
        return mm.deserialize(miniMessage);
    }
    public Component actionBar(int secondsLeft) {
        return mm.deserialize(actionBarFormat.replace("<time>", String.valueOf(secondsLeft)));
    }

    public Component msgNowInCombat() { return prefixed(msgNowInCombat); }
    public Component msgLeftCombat() { return prefixed(msgLeftCombat); }
    public Component msgCommandBlocked() { return prefixed(msgCommandBlocked); }
    public Component msgItemBlocked() { return prefixed(msgItemBlocked); }
    public Component msgRegionBlocked() { return prefixed(msgRegionBlocked); }
    public Component msgLogoutPunish(String player) {
        return prefixed(msgLogoutPunish.replace("<player>", player));
    }
}
