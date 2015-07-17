package de.nonamelabs.devathlon.util;

import org.bukkit.util.Vector;

public enum Direction {
	NORTH(new Vector(0, 0, -1)), SOUTH(new Vector(0, 0, 1)), WEST(new Vector(-1, 0, 0)), EAST(new Vector(1, 0, 0)), UP(new Vector(0, 1, 0)), DOWN(new Vector(0, -1, 0));
	
	private Vector v;
	
	Direction(Vector v) {
		this.v = v;
	}
	
	public Vector getVector() {
		return v;
	}
}
