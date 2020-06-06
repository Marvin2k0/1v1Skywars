package de.marvin2k0.skywars.listener;

import de.marvin2k0.skywars.Skywars;
import de.marvin2k0.skywars.utils.Locations;
import de.marvin2k0.skywars.utils.Text;
import de.marvinleiers.minigameapi.MinigameAPI;
import de.marvinleiers.minigameapi.game.Game;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SignListener implements Listener
{
    public static HashMap<Game, ArrayList<Location>> signs = new HashMap<>();
    @EventHandler
    public void onSign(SignChangeEvent event)
    {
        final Player player = event.getPlayer();

        if (!player.hasPermission("skywars.sign"))
            return;

        if (event.getLine(0).equalsIgnoreCase("[1v1Skywars]") && !event.getLine(1).isEmpty() && Skywars.getApi().exists(Skywars.plugin, event.getLine(1)))
        {
            Game game = Skywars.getApi().getGameFromName(Skywars.plugin, event.getLine(1));

            event.setLine(0, Text.get("prefix"));
            event.setLine(1, event.getLine(1));
            event.setLine(2, game.getPlayers().size() + "/" + Text.get("maxplayers", false));

            addSign(game, (Sign) event.getBlock().getState());
        }
    }

    public static void updateSigns(Game game, String str)
    {
        for (Location loc : signs.get(game))
        {
            if (loc.getBlock().getType().toString().contains("SIGN"))
            {
                Sign sign = (Sign) loc.getBlock().getState();
                sign.setLine(2, str);
                sign.update();
            }
        }
    }

    public void addSign(Game game, Sign sign)
    {
        Locations.setLocation("games." + game.getName() + ".signs." + UUID.randomUUID(), sign.getLocation());
        Skywars.plugin.saveConfig();

        signs.clear();
        signs.put(game, loadSigns(game));
    }

    public static ArrayList<Location> loadSigns(Game game)
    {
        if (!Skywars.plugin.getConfig().isSet("games." + game.getName() + ".signs"))
            return null;

        ArrayList<Location> arr = new ArrayList<>();

        for (Map.Entry<String, Object> entry : Skywars.plugin.getConfig().getConfigurationSection("games." + game.getName() + ".signs").getValues(false).entrySet())
        {
            arr.add(Locations.get("games." + game.getName() + ".signs." + entry.getKey()));
        }

        signs.put(game, arr);

        return arr;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event)
    {
        if (!event.hasBlock())
            return;

        if (event.getClickedBlock().getType().toString().contains("SIGN"))
        {
            final Player player = event.getPlayer();
            final Sign sign = (Sign) event.getClickedBlock().getState();

            if (event.hasItem() && event.getItem().getType() == Material.DIAMOND_AXE)
                return;

            if (sign.getLine(0).equals(Text.get("prefix")) && Skywars.getApi().exists(Skywars.plugin, sign.getLine(1)))
            {
                System.out.println(Text.get("prefix"));

                Game game = Skywars.getApi().getGameFromName(Skywars.plugin, sign.getLine(1));
                game.join(Skywars.plugin, player);

                event.setCancelled(true);
            }
        }
    }
}
