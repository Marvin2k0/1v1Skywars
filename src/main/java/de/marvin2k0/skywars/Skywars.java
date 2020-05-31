package de.marvin2k0.skywars;

import de.marvin2k0.skywars.listener.GameListener;
import de.marvin2k0.skywars.listener.SignListener;
import de.marvin2k0.skywars.utils.Locations;
import de.marvin2k0.skywars.utils.Text;
import de.marvinleiers.minigameapi.utils.ItemUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;
import de.marvinleiers.minigameapi.MinigameAPI;

public class Skywars extends JavaPlugin
{
    private static MinigameAPI api;
    public static Skywars plugin;

    @Override
    public void onEnable()
    {
        api = MinigameAPI.getAPI(this);
        plugin = this;

        Text.setUp(this);

        Inventory lobbyItems = Bukkit.createInventory(null, 27, "");
        lobbyItems.setItem(8, ItemUtils.create(Material.BED, Text.get("bedname", false)));
        lobbyItems.setItem(0, ItemUtils.create(Material.CHEST, Text.get("kitsname", false)));

        api.setLobbyItems(lobbyItems);

        this.getServer().getPluginManager().registerEvents(new GameListener(), this);
        this.getServer().getPluginManager().registerEvents(new SignListener(), this);

        this.getCommand("setgamelobby").setExecutor(this);
        this.getCommand("setgamespawn").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        if (!(sender instanceof Player))
        {
            sender.sendMessage(Text.get("noplayer"));
            return true;
        }

        Player player = (Player) sender;

        if (args.length != 1)
        {
            player.sendMessage("§cUsage: /" + label + " <game>");
            return true;
        }

        String game = args[0];
        String spawnName = label.substring(3).toLowerCase();

        if (!MinigameAPI.exists(game))
            api.createGame(game);

        Locations.setLocation("games." + game + "." + spawnName, player.getLocation());

        player.sendMessage(Text.get("spawnset").replace("%game%", game));
        return true;
    }

    public static MinigameAPI getApi()
    {
        return api;
    }
}
