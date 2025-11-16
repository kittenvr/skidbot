package greenscripter.minecraft.resources;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import greenscripter.minecraft.play.inventory.Slot;

public class ItemHelper {

	public static final String[] LOG = {
		"minecraft:oak_log",
		"minecraft:spruce_log", 
		"minecraft:birch_log",
		"minecraft:jungle_log",
		"minecraft:acacia_log",
		"minecraft:dark_oak_log",
		"minecraft:mangrove_log",
		"minecraft:cherry_log",
		"minecraft:crimson_stem",
		"minecraft:warped_stem"
	};

	public static final String[] LEAVES = {
		"minecraft:oak_leaves",
		"minecraft:spruce_leaves", 
		"minecraft:birch_leaves",
		"minecraft:jungle_leaves",
		"minecraft:acacia_leaves",
		"minecraft:dark_oak_leaves",
		"minecraft:mangrove_leaves",
		"minecraft:cherry_leaves"
	};
	
	public static final String[] SAPLINGS = {
		"minecraft:oak_sapling",
		"minecraft:spruce_sapling", 
		"minecraft:birch_sapling",
		"minecraft:jungle_sapling",
		"minecraft:acacia_sapling",
		"minecraft:dark_oak_sapling",
		"minecraft:mangrove_propagule",
		"minecraft:cherry_sapling"
	};
	
	public static final String[] WOOL = {
		"minecraft:white_wool",
		"minecraft:orange_wool", 
		"minecraft:magenta_wool",
		"minecraft:light_blue_wool",
		"minecraft:yellow_wool",
		"minecraft:lime_wool",
		"minecraft:pink_wool",
		"minecraft:gray_wool",
		"minecraft:light_gray_wool",
		"minecraft:cyan_wool",
		"minecraft:purple_wool",
		"minecraft:blue_wool",
		"minecraft:brown_wool",
		"minecraft:green_wool",
		"minecraft:red_wool",
		"minecraft:black_wool"
	};

	public static boolean isLog(String itemId) {
		for (String log : LOG) {
			if (log.equals(itemId)) return true;
		}
		return false;
	}

	public static boolean isLeaves(String itemId) {
		for (String leaves : LEAVES) {
			if (leaves.equals(itemId)) return true;
		}
		return false;
	}
	
	public static boolean isSapling(String itemId) {
		for (String sapling : SAPLINGS) {
			if (sapling.equals(itemId)) return true;
		}
		return false;
	}

	public static boolean isWool(String itemId) {
		for (String wool : WOOL) {
			if (wool.equals(itemId)) return true;
		}
		return false;
	}
}