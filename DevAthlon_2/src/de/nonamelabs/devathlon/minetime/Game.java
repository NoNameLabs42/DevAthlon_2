package de.nonamelabs.devathlon.minetime;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
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
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import de.nonamelabs.devathlon.Plugin_MineTime;
import de.nonamelabs.devathlon.util.Coordinate;

public class Game implements Listener {
	public static final ChatColor PLUGIN_COLOR = ChatColor.AQUA;
	public static final ChatColor CHATCOLOR = ChatColor.WHITE;
	public static final ChatColor PLUGIN_NAME_COLOR = ChatColor.LIGHT_PURPLE;
	
	public int rooms; //Anzahl der Räume die die Spieler durchgehen müssen
	public int total_time; //Maximale Zeit
	public List<Coordinate> room_list; //Liste der Räume die die Spieler durchgehen
	public Random r = new Random(); //Random
	public boolean stop = false; //Variable ob das Spiel gestoppt ist
	public World w; //Die Welt des Plugins
	public Scoreboard sc; //Das Scoreboard des Plugins
	public String remaining_time; //Die verbleibende Zeit formatiert
	
	public Player p1; //Spieler 1
	public Player p2; //Spieler 2
	public Time p1_time = Time.PRESENT; //Die Position von Spieler 1
	public Time p2_time = Time.PRESENT; //Die Position von Spieler 2
	public int p1_room_number = 0; //Die Raumnummer von Spieler 1
	public int p2_room_number = 0; //Die Raumnummer von Spieler 2
	public Room p1_room; //Der Raum von Spieler 1
	public Room p2_room; //Der Raum von Spieler 2
	
