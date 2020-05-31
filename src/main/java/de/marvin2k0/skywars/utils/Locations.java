package de.marvin2k0.skywars.utils;

import de.marvin2k0.skywars.Skywars;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

public class Locations
{
    public static Location get(String path)
    {
        World world = Bukkit.getWorld(Skywars.plugin.getConfig().getString(path + ".world"));

        double y = Skywars.plugin.getConfig().getDouble(path + ".y");
        double x = Skywars.plugin.getConfig().getDouble(path + ".x");
        double z = Skywars.plugin.getConfig().getDouble(path + ".z");
        double yaw = Skywars.plugin.getConfig().getDouble(path + ".yaw");
        double pitch = Skywars.plugin.getConfig().getDouble(path + ".pitch");

        return new Location(world, x, y, z, (float) yaw, (float) pitch);
    }

    public static void setLocation(String path, Location location)
    {
        Skywars.plugin.getConfig().set(path + ".world", location.getWorld().getName());
        Skywars.plugin.getConfig().set(path + ".x", location.getX());
        Skywars.plugin.getConfig().set(path + ".y", location.getY());
        Skywars.plugin.getConfig().set(path + ".z", location.getZ());
        Skywars.plugin.getConfig().set(path + ".yaw", location.getYaw());
        Skywars.plugin.getConfig().set(path + ".pitch", location.getPitch());

        Skywars.plugin.saveConfig();
    }
}
