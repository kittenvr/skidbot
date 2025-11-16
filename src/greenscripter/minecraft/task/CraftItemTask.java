/*
package greenscripter.minecraft.task;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;

import greenscripter.minecraft.ServerConnection;
import greenscripter.minecraft.play.data.InventoryData;
import greenscripter.minecraft.play.inventory.ItemUtils;
import greenscripter.minecraft.play.inventory.PlayerInventoryScreen;
import greenscripter.minecraft.play.inventory.Slot;
import greenscripter.minecraft.resources.CraftingRecipe;
import greenscripter.minecraft.resources.RecipeTarget;
import greenscripter.minecraft.util.ItemTarget;

public class CraftItemTask extends ResourceTask {

	private final ItemTarget targetItem;
	private final RecipeTarget recipeTarget;
	private boolean started = false;
	private Task craftingTask = null;
	private CraftingRecipe recipe;
	private long lastCraftCheck = 0;
	private static final long CRAFT_CHECK_INTERVAL = 2000;

	public CraftItemTask(RecipeTarget recipeTarget) {
		super(new ItemTarget[]{new ItemTarget(recipeTarget.getItemName(), recipeTarget.getCount())});
		this.targetItem = new ItemTarget(recipeTarget.getItemName(), recipeTarget.getCount());
		this.recipeTarget = recipeTarget;
		this.recipe = recipeTarget.getRecipe();
	}

	@Override
	public boolean isFinished(ServerConnection sc) {
		InventoryData inv = sc.getData(InventoryData.class);
		if (inv == null) return false;

		int currentCount = ItemUtils.countItems(targetItem.getItemName(), inv.getInvIt());
		return currentCount >= targetItem.getTargetCount();
	}

	@Override
	public void onResourceStart(ServerConnection sc) {
		setDebugState("Starting crafting of " + targetItem + " using recipe");
		started = true;
	}

	@Override
	public void onResourceStop(ServerConnection sc, Task interruptTask) {
		if (craftingTask != null) {
			craftingTask.onStop(sc);
		}
		setDebugState("Stopped crafting of " + targetItem);
	}

	@Override
	public Task onResourceTick(ServerConnection sc) {
		InventoryData inv = sc.getData(InventoryData.class);
		if (inv == null) {
			setDebugState("Inventory data not available");
			return this;
		}

		int currentCount = ItemUtils.countItems(targetItem.getItemName(), inv.getInvIt());

		if (currentCount >= targetItem.getTargetCount()) {
			setDebugState("Successfully crafted " + currentCount + "/" + targetItem.getTargetCount() + " " + targetItem.getItemName());
			return null; // Task completed
		} else {
			// Check if we have the required ingredients
			Map<String, Integer> missingIngredients = checkIngredients(sc, inv);

			if (!missingIngredients.isEmpty()) {
				// Need to gather ingredients first
				setDebugState("Need ingredients to craft " + targetItem.getItemName());
				// For now, we'll just return this to continue monitoring
				// In a more complete implementation, we would return specific tasks to gather ingredients
				return this;
			} else {
				// We have ingredients, now perform the crafting
				if (craftingTask == null) {
					// Determine the appropriate crafting method based on recipe size
					if (isSmallRecipe()) {
						craftingTask = new CraftInInventoryTask(recipeTarget);
					} else {
						craftingTask = new UseCraftingTableTask(targetItem);
					}
					craftingTask.onStart(sc);
				}
				
				Task subTask = craftingTask.onTick(sc);
				if (subTask != null) {
					return subTask;
				}
				
				// Check if crafting task is complete and we need to continue
				if (craftingTask.isFinished(sc)) {
					int newCount = ItemUtils.countItems(targetItem.getItemName(), inv.getInvIt());
					if (newCount >= targetItem.getTargetCount()) {
						return null; // Fully completed
					} else {
						// We might need to craft more, so reset the crafting task
						craftingTask.onStop(sc);
						craftingTask = null;
						return this;
					}
				}
				
				return this;
			}
		}
	}

	private Map<String, Integer> checkIngredients(ServerConnection sc, InventoryData inv) {
		Map<String, Integer> missing = new HashMap<>();
		
		// Check if we have the required ingredients based on the recipe
		if (recipe != null) {
			Map<String, ItemTarget> materials = recipe.getMaterials();
			
			for (Map.Entry<String, ItemTarget> entry : materials.entrySet()) {
				String ingredientName = entry.getKey();
				ItemTarget materialTarget = entry.getValue();
				
				if (ingredientName != null && !ingredientName.isEmpty()) {
					// Convert ingredient name to actual item ID if needed
					String ingredientId = ingredientName;
					if (!ingredientId.startsWith("minecraft:")) {
						ingredientId = "minecraft:" + ingredientId;
					}

					int ingredientCount = ItemUtils.countItems(ingredientId, inv.getInvIt());
					
					// Calculate how many of this ingredient we need vs have
					// For this implementation, we'll calculate based on how many items we still need to craft
					int targetItemsLeft = targetItem.getTargetCount() - ItemUtils.countItems(targetItem.getItemName(), inv.getInvIt());
					int ingredientPerItem = calculateIngredientPerItem(ingredientId);
					int needed = ingredientPerItem * targetItemsLeft;
					if (ingredientCount < needed) {
						missing.put(ingredientId, needed - ingredientCount);
					}
				}
			}
		}
		return missing;
	}
	
	private int calculateIngredientPerItem(String ingredientId) {
		// For this basic implementation, count how many of this ingredient 
		// appear in the recipe pattern
		if (recipe != null) {
			int count = 0;
			for (String ingredient : recipe.getPattern()) {
				if (ingredient != null && ingredient.equals(ingredientId.replace("minecraft:", ""))) {
					count++;
				}
			}
			return count;
		}
		return 1; // Default to 1 if we can't determine
	}

	private boolean isSmallRecipe() {
		if (recipe == null) return true; // Default to small if unknown
		return recipe.getWidth() <= 2 && recipe.getHeight() <= 2;
	}

	@Override
	public boolean isEqualResource(ResourceTask other) {
		if (other instanceof CraftItemTask o) {
			return targetItem.equals(o.targetItem);
		}
		return false;
	}

	@Override
	public String toDebugStringName() {
		return "CraftItemTask";
	}
}*/
