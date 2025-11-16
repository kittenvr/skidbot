package greenscripter.minecraft.resources;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

import greenscripter.minecraft.task.CollectItemTask;
import greenscripter.minecraft.task.CraftItemTask;
import greenscripter.minecraft.task.MineBlockTask;
import greenscripter.minecraft.task.ResourceTask;
import greenscripter.minecraft.util.ItemTarget;

public class TaskCatalogue {

    private static final Map<String, String[]> nameToItemMatches = new HashMap<>();
    private static final Map<String, CataloguedResource> nameToResourceTask = new HashMap<>();
    private static final Map<String, CataloguedResource> itemToResourceTask = new HashMap<>();
    private static final Set<String> resourcesObtainable = new HashSet<>();

    public static void init() {
        // Initialize the catalog with all possible resources
        setupResources();
    }

    private static void setupResources() {
        // Define resource tasks here
        {
            String p = "planks";
            String s = "stick";
            String o = null; // null represents empty slot

            // RAW RESOURCES - these should be mined from blocks
            mine("log", new String[]{
                "minecraft:oak_log", "minecraft:spruce_log", "minecraft:birch_log",
                "minecraft:jungle_log", "minecraft:acacia_log", "minecraft:dark_oak_log",
                "minecraft:mangrove_log", "minecraft:cherry_log", "minecraft:crimson_stem", 
                "minecraft:warped_stem"
            }, new String[]{
                "minecraft:oak_log", "minecraft:spruce_log", "minecraft:birch_log",
                "minecraft:jungle_log", "minecraft:acacia_log", "minecraft:dark_oak_log",
                "minecraft:mangrove_log", "minecraft:cherry_log", "minecraft:crimson_stem", 
                "minecraft:warped_stem"
            }, MiningRequirement.HAND);
            
            mine("dirt", new String[]{"minecraft:dirt"}, new String[]{"minecraft:dirt"}, MiningRequirement.HAND);
            mine("cobblestone", new String[]{"minecraft:cobblestone"}, new String[]{"minecraft:cobblestone"}, MiningRequirement.WOOD);
            mine("andesite", new String[]{"minecraft:andesite"}, new String[]{"minecraft:andesite"}, MiningRequirement.HAND);
            mine("granite", new String[]{"minecraft:granite"}, new String[]{"minecraft:granite"}, MiningRequirement.HAND);
            mine("diorite", new String[]{"minecraft:diorite"}, new String[]{"minecraft:diorite"}, MiningRequirement.HAND);

            // Coal and ores - these need to be mined from ore blocks
            mine("coal", new String[]{"minecraft:coal"}, new String[]{"minecraft:coal_ore", "minecraft:deepslate_coal_ore"}, MiningRequirement.WOOD);
            mine("raw_iron", new String[]{"minecraft:raw_iron"}, new String[]{"minecraft:iron_ore", "minecraft:deepslate_iron_ore"}, MiningRequirement.STONE);
            mine("raw_gold", new String[]{"minecraft:raw_gold"}, new String[]{"minecraft:gold_ore", "minecraft:deepslate_gold_ore"}, MiningRequirement.IRON);
            mine("raw_copper", new String[]{"minecraft:raw_copper"}, new String[]{"minecraft:copper_ore", "minecraft:deepslate_copper_ore"}, MiningRequirement.STONE);
            mine("diamond", new String[]{"minecraft:diamond"}, new String[]{"minecraft:diamond_ore", "minecraft:deepslate_diamond_ore"}, MiningRequirement.IRON);
            mine("emerald", new String[]{"minecraft:emerald"}, new String[]{"minecraft:emerald_ore", "minecraft:deepslate_emerald_ore"}, MiningRequirement.IRON);
            mine("redstone", new String[]{"minecraft:redstone"}, new String[]{"minecraft:redstone_ore", "minecraft:deepslate_redstone_ore"}, MiningRequirement.IRON);
            mine("lapis_lazuli", new String[]{"minecraft:lapis_lazuli"}, new String[]{"minecraft:lapis_ore", "minecraft:deepslate_lapis_ore"}, MiningRequirement.STONE);
            alias("lapis", "lapis_lazuli");

            // Wood-related items
            simple("planks", new String[]{
                "minecraft:oak_planks", "minecraft:spruce_planks", "minecraft:birch_planks",
                "minecraft:jungle_planks", "minecraft:acacia_planks", "minecraft:dark_oak_planks",
                "minecraft:mangrove_planks", "minecraft:cherry_planks", "minecraft:crimson_planks", 
                "minecraft:warped_planks"
            }, count -> new CollectItemTask(new ItemTarget("planks", count, true)));

            shapedRecipe2x2("stick", "minecraft:stick", 4, "planks", o, "planks", o);

            // Tools
            tools("wooden", "planks", 
                "minecraft:wooden_pickaxe", "minecraft:wooden_shovel", 
                "minecraft:wooden_sword", "minecraft:wooden_axe", "minecraft:wooden_hoe");
            tools("stone", "cobblestone", 
                "minecraft:stone_pickaxe", "minecraft:stone_shovel", 
                "minecraft:stone_sword", "minecraft:stone_axe", "minecraft:stone_hoe");
            tools("iron", "iron_ingot", 
                "minecraft:iron_pickaxe", "minecraft:iron_shovel", 
                "minecraft:iron_sword", "minecraft:iron_axe", "minecraft:iron_hoe");
            tools("golden", "gold_ingot", 
                "minecraft:golden_pickaxe", "minecraft:golden_shovel", 
                "minecraft:golden_sword", "minecraft:golden_axe", "minecraft:golden_hoe");
            tools("diamond", "diamond", 
                "minecraft:diamond_pickaxe", "minecraft:diamond_shovel", 
                "minecraft:diamond_sword", "minecraft:diamond_axe", "minecraft:diamond_hoe");

            // Armor
            armor("leather", "leather", 
                "minecraft:leather_helmet", "minecraft:leather_chestplate", 
                "minecraft:leather_leggings", "minecraft:leather_boots");
            armor("iron", "iron_ingot", 
                "minecraft:iron_helmet", "minecraft:iron_chestplate", 
                "minecraft:iron_leggings", "minecraft:iron_boots");
            armor("golden", "gold_ingot", 
                "minecraft:golden_helmet", "minecraft:golden_chestplate", 
                "minecraft:golden_leggings", "minecraft:golden_boots");
            armor("diamond", "diamond", 
                "minecraft:diamond_helmet", "minecraft:diamond_chestplate", 
                "minecraft:diamond_leggings", "minecraft:golden_boots");

            // Food
            simple("apple", new String[]{"minecraft:apple"}, count -> new CollectItemTask(new ItemTarget("minecraft:apple", count)));
            
            // Crafted items
            shapedRecipe2x2("crafting_table", "minecraft:crafting_table", 1, p, p, p, p);
            shapedRecipe3x3("furnace", "minecraft:furnace", 1, "cobblestone", "cobblestone", "cobblestone", o, "cobblestone", o, "cobblestone", "cobblestone", "cobblestone");
            shapedRecipe2x2("torch", "minecraft:torch", 4, "coal", o, s, o);
            
            // Aliases
            alias("wooden_pick", "wooden_pickaxe");
            alias("stone_pick", "stone_pickaxe");
            alias("iron_pick", "iron_pickaxe");
            alias("gold_pick", "golden_pickaxe");
            alias("diamond_pick", "diamond_pickaxe");
            
            // Diamond gear aliases
            alias("diamond_helmet", "diamond_helmet");
            alias("diamond_chestplate", "diamond_chestplate");
            alias("diamond_leggings", "diamond_leggings");
            alias("diamond_boots", "diamond_boots");
            alias("diamond_pickaxe", "diamond_pickaxe");
            alias("diamond_axe", "diamond_axe");
            alias("diamond_sword", "diamond_sword");
        }
    }

