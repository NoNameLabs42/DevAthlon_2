package de.nonamelabs.devathlon.minetime;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import de.nonamelabs.devathlon.util.Coordinate;

public class Room {
	public World w;
	public boolean top;
	public Coordinate c;
	
	public Room(World w, Coordinate c, boolean top) {
		this.w = w;
		this.c = c;
		this.top = top;
	}
	
	public void unload() {
		
	}
	
	public void gameTick() {
		
	}
	
	public void player_move(Player p)  {
		if (w.getBlockAt(p.getLocation().add(0, -1, 0)).getType() == Material.PISTON_BASE) {
			p.setVelocity(new Vector(0, 0.5, 0));
			p.playSound(p.getLocation(), Sound.PISTON_EXTEND, 1, 1);
		}
	}
}
