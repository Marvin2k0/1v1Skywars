package de.marvin2k0.skywars.utils;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class Text
{
    static FileConfiguration config;
    static JavaPlugin plugin;

    public static String get(String path)
    {
        return path.equalsIgnoreCase("prefix") ? get(path, false) : get(path, true);
    }

    public static String get(String path, boolean prefix)
    {
        if (config == null)
        {
            System.out.println("config ist null");
            return null;
        }

        return ChatColor.translateAlternateColorCodes('&', prefix ? config.getString("prefix") + " " + config.getString(path) : config.getString(path));
    }

    public static void setUp(JavaPlugin plugin)
    {
        Text.plugin = plugin;
        Text.config = plugin.getConfig();

        config.options().copyDefaults(true);
        config.addDefault("prefix" , "&7[&9Skywars&7]");
        config.addDefault("noplayer", "&cDieser Befehl ist nur für Spieler!");
        config.addDefault("spawnset", "&aErfolgreich &7für Spiel &b%game% &7gesetzt!");
        config.addDefault("notset", "&cFür dieses Spiel wurden noch nicht alle Spawns gesetzt!");
        config.addDefault("join", "&7[&a+&7] &9%player% &7hat das Spiel betreten.");
        config.addDefault("kitsname", "&6Kits");
        config.addDefault("bedname", "&cVerlassen");
        config.addDefault("death", "&9%player% &7hat noch &9%lives% &7Leben.");
        config.addDefault("minplayers", 2);
        config.addDefault("maxplayers", 2);

        saveConfig();
    }

    private static void saveConfig()
    {
        plugin.saveConfig();
    }
}
