package de.nonamelabs.devathlon;

import java.util.logging.Logger;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class Plugin_MineTime extends JavaPlugin {
	public static Plugin_MineTime Plugin;
	public static Logger logger;
	
	public void onEnable() {
		Plugin = this;
		logger = getLogger();
	
		logger.info("Plugin wurde gestartet!");
	}
	
	public void onDisable() {
		logger.info("Plugin wurde gestoppt!");
	}
	
	public boolean onCommand(CommandSender sender, Command command, String commandlabel, String[] args) {
		
		return false;
	}
}
