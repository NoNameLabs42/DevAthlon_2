package de.nonamelabs.devathlon.minetime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import de.nonamelabs.devathlon.Plugin_MineTime;
import de.nonamelabs.devathlon.util.Coordinate;
import de.nonamelabs.devathlon.util.Direction;

public class Room {
	public World w;
	public boolean top;
	public Coordinate c;
	
	public Map<Byte, Block> wools = new HashMap<Byte, Block>();
	public Map<Byte, Block> carpets = new HashMap<Byte, Block>();
	public Map<Byte, Integer> timers = new HashMap<Byte, Integer>();
	public List<Block> beacons = new ArrayList<Block>();
	
	@SuppressWarnings("deprecation")
	public Room(World w, Coordinate c, boolean top) {
		this.w = w;
		this.c = c;
		this.top = top;
		
		for (int x = c.getX(); x <= c.getX() + 23; x++) {
			for (int y = c.getY() + (top ? 24 : 0); y < c.getY() + 23 + (top ? 24 : 0); y++) {
				for (int z = c.getZ(); z <= c.getZ() + 23; z++) {
					Block b = w.getBlockAt(x, y, z);
					if (b.getType() == Material.WOOL) {
						wools.put(b.getData(), b);
						timers.put(b.getData(), 0);
					} else if (b.getType() == Material.CARPET) {
						carpets.put(b.getData(), b);
					} else if (b.getType() == Material.BEACON) {
						beacons.add(b);
						b.setType(Material.AIR);
					}
				}
			}
		}
	}
	
	public void unload() {
		synchronized (timers) {
			for (byte data: timers.keySet()) {
				Direction d = getDirection(wools.get(data));
				
				boolean wand = false;
				Location l = wools.get(data).getLocation().add(d.getVector());
				
				while (!wand) {
					Block change = w.getBlockAt(l);
					change.setType(Material.AIR);
					
					l = l.add(d.getVector());
					if (!(w.getBlockAt(l).getType() == Material.AIR || w.getBlockAt(l).getType() == Material.WOOL)) {
						wand = true;
					}
				}
			}
		}
		
		for (Block b: beacons) {
			b.setType(Material.BEACON);
		}
	}
	
	@SuppressWarnings("deprecation")
	public void gameTick() {
		Bukkit.getScheduler().runTask(Plugin_MineTime.Plugin, new Runnable() {
			
			@Override
			public void run() {
				
				for (Block b: beacons) {
					w.playEffect(b.getLocation(), Effect.ENDER_SIGNAL, 0);
					w.playEffect(b.getLocation(), Effect.FLYING_GLYPH, 0);
				}
				
				synchronized (timers) {
					for (byte data: timers.keySet()) {
						if (timers.get(data) > 0) {
							timers.put(data, timers.get(data)-1);
							
							Direction d = getDirection(wools.get(data));
							
							boolean wand = false;
							Location l = wools.get(data).getLocation().add(d.getVector());
							
							while (!wand) {
								Block change = w.getBlockAt(l);
								change.setType(Material.WOOL);
								change.setData(data);
								
								l = l.add(d.getVector());
								if (!(w.getBlockAt(l).getType() == Material.AIR || w.getBlockAt(l).getType() == Material.WOOL)) {
									wand = true;
								}
							}
							
						} else {					
							Direction d = getDirection(wools.get(data));
							
							boolean wand = false;
							Location l = wools.get(data).getLocation().add(d.getVector());
							
							while (!wand) {
								Block change = w.getBlockAt(l);
								change.setType(Material.AIR);
								
								l = l.add(d.getVector());
								if (!(w.getBlockAt(l).getType() == Material.AIR || w.getBlockAt(l).getType() == Material.WOOL)) {
									wand = true;
								}
							}
						}
					}
				}
			}
		});
	}
	
	@SuppressWarnings("deprecation")
	public void player_move(Player p)  {
		if (w.getBlockAt(p.getLocation().add(0, -1, 0)).getType() == Material.PISTON_BASE) {
			p.setVelocity(new Vector(0, 0.75, 0));
			p.playSound(p.getLocation(), Sound.PISTON_EXTEND, 1, 1);
		} else 	if (w.getBlockAt(p.getLocation().add(0, -1, 0)).getType() == Material.PISTON_STICKY_BASE) {
			p.setVelocity(new Vector(0, 0.75, 0));
			p.playSound(p.getLocation(), Sound.PISTON_EXTEND, 1, 1);
		} else if (w.getBlockAt(p.getLocation()).getType() == Material.CARPET) {
			Block b = w.getBlockAt(p.getLocation());
			synchronized (timers) {
				timers.put(b.getData(), 6);
			}
			gameTick();
		}
	}
	
	public Direction getDirection(Block b) {
		for (Direction direc: Direction.values()) {
			Block block = b.getWorld().getBlockAt(b.getLocation().add(direc.getVector()));
			if (block.getType() == Material.WOOL || block.getType() == Material.AIR) {
				return direc;
			}
		}
		
		return Direction.NORTH;
	}
}
