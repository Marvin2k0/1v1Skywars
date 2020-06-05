package de.marvin2k0.skywars.listener;

import com.avaje.ebeaninternal.server.text.csv.CsvUtilReader;
import de.marvin2k0.skywars.Skywars;
import de.marvin2k0.skywars.utils.Locations;
import de.marvin2k0.skywars.utils.Text;
import de.marvinleiers.minigameapi.MinigameAPI;
import de.marvinleiers.minigameapi.events.*;
import de.marvinleiers.minigameapi.game.Game;
import de.marvinleiers.minigameapi.game.GamePlayer;
import de.marvinleiers.minigameapi.utils.CountdownTimer;
import de.marvinleiers.minigameapi.utils.ItemUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.ArrayList;
import java.util.HashMap;

public class GameListener implements Listener
{
    private static ArrayList<Player> move = new ArrayList<>();
    private static HashMap<Player, Location> spawns = new HashMap<>();
    private static HashMap<Player, Integer> health = new HashMap<>();
    private static HashMap<Player, Inventory> kits = new HashMap<>();
    private static HashMap<Player, ItemStack[]> kitArmor = new HashMap<>();

    @EventHandler
    public void onGameJoin(PlayerGameJoinEvent event)
    {
        if (event.getPlugin() != Skywars.plugin)
        {
            return;
        }

        Player player = event.getPlayer();
        Game game = event.getGame();

        game.sendMessage(Text.get("join").replace("%player%", event.getPlayer().getName()));

        if (!Skywars.plugin.getConfig().isSet("games." + game.getName() + ".spawns.2")
                || !Skywars.plugin.getConfig().isSet("games." + game.getName() + ".spawns.1")
                || !Skywars.plugin.getConfig().isSet("games." + game.getName() + ".gamelobby"))
        {
            player.sendMessage(Text.get("notset"));
            game.leave(Skywars.plugin, event.getPlayer());
            return;
        }

        event.getPlayer().teleport(Locations.get("games." + game.getName() + ".gamelobby"));
        health.put(event.getPlayer(), 10);
    }

