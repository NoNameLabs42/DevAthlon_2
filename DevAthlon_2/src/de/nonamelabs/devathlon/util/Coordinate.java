package de.nonamelabs.devathlon.util;

import org.bukkit.configuration.file.FileConfiguration;

/**
 * Positionen die man leicht in Config speichern und auslesen kann
 */
public class Coordinate {
	public int x, y, z;
	
	public Coordinate(int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public Coordinate(FileConfiguration config, int id) {
		this.x = config.getInt("room" + id + "_x");
		this.y = config.getInt("room" + id + "_y");
		this.z = config.getInt("room" + id + "_z");
	}
	
	public int getX() {
		return x;
	}
	public void setX(int x) {
		this.x = x;
	}
	public int getY() {
		return y;
	}
	public void setY(int y) {
		this.y = y;
	}
	public int getZ() {
		return z;
	}
	public void setZ(int z) {
		this.z = z;
	}	
	
	@Override
	public String toString() {
		return "(" + this.x + ", " + this.y + ", " + this.z + ")";
	}
	
	public void savetoConfig(FileConfiguration config, int id) {
		config.set("room" + id + "_x", x);
		config.set("room" + id + "_y", y);
		config.set("room" + id + "_z", z);
	}
}
