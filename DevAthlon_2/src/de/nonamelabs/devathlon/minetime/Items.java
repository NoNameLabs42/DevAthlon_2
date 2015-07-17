package de.nonamelabs.devathlon.minetime;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class Items {
	public static ItemStack getWarpItem() {
		ItemStack item = new ItemStack(Material.WATCH);
		List<String> lore = new ArrayList<String>();
		lore.add(ChatColor.LIGHT_PURPLE + "A Link between times");
		lore.add(ChatColor.LIGHT_PURPLE + "Kostet 123.457.000 Kekse");
		lore.add(ChatColor.LIGHT_PURPLE + "Produziert 98.765 Kekse pro Sekunde");
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(ChatColor.BLUE + "Zeitmaschine");
		meta.setLore(lore);
		item.setItemMeta(meta);

		return item;
	}
	
	public static ItemStack getFutureItem() {
		ItemStack item = new ItemStack(Material.NETHER_STAR);
		List<String> lore = new ArrayList<String>();
		lore.add(ChatColor.LIGHT_PURPLE + "Zur�ck in die Zukunft III");
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(ChatColor.BLUE + "Ab in die Zukunft");
		meta.setLore(lore);
		item.setItemMeta(meta);

		return item;
	}
	
	public static ItemStack getNowItem() {
		ItemStack item = new ItemStack(Material.EMERALD);
		List<String> lore = new ArrayList<String>();
		lore.add(ChatColor.LIGHT_PURPLE + "Zur�ck in die Zukunft II");
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(ChatColor.BLUE + "Ab in das Jetzt");
		meta.setLore(lore);
		item.setItemMeta(meta);

		return item;
	}
	
	public static ItemStack getYesterdayItem() {
		ItemStack item = new ItemStack(Material.DIAMOND);
		List<String> lore = new ArrayList<String>();
		lore.add(ChatColor.LIGHT_PURPLE + "Zur�ck in die Zukunft I");
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(ChatColor.BLUE + "Ab in die Vergangenheit");
		meta.setLore(lore);
		item.setItemMeta(meta);

		return item;
	}
	
	public static ItemStack getSelectorItem() {
		ItemStack item = new ItemStack(Material.WOOD_HOE);
		List<String> lore = new ArrayList<String>();
		lore.add(ChatColor.LIGHT_PURPLE + "Bestimte Ecke des Raumes schlagen um Raum hinzu zu f�gen");
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(ChatColor.BLUE + "RoomAdd");
		meta.setLore(lore);
		item.setItemMeta(meta);

		return item;
	}
}
