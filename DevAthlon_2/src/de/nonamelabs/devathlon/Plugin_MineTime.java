package de.nonamelabs.devathlon;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import de.nonamelabs.devathlon.minetime.Game;
import de.nonamelabs.devathlon.util.Coordinate;

public class Plugin_MineTime extends JavaPlugin {
	public static Plugin_MineTime Plugin;
	public static Logger logger;
	
	public Game game;
	public List<Coordinate> rooms = new ArrayList<Coordinate>();
	
	public void onEnable() {
		Plugin = this;
		logger = getLogger();
	
		loadConfig();
		logger.info("Plugin wurde gestartet!");
	}
	
	public void onDisable() {
		save_Config();
		logger.info("Plugin wurde gestoppt!");
	}
	
	public void save_Config() {
		FileConfiguration config = getConfig();
		
		
		saveConfig();
	}
	
	public void loadConfig() {
		FileConfiguration config = getConfig();
				
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
			
			if (game!=null) {
				sender.sendMessage(ChatColor.RED + "Das Spiel ist bereits gestartet!");
			} else {
				game = new Game(rooms, time);
				sender.sendMessage(ChatColor.GREEN + "Du hast das Spiel gestartet!");
			}
			return true;
		} else if (commandlabel.equalsIgnoreCase("stopgame")) {
						
			if (game==null) {
				sender.sendMessage(ChatColor.RED + "Das Spiel war nicht gestartet!");
			} else {
				game.stop();
				game = null;
				sender.sendMessage(ChatColor.GREEN + "Du hast das Spiel gestoppt!");
			}
			return true;
		} else if (commandlabel.equalsIgnoreCase("addRoom")) {
			if (!(sender instanceof Player)) {
				
				
				return true;	
			} else {
				sender.sendMessage(ChatColor.RED + "Du bist kein Spieler!");
				return false;
			}
		} else if (commandlabel.equalsIgnoreCase("resetRooms")) {
			rooms.clear();
			sender.sendMessage(ChatColor.GREEN + "Alle Raeume wurden zurueckgesetzt!");
			return true;
		} 
		
		return false;
	}
}
