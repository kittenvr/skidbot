package greenscripter.minecraft.resources;

import java.util.HashMap;
import java.util.Map;

public class ResourceCatalog {
	
	private static final Map<String, ResourceInfo> resourceMap = new HashMap<>();
	
	static {
		// Initialize with basic resources
		initializeResources();
	}
	
	private static void initializeResources() {
		// Basic items
		put("diamond_helmet", new ResourceInfo("diamond_helmet", "minecraft:diamond_helmet"));
		put("diamond_chestplate", new ResourceInfo("diamond_chestplate", "minecraft:diamond_chestplate"));
		put("diamond_leggings", new ResourceInfo("diamond_leggings", "minecraft:diamond_leggings"));
		put("diamond_boots", new ResourceInfo("diamond_boots", "minecraft:diamond_boots"));
		put("diamond_pickaxe", new ResourceInfo("diamond_pickaxe", "minecraft:diamond_pickaxe"));
		put("diamond_axe", new ResourceInfo("diamond_axe", "minecraft:diamond_axe"));
		put("diamond_sword", new ResourceInfo("diamond_sword", "minecraft:diamond_sword"));
		put("crafting_table", new ResourceInfo("crafting_table", "minecraft:crafting_table"));
		
		// Basic materials
		put("diamond", new ResourceInfo("diamond", "minecraft:diamond"));
		put("stick", new ResourceInfo("stick", "minecraft:stick"));
		put("planks", new ResourceInfo("planks", "minecraft:oak_planks")); // Generic planks
		
		// Wood types (for planks)
		put("oak_planks", new ResourceInfo("oak_planks", "minecraft:oak_planks"));
		put("spruce_planks", new ResourceInfo("spruce_planks", "minecraft:spruce_planks"));
		put("birch_planks", new ResourceInfo("birch_planks", "minecraft:birch_planks"));
		put("jungle_planks", new ResourceInfo("jungle_planks", "minecraft:jungle_planks"));
		put("acacia_planks", new ResourceInfo("acacia_planks", "minecraft:acacia_planks"));
		put("dark_oak_planks", new ResourceInfo("dark_oak_planks", "minecraft:dark_oak_planks"));
		put("mangrove_planks", new ResourceInfo("mangrove_planks", "minecraft:mangrove_planks"));
		put("cherry_planks", new ResourceInfo("cherry_planks", "minecraft:cherry_planks"));
	}
	
	private static void put(String name, ResourceInfo info) {
		resourceMap.put(name, info);
	}
	
	public static ResourceInfo getResource(String name) {
		return resourceMap.get(name);
	}
	
	public static boolean hasResource(String name) {
		return resourceMap.containsKey(name);
	}
	
	public static class ResourceInfo {
		public final String name;
		public final String itemId;
		
		public ResourceInfo(String name, String itemId) {
			this.name = name;
			this.itemId = itemId;
		}
	}
}