package de.nonamelabs.devathlon.minetime;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import de.nonamelabs.devathlon.Plugin_MineTime;
import de.nonamelabs.devathlon.util.Coordinate;

public class Game implements Listener {
	public static final ChatColor PLUGIN_COLOR = ChatColor.AQUA;
	public static final ChatColor CHATCOLOR = ChatColor.WHITE;
	public static final ChatColor PLUGIN_NAME_COLOR = ChatColor.LIGHT_PURPLE;
	
	public int rooms;
	public int time;
	public List<Coordinate> room_list;
	public Random r = new Random();
	public Player p1;
	public Player p2;
	public Time p1_time = Time.PRESENT;
	public Time p2_time = Time.PRESENT;
	public World w;
	
	public Game(int rooms, int time, List<Coordinate> room_list) {
		this.rooms = rooms;
		this.time = time;
		this.room_list = new ArrayList<Coordinate>();
		
		for (int i = 0; i < rooms; i++) {
			Coordinate c;
			do {
				c = room_list.get(r.nextInt(room_list.size()));
			} while(this.room_list.contains(c));
			
			this.room_list.add(c);
		}
		
		p1 = (Player) Bukkit.getOnlinePlayers().toArray()[r.nextInt(Bukkit.getOnlinePlayers().size())];
		
		w = p1.getWorld();
		
		do {
			p2 = (Player) Bukkit.getOnlinePlayers().toArray()[r.nextInt(Bukkit.getOnlinePlayers().size())];
		} while (p2.equals(p1));
		
		Bukkit.getPluginManager().registerEvents(this, Plugin_MineTime.Plugin);
		
		start();
	}
	
	public void start() {
		initMap();
		
		setupPlayer(p1, room_list.get(0), false);		
		setupPlayer(p2, room_list.get(0), true);
		
		for (Player p: Bukkit.getOnlinePlayers()) {
			if (!(p1.equals(p) || p2.equals(p))) {
				setupSpectator(p);
			}
		}
	}
	
	@SuppressWarnings("deprecation")
	public void initMap() {
		for (Coordinate c: room_list) {
			for (int x = c.getX(); x <= c.getX()+23; x++) {
				for (int y = c.getY(); y <= c.getY()+23; y++) {
					for (int z = c.getZ(); z <= c.getZ()+23; z++) {
						Block to = w.getBlockAt(x, y+24, z);
						Block from = w.getBlockAt(x, y, z);
						to.setType(from.getType());
						to.setData(from.getData());
					}	
				}
			}
		}
	}
	
	public void setupPlayer(Player p, Coordinate spawn, boolean second) {
		p.setGameMode(GameMode.ADVENTURE);
		sendGameMessage("Du bist Spieler!", p);
		p.teleport(new Location(w, spawn.getX()+21, spawn.getY()+ 1 + 8 + (second ? 24 : 0), spawn.getZ()+1));
		
		p.getInventory().clear();
		p.getInventory().setItem(8, Items.getWarpItem());
	}
	
	public void setupSpectator(Player p) {
		p.setGameMode(GameMode.SPECTATOR);
		p.teleport(r.nextBoolean() ? p1 : p2);
		sendGameMessage("Du bist Spectator!", p);

		p.getInventory().clear();
	}
	
	public void sendGameMessage(String message) {
		Bukkit.broadcastMessage(ChatColor.GRAY + "[" + PLUGIN_NAME_COLOR + "MineTime" + ChatColor.GRAY + "] " + PLUGIN_COLOR + message);
	}
	
	public void sendGameMessage(String message, Player p) {
		p.sendMessage(ChatColor.GRAY + "[" + PLUGIN_NAME_COLOR + "MineTime" + ChatColor.GRAY + "] " + PLUGIN_COLOR + message);
	}
	
	public void stop() {
		Plugin_MineTime.Plugin.game = null;
		Bukkit.getScheduler().runTask(Plugin_MineTime.Plugin, new Runnable() {
			
			@Override
			public void run() {
				for (Player p: Bukkit.getOnlinePlayers()) {
					p.teleport(w.getSpawnLocation());
					p.setGameMode(GameMode.ADVENTURE);
				}
			}
		});
				
		HandlerList.unregisterAll(this);
	}
	
	
	//Events! ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		event.setJoinMessage("");
		setupSpectator(event.getPlayer());
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerQuitEvent event) {
		event.setQuitMessage("");
		if (p1.equals(event.getPlayer()) || p2.equals(event.getPlayer())) {
			sendGameMessage(event.getPlayer().getName() + " hat das Spiel verlassen!");
			sendGameMessage("Das Spiel ist zu Ende!");
			stop();
		}
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		event.setCancelled(true);
		ItemStack ic = event.getItem();		
		if (ic != null) {
			if ((event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) && ic.equals(Items.getWarpItem())) {
				event.getPlayer().openInventory(Items.getWarpInventory(event.getPlayer().equals(p1) ? p1_time : p2_time));
			}
		}
	}
	
	@EventHandler
	public void onPlayerDrop(PlayerDropItemEvent event) {
		event.setCancelled(true);
	}
	
	@EventHandler
	public void onPlayerPickup(PlayerPickupItemEvent event) {
		event.setCancelled(true);
	}
	
	@EventHandler
	public void onPlayerLooseHunger(FoodLevelChangeEvent event) {
		event.setCancelled(true);
	}
	
	@EventHandler
	public void onPlayerChat(AsyncPlayerChatEvent event) {
		if (event.getMessage().contains("%")) {
			event.setCancelled(true);
			return;
		}

		if (!(p1.equals(event.getPlayer()) || p2.equals(event.getPlayer()))) {
			event.setCancelled(true);
			sendGameMessage("Du bist Spectator, du kannst nichts in den Chat schreiben", event.getPlayer());
			return;
		}
		
		Player p = event.getPlayer();
		String color = ChatColor.getLastColors(p.getDisplayName());
		event.setFormat(color + "%1$s" + CHATCOLOR + ": " + "%2$s");
	}
	
	@EventHandler
	public void onPlayerDamage(EntityDamageEvent event) {
		event.setCancelled(true);
	}
	
	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		event.setCancelled(true);
	}
}
