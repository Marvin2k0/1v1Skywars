package de.marvin2k0.skywars.listener;

import de.marvin2k0.skywars.utils.Text;
import de.marvinleiers.minigameapi.MinigameAPI;
import de.marvinleiers.minigameapi.game.Game;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class SignListener implements Listener
{
    @EventHandler
    public void onSign(SignChangeEvent event)
    {
        final Player player = event.getPlayer();

        if (!player.hasPermission("monopoly.sign"))
            return;

        if (event.getLine(0).equalsIgnoreCase("[1v1Skywars]") && !event.getLine(1).isEmpty() && MinigameAPI.exists(event.getLine(1)))
        {
            event.setLine(0, Text.get("prefix"));
            event.setLine(1, event.getLine(1));
        }
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

            if (sign.getLine(0).equals(Text.get("prefix")) && MinigameAPI.exists(sign.getLine(1)))
            {
                Game game = MinigameAPI.getGameFromName(sign.getLine(1));
                game.join(player);

                event.setCancelled(true);
            }
        }
    }
}