    private static CataloguedResource put(String name, String[] matches, Function<Integer, ResourceTask> getTask) {
        CataloguedResource result = new CataloguedResource(matches, getTask);
        
        if (nameToResourceTask.containsKey(name)) {
            // Simply update the task for this existing resource instead of throwing
            nameToResourceTask.put(name, result);
        } else {
            nameToResourceTask.put(name, result);
        }
        nameToItemMatches.put(name, matches);
        for (String match : matches) {
            resourcesObtainable.add(match);
        }

        // If this resource is just one item, consider it collectable.
        if (matches.length == 1) {
            // Update or add to item mapping too
            itemToResourceTask.put(matches[0], result);
        }

        return result;
    }

    // Get item matches by catalogued name
    public static String[] getItemMatches(String name) {
        if (!nameToItemMatches.containsKey(name)) {
            return new String[0];
        }
        return nameToItemMatches.get(name);
    }

    public static boolean isObtainable(String itemId) {
        return resourcesObtainable.contains(itemId);
    }

    public static ItemTarget getItemTarget(String name, int count) {
        return new ItemTarget(name, count, true);
    }

    // Get a task for a catalogued resource
    public static ResourceTask getItemTask(String name, int count) {
        if (!taskExists(name)) {
            System.out.println("Task " + name + " does not exist.");
            return null;
        }

        return nameToResourceTask.get(name).getResource(count);
    }

