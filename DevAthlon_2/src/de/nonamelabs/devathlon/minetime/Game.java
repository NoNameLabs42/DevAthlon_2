package de.nonamelabs.devathlon.minetime;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;

import de.nonamelabs.devathlon.Plugin_MineTime;
import de.nonamelabs.devathlon.util.Coordinate;

public class Game implements Listener {
	
	public int rooms;
	public int time;
	public List<Coordinate> room_list;
	public Random r = new Random();
	public Player p1;
	public Player p2;
	
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
		
		do {
			p2 = (Player) Bukkit.getOnlinePlayers().toArray()[r.nextInt(Bukkit.getOnlinePlayers().size())];
		} while (p2.equals(p1));
		
		Bukkit.getPluginManager().registerEvents(this, Plugin_MineTime.Plugin);
	}
	
	public void stop() {
		HandlerList.unregisterAll(this);
	}
	
	
	//Events! ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	@EventHandler
	public void onPlayerDrop(PlayerDropItemEvent event) {
		event.setCancelled(true);
	}
	
	@EventHandler
	public void onPlayerPickup(PlayerPickupItemEvent event) {
		event.setCancelled(true);
	}
	
	@EventHandler
	public void onPlayerLooseHunger()
}