    @EventHandler
    public void onStart(GameStartEvent event)
    {
        if (event.getPlugin() != Skywars.plugin)
        {
            return;
        }

        Game game = event.getGame();

        if (game.getPlayers().size() == 2)
        {
            Location spawn1 = Locations.get("games." + game.getName() + ".spawns.1");
            Location spawn2 = Locations.get("games." + game.getName() + ".spawns.2");

            Player player1 = (Player) game.getPlayers().toArray()[0];
            spawns.put(player1, spawn1);
            player1.teleport(spawn1);

            Player player2 = (Player) game.getPlayers().toArray()[1];
            spawns.put(player2, spawn2);
            player2.teleport(spawn2);
        }

        for (Player p : game.getPlayers())
        {
            getItems(p);
        }

        for (Player player : game.getPlayers())
            move.add(player);

        new CountdownTimer(Skywars.plugin, 5,
                () -> {
                    for (Player player : game.getPlayers())
                        player.sendMessage(" ");
                },
                () -> {
                    for (Player player : game.getPlayers())
                        move.remove(player);
                },
                (t) -> {
                    for (Player player : game.getPlayers())
                        printSeconds(player, t.getSecondsLeft());
                }).scheduleTimer();
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event)
    {
        Player player = event.getPlayer();

        if (move.contains(player))
        {
            event.setTo(event.getFrom());
            return;
        }

        if (!MinigameAPI.inGame(player))
            return;

        GamePlayer gp = MinigameAPI.gameplayers.get(player);
        Game game = gp.getGame();

        if (!game.hasStarted())
            return;

        int y = 0;

        if (Skywars.plugin.getConfig().isSet("games." + gp.getGame().getName() + ".level"))
            y = Skywars.plugin.getConfig().getInt("games." + gp.getGame().getName() + ".level");

        if (event.getTo().getY() <= y)
        {
            event.setCancelled(true);

            for (Player p : gp.getGame().getPlayers())
                p.teleport(spawns.get(p));

            int leben = health.get(player) - 1;
            health.remove(player);
            health.put(player, leben);

            player.setHealth(player.getMaxHealth());

            if (leben <= 0)
            {
                for (Player p : gp.getGame().getPlayers())
                {
                    p.sendTitle("§6" + player.getName(), "§7hat gewonnen!");
                    p.playSound(p.getLocation(), Sound.LEVEL_UP, 1, 1);
                    move.add(p);
                }

                Bukkit.getScheduler().scheduleSyncDelayedTask(Skywars.plugin, () -> {

                    for (Player p : game.getPlayers())
                    {
                        move.remove(p);
                    }

                    game.reset(Skywars.plugin, false);
                }, 5 * 20);

                return;
            }

            game.sendMessage(Text.get("death").replace("%player%", player.getName()).replace("%lives%", health.get(player) + ""));

            for (Player p : game.getPlayers())
            {
                getItems(p);
            }

            for (Player p : gp.getGame().getPlayers())
                move.add(p);

            new CountdownTimer(Skywars.plugin, 5,
                    () -> {
                        for (Player p : gp.getGame().getPlayers())
                            p.sendMessage(" ");
                    },
                    () -> {
                        for (Player p : gp.getGame().getPlayers())
                            move.remove(p);
                    },
                    (t) -> {
                        for (Player p : gp.getGame().getPlayers())
                            printSeconds(p, t.getSecondsLeft());
                    }).scheduleTimer();
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event)
    {
        Player player = event.getEntity().getPlayer();

        if (!MinigameAPI.inGame(player))
            return;

        event.setDroppedExp(0);
        event.getDrops().clear();

        Game game = MinigameAPI.gameplayers.get(player).getGame();
        player.setHealth(player.getMaxHealth());
        player.spigot().respawn();

        Bukkit.getScheduler().scheduleSyncDelayedTask(Skywars.plugin, () -> {
            for (Player p : game.getPlayers())
            {
                getItems(p);
                p.teleport(spawns.get(p));
            }
        }, 1);

        move.addAll(game.getPlayers());

        new CountdownTimer(Skywars.plugin, 5,
                () -> {
                    for (Player p : game.getPlayers())
                        p.sendMessage(" ");
                },
                () -> {
                    for (Player p : game.getPlayers())
                        move.remove(p);
                },
                (t) -> {
                    for (Player p : game.getPlayers())
                        printSeconds(p, t.getSecondsLeft());
                }).scheduleTimer();

        health.put(player, health.get(player) - 1);
        game.sendMessage(Text.get("death").replace("%player%", player.getName()).replace("%lives%", health.get(player) + ""));
        event.setDeathMessage("");
    }

    private void getItems(Player player)
    {
        player.getInventory().clear();
        player.getInventory().setContents(kits.get(player).getContents().clone());
        player.getInventory().setArmorContents(kitArmor.get(player).clone());
        player.updateInventory();
        player.setFoodLevel(20);
    }

    @EventHandler
    public void onItemInteractIngame(PlayerInGameItemInteractEvent event)
    {
        event.setCancelled(false);
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event)
    {
        Player player = event.getPlayer();

        if (!MinigameAPI.inGame(player))
            return;

        Game game = MinigameAPI.gameplayers.get(player).getGame();

        if (event.getBlock().getType() != Material.COBBLESTONE && event.getBlock().getType() != Material.WEB)
        {
            event.setCancelled(false);
            return;
        }

        if (event.getBlock().getType() == Material.WEB)
        {
            Bukkit.getScheduler().scheduleSyncDelayedTask(Skywars.plugin, () -> event.getBlock().setType(Material.AIR), 2 * 20);
            return;
        }

        new CountdownTimer(Skywars.plugin, 2,
                () -> {
                },
                () -> event.getBlock().setType(Material.REDSTONE_BLOCK),
                (t) -> {
                }).scheduleTimer();

        new CountdownTimer(Skywars.plugin, 3,
                () -> {
                },
                () -> event.getBlock().setType(Material.AIR),
                (t) -> {
                }).scheduleTimer();
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event)
    {
        Player player = event.getPlayer();

        if (!MinigameAPI.inGame(player))
            return;

        event.setCancelled(true);
    }

    @EventHandler
    public void onBucket(PlayerBucketEmptyEvent event)
    {
        Player player = event.getPlayer();

        if (!MinigameAPI.inGame(player))
            return;

        if (event.getBucket() == Material.WATER_BUCKET)
        {
            Block block = event.getBlockClicked().getRelative(event.getBlockFace());

            Bukkit.getScheduler().scheduleSyncDelayedTask(Skywars.plugin, () -> block.setType(Material.AIR),2 * 20);
        }
    }

    @EventHandler
    public void onLobbyItem(PlayerInLobbyItemInteractEvent event)
    {
        if (event.getPlugin() != Skywars.plugin)
        {
            return;
        }

        event.setCancelled(true);

        ItemStack item = event.getItem();

        if (item.hasItemMeta() && item.getItemMeta().getDisplayName().equals(Text.get("bedname", false)))
            event.getGame().leave(Skywars.plugin, event.getPlayer());

        if (item.getItemMeta().getDisplayName().equals(Text.get("kitsname", false)))
        {
            openKits(event.getPlayer());
        }
    }

    private void openKits(Player player)
    {
        Inventory inv = Bukkit.createInventory(null, 9, Text.get("kitsname", false));

        inv.addItem(ItemUtils.create(Material.DIAMOND_HELMET, "§9Diamant Kit"));
        inv.addItem(ItemUtils.create(Material.GOLD_HELMET, "§6Gold Kit"));

        player.openInventory(inv);
    }

    @EventHandler
    public void onInLobbyInventoryClickEvent(PlayerInLobbyInventoryClickEvent event)
    {
        Inventory inv = event.getInventory();

        if (!inv.getName().equalsIgnoreCase(Text.get("kitsname", false)))
            return;

        event.setCancelled(true);

        if (event.getClickedItem().getType() == Material.DIAMOND_HELMET)
        {
            getKit(event.getPlayer(), 1);
        }
        else if (event.getClickedItem().getType() == Material.GOLD_HELMET)
        {
            getKit(event.getPlayer(), 2);
        }
        else
        {
            getKit(event.getPlayer(), 1);
        }

        event.getPlayer().closeInventory();
    }

    private void getKit(Player player, int value)
    {
        if (value != 1 && value != 2)
            return;

        Inventory inv = Bukkit.createInventory(null, InventoryType.PLAYER);
        ItemStack[] armor = new ItemStack[4];

        HashMap<Enchantment, Integer> prot1 = new HashMap<>();
        prot1.put(Enchantment.PROTECTION_ENVIRONMENTAL, 1);

        HashMap<Enchantment, Integer> prot2 = new HashMap<>();
        prot2.put(Enchantment.PROTECTION_ENVIRONMENTAL, 2);

        if (value == 1)
        {
            ItemStack helmet = ItemUtils.create(Material.DIAMOND_HELMET, prot2);
            armor[3] = (helmet);

            ItemStack chest = ItemUtils.create(Material.DIAMOND_CHESTPLATE, prot2);
            armor[2] = (chest);

            ItemStack leggins = ItemUtils.create(Material.IRON_LEGGINGS, prot1);
            armor[1] = (leggins);

            ItemStack boots = ItemUtils.create(Material.IRON_BOOTS, prot1);
            armor[0] = (boots);

            inv.setItem(0, new ItemStack(Material.IRON_SWORD));
            inv.setItem(1, new ItemStack(Material.FISHING_ROD));

            ItemStack snowballs = new ItemStack(Material.SNOW_BALL);
            snowballs.setAmount(5);
            inv.setItem(2, snowballs);

            ItemStack web = new ItemStack(Material.WEB);
            web.setAmount(5);
            inv.setItem(3, web);

            ItemStack cobble = new ItemStack(Material.COBBLESTONE);
            cobble.setAmount(64);
            inv.setItem(4, cobble);

            inv.setItem(5, new ItemStack(Material.GOLDEN_APPLE));
            inv.setItem(8, new ItemStack(Material.WATER_BUCKET));

            kits.put(player, inv);
            kitArmor.put(player, armor);
        }
        else
        {
            ItemStack helmet = ItemUtils.create(Material.GOLD_HELMET, prot2);
            armor[3] = (helmet);

            ItemStack chest = ItemUtils.create(Material.GOLD_CHESTPLATE, prot1);
            armor[2] = (chest);

            armor[1] = (new ItemStack(Material.IRON_LEGGINGS));
            armor[0] = (new ItemStack(Material.IRON_BOOTS));

            inv.setItem(0, new ItemStack(Material.IRON_SWORD));
            inv.setItem(1, new ItemStack(Material.FISHING_ROD));

            ItemStack snowballs = new ItemStack(Material.SNOW_BALL);
            snowballs.setAmount(5);
            inv.setItem(2, snowballs);

            ItemStack web = new ItemStack(Material.WEB);
            web.setAmount(5);
            inv.setItem(3, web);

            ItemStack cobble = new ItemStack(Material.COBBLESTONE);
            cobble.setAmount(64);
            inv.setItem(4, cobble);

            inv.setItem(5, new ItemStack(Material.GOLDEN_APPLE));
            inv.setItem(8, new ItemStack(Material.WATER_BUCKET));

            kits.put(player, inv);
            kitArmor.put(player, armor);
        }
    }

    private void printSeconds(Player player, int seconds)
    {
        if (seconds <= 5)
            player.sendMessage(de.marvinleiers.minigameapi.utils.Text.get("countdown").replace("%seconds%", seconds + ""));
        else if (seconds % 5 == 0)
            player.sendMessage(de.marvinleiers.minigameapi.utils.Text.get("countdown").replace("%seconds%", seconds + ""));
    }
}
