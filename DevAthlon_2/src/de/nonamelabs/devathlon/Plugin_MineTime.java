package de.nonamelabs.devathlon;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import de.nonamelabs.devathlon.minetime.Game;
import de.nonamelabs.devathlon.minetime.Items;
import de.nonamelabs.devathlon.util.Coordinate;

public class Plugin_MineTime extends JavaPlugin implements Listener {
	public static Plugin_MineTime Plugin;
	public static Logger logger;
	
	public Game game;
	public List<Coordinate> room_list = new ArrayList<Coordinate>();
	
	public void onEnable() {
		Plugin = this;
		logger = getLogger();
	
		getServer().getPluginManager().registerEvents(this, this);
		
		loadConfig();
		logger.info("Plugin wurde gestartet!");
	}
	
	public void onDisable() {
		
		if (game != null) {
			game.s_stop();
		}
		
		save_Config();
		logger.info("Plugin wurde gestoppt!");
	}
	
	public void save_Config() {
		FileConfiguration config = getConfig();
		
		config.set("rooms", room_list.size());
		
		for (int i = 1; i <= room_list.size(); i++) {
			room_list.get(i-1).savetoConfig(config, i);
		}
		
		saveConfig();
	}
	
	public void loadConfig() {
		FileConfiguration config = getConfig();
				
		int size = config.getInt("rooms");
		
		for (int i = 1; i <= size; i++) {
			room_list.add(new Coordinate(config, i));
		}
		
		saveConfig();
	}
	
	public boolean onCommand(CommandSender sender, Command command, String commandlabel, String[] args) {
		if (commandlabel.equalsIgnoreCase("startgame")) {
			if (args.length < 2) {
				sender.sendMessage(ChatColor.RED + "Nicht genügend Argumente!");
				return false;
			}
			
			int rooms = 0;
			int time = 0;
			
			try {
				rooms = Integer.valueOf(args[0]);
			} catch (NumberFormatException e) {
				sender.sendMessage(ChatColor.RED + "Das erste Argument muss die Anzahl der Räume sein!");
				return false;
			}
			
			try {
				time = Integer.valueOf(args[1]);
			} catch (NumberFormatException e) {
				sender.sendMessage(ChatColor.RED + "Das zweite Argument muss die maximale Zeit sein!");
				return false;
			}
			
			if (rooms < 1) {
				sender.sendMessage(ChatColor.RED + "Es muss mindestens einen Raum geben!");
				return false;
			}
			
			if (rooms > room_list.size()) {
				sender.sendMessage(ChatColor.RED + "Es wurden noch nicht genügend Räume hinzugefügt! Mit /selector bekommst du ein Item mit dem du neue Räume hinzufügst!");
				return false;
			}
			
			if (time < 5) {
				sender.sendMessage(ChatColor.RED + "Es müssen mindestends 5 Sekunden Zeit sein!");
				return false;
			}
			
			if (Bukkit.getOnlinePlayers().size() < 2) {
				sender.sendMessage(ChatColor.RED + "Es müssen mindestends 2 Spieler online sein!");
				return false;
			}
			
			if (game!=null) {
				sender.sendMessage(ChatColor.RED + "Das Spiel ist bereits gestartet!");
			} else {
				sender.sendMessage(ChatColor.GREEN + "Du hast das Spiel gestartet!");
				game = new Game(rooms, time, room_list);
			}
			return true;
		} else if (commandlabel.equalsIgnoreCase("stopgame")) {
						
			if (game==null) {
				sender.sendMessage(ChatColor.RED + "Das Spiel war nicht gestartet!");
			} else {
				game.s_stop();
				sender.sendMessage(ChatColor.GREEN + "Du hast das Spiel gestoppt!");
			}
			return true;
		} else if (commandlabel.equalsIgnoreCase("selector")) {
			if (sender instanceof Player) {
				((Player)sender).getInventory().addItem(Items.getSelectorItem());
				sender.sendMessage(ChatColor.GREEN + "Du hast das Selector-Item erhalten! Rechtsklicke die Nord_West_Tiefste Ecke des Raums (der Raum muss 24 * 24 * 24 Blöcke groß sein und 3 Etagen beinhalten) um einen Raum hinzuzufügen!");
				return true;	
			} else {
				sender.sendMessage(ChatColor.RED + "Du bist kein Spieler!");
				return false;
			}
		} else if (commandlabel.equalsIgnoreCase("resetRooms")) {
			room_list.clear();
			sender.sendMessage(ChatColor.GREEN + "Alle Räume wurden zurückgesetzt!");
			return true;
		} 
		
		return false;
	}
	
	@EventHandler
	public void onPlayerAction(PlayerInteractEvent event) {
		ItemStack item = event.getItem();
		if (item == null) return;
		
		if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
			if (item.equals(Items.getSelectorItem())) {
				event.setCancelled(true);
				addRoom(event.getPlayer(), event.getClickedBlock().getLocation());
			}
		}
	}
	
	public void addRoom(Player p, Location l) {
		Coordinate c = new Coordinate(l.getBlockX(), l.getBlockY(), l.getBlockZ());
		room_list.add(c);
		p.sendMessage(ChatColor.GREEN + "Es wurde ein Raum bei " + c + " hinzugefügt! Es gibt jetzt " + room_list.size() + " Räume!");
	}
}