    public static ResourceTask getItemTask(ItemTarget target) {
        if (target.isCatalogueItem()) {
            return getItemTask(target.getCatalogueName(), target.getTargetCount());
        } else {
            // If it's not a catalogue item, just collect it directly
            return new CollectItemTask(target);
        }
    }

    public static boolean taskExists(String name) {
        return nameToResourceTask.containsKey(name);
    }

    public static Collection<String> resourceNames() {
        return nameToResourceTask.keySet();
    }

    private static CataloguedResource simple(String name, String[] matches, Function<Integer, ResourceTask> getTask) {
        return put(name, matches, getTask);
    }

    private static CataloguedResource simple(String name, String match, Function<Integer, ResourceTask> getTask) {
        return simple(name, new String[]{match}, getTask);
    }

    private static CataloguedResource mine(String name, String[] itemMatches, String[] blockIds, MiningRequirement requirement) {
        return put(name, itemMatches, count -> new MineBlockTask(new ItemTarget(name, count, true), blockIds, requirement));
    }

    private static CataloguedResource shapedRecipe2x2(String name, String match, int outputCount, String s0, String s1, String s2, String s3) {
        CraftingRecipe recipe = CraftingRecipe.newShapedRecipe(name, new String[]{s0, s1, s2, s3}, outputCount);
        return put(name, new String[]{match}, count -> new CraftItemTask(new RecipeTarget(match, count, recipe)));
    }

    private static CataloguedResource shapedRecipe3x3(String name, String match, int outputCount, String s0, String s1, String s2, String s3, String s4, String s5, String s6, String s7, String s8) {
        CraftingRecipe recipe = CraftingRecipe.newShapedRecipe(name, new String[]{s0, s1, s2, s3, s4, s5, s6, s7, s8}, outputCount);
        return put(name, new String[]{match}, count -> new CraftItemTask(new RecipeTarget(match, count, recipe)));
    }

    private static void tools(String toolMaterialName, String material, String pickaxeItem, String shovelItem, String swordItem, String axeItem, String hoeItem) {
        String s = "stick";
        String o = null;
        //noinspection UnnecessaryLocalVariable
        String m = material;
        shapedRecipe3x3(toolMaterialName + "_pickaxe", pickaxeItem, 1, m, m, m, o, s, o, o, s, o);
        shapedRecipe3x3(toolMaterialName + "_shovel", shovelItem, 1, o, m, o, o, s, o, o, s, o);
        shapedRecipe3x3(toolMaterialName + "_sword", swordItem, 1, o, m, o, o, m, o, o, s, o);
        shapedRecipe3x3(toolMaterialName + "_axe", axeItem, 1, m, m, o, m, s, o, o, s, o);
        shapedRecipe3x3(toolMaterialName + "_hoe", hoeItem, 1, m, m, o, o, s, o, o, s, o);
    }

    private static void armor(String armorMaterialName, String material, String helmetItem, String chestplateItem, String leggingsItem, String bootsItem) {
        String o = null;
        //noinspection UnnecessaryLocalVariable
        String m = material;
        shapedRecipe3x3(armorMaterialName + "_helmet", helmetItem, 1, m, m, m, m, o, m, o, o, o);
        shapedRecipe3x3(armorMaterialName + "_chestplate", chestplateItem, 1, m, o, m, m, m, m, m, m, m);
        shapedRecipe3x3(armorMaterialName + "_leggings", leggingsItem, 1, m, m, m, m, o, m, m, o, m);
        shapedRecipe3x3(armorMaterialName + "_boots", bootsItem, 1, o, o, o, m, o, m, m, o, m);
    }

    private static void alias(String newName, String original) {
        if (!nameToResourceTask.containsKey(original) || !nameToItemMatches.containsKey(original)) {
            System.out.println("Invalid resource: " + original + ". Will not create alias.");
        } else {
            nameToResourceTask.put(newName, nameToResourceTask.get(original));
            nameToItemMatches.put(newName, nameToItemMatches.get(original));
        }
    }

    // Inner class for storing catalogued resources
    private static class CataloguedResource {
        private final String[] targets;
        private final Function<Integer, ResourceTask> getResource;

        public CataloguedResource(String[] targets, Function<Integer, ResourceTask> getResource) {
            this.targets = targets;
            this.getResource = getResource;
        }

        public ResourceTask getResource(int count) {
            return getResource.apply(count);
        }
    }
}