	/**
	 * Aufbauen des Spiels und der Spieler
	 */
	public Game(int rooms, int time, List<Coordinate> room_list) {
		this.rooms = rooms;
		this.total_time = time;
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
	
	/**
	 * Spieler initialisieren etc
	 */
	public void start() {
		initMap();
		
		sc = Bukkit.getScoreboardManager().getNewScoreboard();
		
		setupPlayer(p1, room_list.get(0), false);		
		setupPlayer(p2, room_list.get(0), true);
		
		for (Player p: Bukkit.getOnlinePlayers()) {
			if (!(p1.equals(p) || p2.equals(p))) {
				setupSpectator(p);
			}
		}
		
		Bukkit.getScheduler().runTaskAsynchronously(Plugin_MineTime.Plugin, new Runnable() {
			
			@Override
			public void run() {
				for (int t= 1; t <= total_time && !stop; t++) {
					
					int time = total_time - t;
					String seconds = String.valueOf(time % 60);
					
					for (int i = 0; i < 2 - seconds.length(); i++) {
						seconds = "0" + seconds;
					}
					
					remaining_time = (time / 60) + ":" + seconds;
					
					p1_room.gameTick();
					p2_room.gameTick();
					
					if (time % 30 == 0) {
						sendGameMessage(remaining_time + " verbleibend!");
					}
					
					updateScoreboard();
					
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				
				if (stop) return;
				
				sendGameMessage("Die Zeit ist um!");
				sendGameMessage("Spieler " + (p1_room_number > p2_room_number ? p1.getDisplayName() : p2.getDisplayName()) + " hat das Spiel gewonnen!");
				stop();
			}
		});
	}
	
	/**
	 * Scoreboard updaten
	 */
	public void updateScoreboard() {
		Objective obj;
		
		if (sc.getObjective("MineTime") == null) {
			obj = sc.registerNewObjective("MineTime", "dummy");
		} else {
			obj = sc.getObjective("MineTime");
			obj.unregister();
			obj = sc.registerNewObjective("MineTime", "dummy");
		}
		
		obj.setDisplayName(PLUGIN_NAME_COLOR + "MineTime");
		obj.setDisplaySlot(DisplaySlot.SIDEBAR);
		
		int curr = 0;
		obj.getScore(ChatColor.GOLD + p1.getDisplayName() + ChatColor.WHITE + ": " + (p1_room_number+1)).setScore(curr++);
		obj.getScore(ChatColor.GOLD + p2.getDisplayName() + ChatColor.WHITE + ": " + (p2_room_number+1)).setScore(curr++);
		obj.getScore(ChatColor.RESET + "").setScore(curr++);
		obj.getScore(ChatColor.GOLD + "Levels" + ChatColor.WHITE + ": " + rooms).setScore(curr++);;
		obj.getScore(ChatColor.RESET + "" + ChatColor.RESET).setScore(curr++);
		obj.getScore(ChatColor.GOLD + "Zeit" + ChatColor.WHITE + ": " + remaining_time).setScore(curr++);
	}
	
	/**
	 * Jeden der Räume in der Map für zweiten Spieler duplizieren
	 */
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
	
	/**
	 * Einen Spieler initialisieren
	 */
	public void setupPlayer(Player p, Coordinate spawn, boolean second) {
		p.setGameMode(GameMode.ADVENTURE);
		sendGameMessage("Du bist Spieler!", p);
		p.teleport(new Location(w, spawn.getX()+21, spawn.getY()+ 1 + 8 + (second ? 24 : 0), spawn.getZ()+1));
		if (second) {
			p2_room = new Room(w, spawn, true);
		} else {
			p1_room = new Room(w, spawn, false);
		}
		p.setHealth(20);
		p.setFoodLevel(20);
		
		p.getInventory().clear();
		p.getInventory().setItem(8, Items.getWarpItem());
		
		p.setPlayerTime(6500, false);
		
		p.playEffect(p.getLocation(), Effect.RECORD_PLAY, Material.RECORD_11);
		
		p.setScoreboard(sc);
	}
	
	/**
	 * Einen Spectator initialisieren
	 */
	public void setupSpectator(Player p) {
		p.setGameMode(GameMode.SPECTATOR);
		p.teleport(r.nextBoolean() ? p1 : p2);
		sendGameMessage("Du bist Spectator!", p);
		p.setHealth(20);
		p.setFoodLevel(20);

		p.setPlayerTime(6500, false);
		
		p.getInventory().clear();
		
		p.setScoreboard(sc);
	}
	
	/**
	 * Formatierte Spielnachricht senden
	 */
	public void sendGameMessage(String message) {
		Bukkit.broadcastMessage(ChatColor.GRAY + "[" + PLUGIN_NAME_COLOR + "MineTime" + ChatColor.GRAY + "] " + PLUGIN_COLOR + message);
	}
	
	/**
	 * Formatierte Spielnachricht an einen Spieler senden
	 */
	public void sendGameMessage(String message, Player p) {
		p.sendMessage(ChatColor.GRAY + "[" + PLUGIN_NAME_COLOR + "MineTime" + ChatColor.GRAY + "] " + PLUGIN_COLOR + message);
	}
	
	/**
	 * Spiel beenden ohne besondere Dinge
	 */
	public void stop() {
		stop = true;
		Plugin_MineTime.Plugin.game = null;
		Bukkit.getScheduler().runTask(Plugin_MineTime.Plugin, new Runnable() {
			
			@Override
			public void run() {
				p1_room.unload();
				p2_room.unload();
				
				sc.clearSlot(DisplaySlot.SIDEBAR);
				sc = null;
				
				for (Player p: Bukkit.getOnlinePlayers()) {
					p.teleport(w.getSpawnLocation());
					p.setGameMode(GameMode.ADVENTURE);
					p.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
				}
			}
		});
				
		HandlerList.unregisterAll(this);
	}
	
	/**
	 * Spiel beenden und Sound abspielen
	 */
	public void stop_finish() {
		stop = true;
		Plugin_MineTime.Plugin.game = null;
		Bukkit.getScheduler().runTask(Plugin_MineTime.Plugin, new Runnable() {
			
			@Override
			public void run() {
				p1_room.unload();
				p2_room.unload();
				
				sc.clearSlot(DisplaySlot.SIDEBAR);
				sc = null;
				
				for (Player p: Bukkit.getOnlinePlayers()) {
					p.teleport(w.getSpawnLocation());
					p.setGameMode(GameMode.ADVENTURE);
					p.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
				}

				p1.playEffect(p1.getLocation(), Effect.RECORD_PLAY, Material.RECORD_10);
				p2.playEffect(p2.getLocation(), Effect.RECORD_PLAY, Material.RECORD_10);
			}
		});
				
		HandlerList.unregisterAll(this);
	}
	
	/**
	 * Bereits synchrones beenden des Spiels
	 */
	public void s_stop() {
		stop = true;
		Plugin_MineTime.Plugin.game = null;
		p1_room.unload();
		p2_room.unload();
		sc.clearSlot(DisplaySlot.SIDEBAR);
		sc = null;
		for (Player p: Bukkit.getOnlinePlayers()) {
			p.teleport(w.getSpawnLocation());
			p.setGameMode(GameMode.ADVENTURE);
			p.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
		}
				
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
		if (event.getAction() != Action.PHYSICAL) {
			event.setCancelled(true);
		}
		
		ItemStack ic = event.getItem();		
		if (ic != null) {
			//Zeitmaschine
			if ((event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) && ic.equals(Items.getWarpItem())) {
				if (w.getBlockAt(event.getPlayer().getLocation().add(0, -1, 0)).getType() == Material.GOLD_BLOCK || w.getBlockAt(event.getPlayer().getLocation().add(0, -2, 0)).getType() == Material.GOLD_BLOCK) {
					event.getPlayer().openInventory(Items.getWarpInventory(event.getPlayer().equals(p1) ? p1_time : p2_time));
				} else {
					sendGameMessage("Du hast nicht genug Energie, du musst auf einem Goldblock stehen um die Zeitmaschine zu benutzen!", event.getPlayer());
				}
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
	
	/**
	 * Chat-Nachrichten formatieren
	 */
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
	
	/**
	 * teleport
	 */
	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		event.setCancelled(true);
		
		Inventory iv = event.getInventory();
		Player p = (Player) event.getWhoClicked();
		ItemStack ic = event.getCurrentItem();
		
		if (iv == null || p == null || ic == null) {
			return;
		}
		
		//Teleport
		if (iv.getName().equals(Items.getWarpInventory(Time.PRESENT).getName())) {
			int teleport = 0;
			switch (p.equals(p1) ? p1_time : p2_time) {
			case PRESENT:
				teleport = -8;
				break;
			case FUTURE:
				teleport = -16;
				break;
			case PAST:
				teleport = 0;
				break;
			}
			
			if (ic.equals(Items.getYesterdayItem())) {
				p.closeInventory();
				teleport += 0;
				if (p.equals(p1)) {
					p1_time = Time.PAST;
					p1.setPlayerTime(7500, false);
				} else {
					p2_time = Time.PAST;
					p2.setPlayerTime(7500, false);
				}
				p.teleport(p.getLocation().add(0, teleport, 0));
				p.playSound(p.getLocation(), Sound.FIREWORK_BLAST, 1, 1);
			} else if (ic.equals(Items.getNowItem())) {
				p.closeInventory();
				teleport += 8;
				if (p.equals(p1)) {
					p1_time = Time.PRESENT;
					p1.setPlayerTime(6500, false);
				} else {
					p2_time = Time.PRESENT;
					p2.setPlayerTime(6500, false);
				}
				p.teleport(p.getLocation().add(0, teleport, 0));
				p.playSound(p.getLocation(), Sound.FIREWORK_BLAST, 1, 1);
			} else if (ic.equals(Items.getFutureItem())) {
				p.closeInventory();
				teleport += 16;
				if (p.equals(p1)) {
					p1_time = Time.FUTURE;
					p1.setPlayerTime(7000, false);
				} else {
					p2_time = Time.FUTURE;
					p2.setPlayerTime(7000, false);
				}
				p.teleport(p.getLocation().add(0, teleport, 0));
				p.playSound(p.getLocation(), Sound.FIREWORK_BLAST, 1, 1);
			}
		}
	}
	
	/**
	 * Level_finish und Rooms
	 */
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		Player p = event.getPlayer();
		if (p == null) {
			return;
		}
		
		if (p.equals(p1) || p.equals(p2)) {
			
			if (p.equals(p1)) {
				p1_room.player_move(p);
			} else {
				p2_room.player_move(p);
			}
			
			if (w.getBlockAt(event.getPlayer().getLocation().add(0, -1, 0)).getType() == Material.EMERALD_BLOCK || w.getBlockAt(event.getPlayer().getLocation().add(0, -2, 0)).getType() == Material.EMERALD_BLOCK) {
				
				if (p.equals(p1)) {
					p1_room_number++;
					if (p1_room_number == rooms) {
						sendGameMessage("Du hast das Spiel gewonnen", p);
						sendGameMessage(p.getDisplayName() + " hat das Spiel gewonnen!");
						
						stop_finish();
					} else {
						p1_room.unload();
						
						Material m;
						while (true) {
							m = Material.values()[r.nextInt(Material.values().length)];
							if (m == Material.GOLD_RECORD || m == Material.RECORD_3 || m == Material.RECORD_4 || m == Material.RECORD_5 || m == Material.RECORD_6 || m == Material.RECORD_7 || m == Material.RECORD_8) {
								break;
							}
						}
						
						Coordinate spawn = room_list.get(p1_room_number);
						p1_time = Time.PRESENT;
						p1.setPlayerTime(6500, true);
						p.teleport(new Location(w, spawn.getX()+21, spawn.getY()+ 1 + 8, spawn.getZ()+1));
						p.playEffect(p.getLocation(), Effect.RECORD_PLAY, m);
						p1_room = new Room(w, spawn, false);
						
						sendGameMessage("Du hast Test " + (p1_room_number) + " erfolgreich absolviert!", p);
						p.playSound(p.getLocation(), Sound.LEVEL_UP, 1, 1);
						p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 40, 10000, false, false), true);
					}
				} else {
					p2_room_number++;
					if (p2_room_number == rooms) {
						sendGameMessage("Du hast das Spiel gewonnen", p);
						sendGameMessage(p.getDisplayName() + " hat das Spiel gewonnen!");
						
						stop_finish();
					} else {
						p2_room.unload();
						
						Material m;
						while (true) {
							m = Material.values()[r.nextInt(Material.values().length)];
							if (m == Material.GOLD_RECORD || m == Material.RECORD_3 || m == Material.RECORD_4 || m == Material.RECORD_5 || m == Material.RECORD_6 || m == Material.RECORD_7 || m == Material.RECORD_8) {
								break;
							}
						}
						
						Coordinate spawn = room_list.get(p2_room_number);
						p2_time = Time.PRESENT;
						p2.setPlayerTime(6500, false);
						p.teleport(new Location(w, spawn.getX()+21, spawn.getY()+ 1 + 8 + 24, spawn.getZ()+1));
						p.playEffect(p.getLocation(), Effect.RECORD_PLAY, m);
						p2_room = new Room(w, spawn, true);
						
						sendGameMessage("Du hast Test " + (p2_room_number) + " erfolgreich absolviert!", p);
						p.playSound(p.getLocation(), Sound.LEVEL_UP, 1, 1);
						
						p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 40, 10000, false, false), true);
					}
				}
			}
		}
	}
}
