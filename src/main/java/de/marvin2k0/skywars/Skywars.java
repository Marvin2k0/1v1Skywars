package de.marvin2k0.skywars;

import de.marvin2k0.skywars.listener.GameListener;
import de.marvin2k0.skywars.listener.SignListener;
import de.marvin2k0.skywars.utils.Locations;
import de.marvin2k0.skywars.utils.Text;
import de.marvinleiers.minigameapi.MinigameMain;
import de.marvinleiers.minigameapi.game.Game;
import de.marvinleiers.minigameapi.utils.ItemUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
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
        api.setLobbyItems(this, lobbyItems);

        Inventory gameItems = Bukkit.createInventory(null, 27, "");
        gameItems.setItem(0, new ItemStack(Material.AIR));
        api.setGameItems(this, gameItems);

        this.getServer().getPluginManager().registerEvents(new GameListener(), this);
        this.getServer().getPluginManager().registerEvents(new SignListener(), this);

        this.getCommand("setgamelobby").setExecutor(this);
        this.getCommand("setgamespawn").setExecutor(this);
        this.getCommand("setylevel").setExecutor(this);
        this.getCommand("leave").setExecutor(this);
    }

    @Override
    public void onDisable()
    {
        MinigameMain.disable(this, api.getGames(this));
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

        if (label.equalsIgnoreCase("setgamelobby"))
        {
            if (args.length != 1)
            {
                player.sendMessage("§cUsage: /setgamlobby <game>");
                return true;
            }

            String game = args[0];

            if (!getApi().exists(this, game))
                api.createGame(this, game);

            Locations.setLocation("games." + game + ".lobby.", player.getLocation());

            player.sendMessage(Text.get("spawnset").replace("%game%", game));
            return true;
        }

        else if (label.equalsIgnoreCase("setgamespawn"))
        {
            if (args.length != 2)
            {
                player.sendMessage("§cUsage: /setgamlobby <game> <1|2>");
                return true;
            }

            if (!args[1].equalsIgnoreCase("1") && !args[1].equalsIgnoreCase("2"))
            {
                player.sendMessage("§cUsage: /setgamlobby <game> <1|2>");
                return true;
            }

            String game = args[0];
            String spawnName = args[1];

            Locations.setLocation("games." + game + ".spawns." + spawnName, player.getLocation());
            player.sendMessage(Text.get("spawnset").replace("%game%", game));
            return true;
        }

        else if (label.equalsIgnoreCase("setylevel"))
        {
            if (args.length != 2)
            {
                player.sendMessage("§cUsage: /setylevel <game> <Y-Koordinate>");
                return true;
            }

            try
            {
                int y = Integer.parseInt(args[1]);

                getConfig().set("games." + args[0] + ".level", y);
                saveConfig();
            }
            catch (NumberFormatException e)
            {
                player.sendMessage("§4Error: §cBitte nur Zahlen eingeben! §4" + args[1] + " §cist keine Zahl!");
            }
        }

        else if (label.equalsIgnoreCase("leave"))
        {
            if (!MinigameAPI.inGame(player))
                return true;

            Game game = MinigameAPI.gameplayers.get(player).getGame();
            game.leave(this, player);
            return true;
        }

        return true;
    }

    public static MinigameAPI getApi()
    {
        return api;
    }
}
