package greenscripter.minecraft.resources;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import greenscripter.minecraft.util.ItemTarget;

public class CraftingRecipe {
	
	public enum RecipeType {
		SHAPED, SHAPELESS
	}
	
	private final RecipeType type;
	private final String[] pattern; // 0-8 for 3x3, 0-3 for 2x2
	private final Map<String, ItemTarget> materials; // key to ItemTarget
	private final ItemTarget result;
	private final int width;
	private final int height;

	public CraftingRecipe(RecipeType type, String[] pattern, Map<String, ItemTarget> materials, ItemTarget result, int width, int height) {
		this.type = type;
		this.pattern = pattern;
		this.materials = materials;
		this.result = result;
		this.width = width;
		this.height = height;
	}

	public static CraftingRecipe newShapedRecipe(String resultName, String[] pattern, int resultCount) {
		// Create materials map from the pattern (null values will be represented by "null" strings)
		Map<String, ItemTarget> materials = new HashMap<>();
		
		for (String s : pattern) {
			if (s != null && !s.isEmpty() && !s.equals("null") && !materials.containsKey(s)) {
				materials.put(s, new ItemTarget(s, 1)); // Add the material
			}
		}
		
		int width = 3; // Default for 3x3
		int height = 3;
		
		// Check if it's a 2x2 recipe
		if (pattern.length == 4) {
			width = 2;
			height = 2;
		}
		
		return new CraftingRecipe(RecipeType.SHAPED, pattern, materials, new ItemTarget(resultName, resultCount), width, height);
	}

	public static CraftingRecipe newShapedRecipe(String resultName, ItemTarget[] pattern, int resultCount) {
		// This version takes ItemTarget objects directly
		Map<String, ItemTarget> materials = new HashMap<>();
		String[] stringPattern = new String[pattern.length];
		
		for (int i = 0; i < pattern.length; i++) {
			if (pattern[i] != null) {
				stringPattern[i] = pattern[i].getItemName();
				if (!materials.containsKey(pattern[i].getItemName())) {
					materials.put(pattern[i].getItemName(), pattern[i]);
				}
			} else {
				stringPattern[i] = null;
			}
		}
		
		int width = 3; // Default for 3x3
		int height = 3;
		
		// Check if it's a 2x2 recipe
		if (pattern.length == 4) {
			width = 2;
			height = 2;
		}
		
		return new CraftingRecipe(RecipeType.SHAPED, stringPattern, materials, new ItemTarget(resultName, resultCount), width, height);
	}

	public RecipeType getType() {
		return type;
	}

	public String[] getPattern() {
		return pattern;
	}

	public Map<String, ItemTarget> getMaterials() {
		return materials;
	}

	public ItemTarget getResult() {
		return result;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}
}