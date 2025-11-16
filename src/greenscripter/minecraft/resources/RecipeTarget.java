package greenscripter.minecraft.resources;

public class RecipeTarget {
	private final String itemName;
	private final int count;
	private final CraftingRecipe recipe;

	public RecipeTarget(String itemName, int count, CraftingRecipe recipe) {
		this.itemName = itemName;
		this.count = count;
		this.recipe = recipe;
	}

	public String getItemName() {
		return itemName;
	}

	public int getCount() {
		return count;
	}

	public CraftingRecipe getRecipe() {
		return recipe;
	}
}