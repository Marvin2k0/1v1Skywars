package de.marvin2k0.skywars.listener;

import de.marvin2k0.skywars.Skywars;
import de.marvin2k0.skywars.utils.Locations;
import de.marvin2k0.skywars.utils.Text;
import de.marvinleiers.minigameapi.events.GameStartEvent;
import de.marvinleiers.minigameapi.events.PlayerGameJoinEvent;
import de.marvinleiers.minigameapi.events.PlayerInLobbyItemInteractEvent;
import de.marvinleiers.minigameapi.game.Game;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

public class GameListener implements Listener
{
    @EventHandler
    public void onGameJoin(PlayerGameJoinEvent event)
    {
        Player player = event.getPlayer();
        Game game = event.getGame();

        game.sendMessage(Text.get("join").replace("%player%", event.getPlayer().getName()));

        if (!Skywars.plugin.getConfig().isSet("games." + game.getName() + ".gamespawn") || !Skywars.plugin.getConfig().isSet("games." + game.getName() + ".gamelobby"))
        {
            player.sendMessage(Text.get("notset"));
            game.leave(event.getPlayer());
            return;
        }

        event.getPlayer().teleport(Locations.get("games." + game.getName() + ".gamelobby"));
    }

    @EventHandler
    public void onStart(GameStartEvent event)
    {
        Game game = event.getGame();

        if (!Skywars.plugin.getConfig().isSet("games." + game.getName() + ".spawn"))
            return;

        Location spawn = Locations.get("games." + game.getName() + ".spawn");

        for (Player player : game.getPlayers())
            player.teleport(spawn);
    }

    @EventHandler
    public void onLobbyItem(PlayerInLobbyItemInteractEvent event)
    {
        ItemStack item = event.getItem();

        if (item.hasItemMeta() && item.getItemMeta().getDisplayName().equals(Text.get("bedname", false)))
            event.getGame().leave(event.getPlayer());
    }
}
