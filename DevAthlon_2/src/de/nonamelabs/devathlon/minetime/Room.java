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
	
	public Map<Byte, Block> glass_blocks = new HashMap<Byte, Block>();
	public Map<Byte, Block> carpets = new HashMap<Byte, Block>();
	public Map<Byte, Integer> timers = new HashMap<Byte, Integer>();
	public Map<Byte, Boolean> extended = new HashMap<Byte, Boolean>();
	
	public List<Block> beacons = new ArrayList<Block>();
	public List<Block> gold_blocks = new ArrayList<Block>();
	
	@SuppressWarnings("deprecation")
	public Room(World w, Coordinate c, boolean top) {
		this.w = w;
		this.c = c;
		this.top = top;
		
		for (int x = c.getX(); x <= c.getX() + 23; x++) {
			for (int y = c.getY() + (top ? 24 : 0); y < c.getY() + 23 + (top ? 24 : 0); y++) {
				for (int z = c.getZ(); z <= c.getZ() + 23; z++) {
					Block b = w.getBlockAt(x, y, z);
					if (b.getType() == Material.STAINED_GLASS) {
						glass_blocks.put(b.getData(), b);
						timers.put(b.getData(), 0);
						extended.put(b.getData(), false);
					} else if (b.getType() == Material.CARPET) {
						carpets.put(b.getData(), b);
					} else if (b.getType() == Material.BEACON) {
						beacons.add(b);
						b.setType(Material.AIR);
					} else if (b.getType() == Material.GOLD_BLOCK) {
						gold_blocks.add(b);
					}
				}
			}
		}
	}
	
	public void unload() {
		synchronized (timers) {
			for (byte data: timers.keySet()) {
				if (extended.get(data)) {
					Direction d = getDirection(glass_blocks.get(data));
					
					boolean wand = false;
					Location l = glass_blocks.get(data).getLocation().add(d.getVector());
					
					while (!wand) {
						Block change = w.getBlockAt(l);
						change.setType(Material.AIR);
						
						l = l.add(d.getVector());
						if (!(w.getBlockAt(l).getType() == Material.AIR || w.getBlockAt(l).getType() == Material.STAINED_GLASS)) {
							wand = true;
						}
					}
					extended.put(data, false);
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
				}
				for (Block b: gold_blocks) {
					w.playEffect(b.getLocation().add(0, 2, 0), Effect.FLYING_GLYPH, 100);
					w.playEffect(b.getLocation().add(0, 2, 0), Effect.FLYING_GLYPH, 100);
				}
				synchronized (timers) {
					for (final byte data: timers.keySet()) {
						if (timers.get(data) > 0) {
							timers.put(data, timers.get(data)-1);
							if (!extended.get(data)) {
								extended.put(data, true);
								
								Bukkit.getScheduler().runTaskAsynchronously(Plugin_MineTime.Plugin, new Runnable() {
									
									@Override
									public void run() {
										Direction d = getDirection(glass_blocks.get(data));
										
										boolean wand = false;
										Location l = glass_blocks.get(data).getLocation().add(d.getVector());
										
										while (!wand) {
											final Block change = w.getBlockAt(l);
											
											Bukkit.getScheduler().runTask(Plugin_MineTime.Plugin, new Runnable() {
												@Override
												public void run() {
													change.setType(Material.STAINED_GLASS);
													change.setData(data);
												}
											});
											
											l = l.add(d.getVector());
											if (!(w.getBlockAt(l).getType() == Material.AIR || w.getBlockAt(l).getType() == Material.STAINED_GLASS)) {
												wand = true;
											}
											
											try {
												Thread.sleep(50);
											} catch (InterruptedException e) {
												e.printStackTrace();
											}
										}
									}
								});
							}							
						} else {		
							if (extended.get(data)) {
								extended.put(data, false);
								
								Bukkit.getScheduler().runTaskAsynchronously(Plugin_MineTime.Plugin, new Runnable() {
									
									@Override
									public void run() {
										Direction d = getDirection(glass_blocks.get(data));
										
										boolean wand = false;
										Location l = glass_blocks.get(data).getLocation().add(d.getVector());
										
										while (!wand) {
											final Block change = w.getBlockAt(l);
											
											Bukkit.getScheduler().runTask(Plugin_MineTime.Plugin, new Runnable() {
												@Override
												public void run() {
													change.setType(Material.AIR);
												}
											});
											
											l = l.add(d.getVector());
											if (!(w.getBlockAt(l).getType() == Material.AIR || w.getBlockAt(l).getType() == Material.STAINED_GLASS)) {
												wand = true;
											}
											
											try {
												Thread.sleep(100);
											} catch (InterruptedException e) {
												e.printStackTrace();
											}
										}
									}
								});
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
			if (block.getType() == Material.STAINED_GLASS || block.getType() == Material.AIR) {
				return direc;
			}
		}
		
		return Direction.NORTH;
	}
}
