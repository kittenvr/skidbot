package greenscripter.minecraft.task;

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

public class CraftInInventoryTask extends ResourceTask {

	private final RecipeTarget recipeTarget;
	private final ItemTarget targetItem;
	private boolean started = false;
	private CraftingRecipe recipe;
	private int itemsToCraft = 0;
	private int itemsCrafted = 0;

	public CraftInInventoryTask(RecipeTarget recipeTarget) {
		super(new ItemTarget[]{new ItemTarget(recipeTarget.getItemName(), recipeTarget.getCount())});
		this.recipeTarget = recipeTarget;
		this.targetItem = new ItemTarget(recipeTarget.getItemName(), recipeTarget.getCount());
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
		setDebugState("Starting crafting in inventory: " + targetItem);
		started = true;
		
		InventoryData inv = sc.getData(InventoryData.class);
		if (inv != null) {
			int currentCount = ItemUtils.countItems(targetItem.getItemName(), inv.getInvIt());
			this.itemsToCraft = targetItem.getTargetCount() - currentCount;
			this.itemsCrafted = currentCount;
		}
	}

	@Override
	public void onResourceStop(ServerConnection sc, Task interruptTask) {
		setDebugState("Stopped crafting in inventory: " + targetItem);
	}

	@Override
	public Task onResourceTick(ServerConnection sc) {
		InventoryData inv = sc.getData(InventoryData.class);
		if (inv == null) {
			setDebugState("Inventory data not available");
			return this;
		}

		PlayerInventoryScreen inventory = inv.getInventory();

		// Count current items to see if we've reached our target
		int currentCount = ItemUtils.countItems(targetItem.getItemName(), inv.getInvIt());
		if (currentCount >= targetItem.getTargetCount()) {
			setDebugState("Successfully crafted " + currentCount + "/" + targetItem.getTargetCount() + " " + targetItem.getItemName());
			return null; // Task completed
		}

		// Check if we have required ingredients
		if (!hasIngredients(sc, inv)) {
			setDebugState("Missing ingredients for crafting in inventory");
			return this; // Wait for ingredients to be gathered by other tasks
		}

		// Clear any existing items in crafting grid
		clearCraftingGrid(sc, inventory);

		// Place ingredients in crafting grid according to recipe pattern
		boolean placedIngredients = placeIngredients(sc, inventory);
		if (!placedIngredients) {
			setDebugState("Failed to place ingredients in crafting grid");
			return this;
		}

		// Craft the item by clicking the output slot
		craftItem(sc, inventory);

		setDebugState("Crafting in inventory: " + currentCount + "/" + targetItem.getTargetCount() + " " + targetItem.getItemName());
		return this;
	}

	private boolean hasIngredients(ServerConnection sc, InventoryData inv) {
		if (recipe == null) return false;

		for (Map.Entry<String, ItemTarget> entry : recipe.getMaterials().entrySet()) {
			String ingredientName = entry.getKey();
			ItemTarget target = entry.getValue();

			// Convert ingredient name to full Minecraft ID if needed
			String ingredientId = ingredientName;
			if (!ingredientId.startsWith("minecraft:")) {
				ingredientId = "minecraft:" + ingredientId;
			}

			int availableCount = ItemUtils.countItems(ingredientId, inv.getInvIt());
			if (availableCount < target.getTargetCount()) {
				setDebugState("Missing ingredient: " + ingredientId + " (need " + target.getTargetCount() + ", have " + availableCount + ")");
				return false;
			}
		}
		return true;
	}

	private void clearCraftingGrid(ServerConnection sc, PlayerInventoryScreen inventory) {
		// Clear slots 1-4 (the 2x2 crafting grid in player inventory)
		for (int i = 1; i <= 4; i++) {
			Slot slot = inventory.getSlot(i);
			if (slot.hasItem()) {
				// Move the item back to the main inventory
				// Find an empty slot in the main inventory (slots 9-35)
				int emptySlot = -1;
				for (int j = 9; j <= 35; j++) {
					Slot invSlot = inventory.getSlot(j);
					if (!invSlot.hasItem()) {
						emptySlot = j;
						break;
					}
				}
				
				if (emptySlot != -1) {
					// Move the item from crafting slot to empty inventory slot
					try {
						inventory.clickSlot(i, 0, 0); // Pick up from crafting slot
						inventory.clickSlot(emptySlot, 0, 0); // Place in inventory slot
						setDebugState("Cleared crafting slot " + i + ", moved item to inventory slot " + emptySlot);
					} catch (Exception e) {
						setDebugState("Failed to clear crafting slot " + i + ": " + e.getMessage());
					}
				} else {
					// No empty slots, we may need to drop the item or combine with existing stacks
					setDebugState("No empty inventory slots to clear crafting slot " + i);
				}
			}
		}
	}

	private boolean placeIngredients(ServerConnection sc, PlayerInventoryScreen inventory) {
		if (recipe == null) return false;

		// Get the crafting grid slots (1-4 in inventory)
		int width = Math.min(2, recipe.getWidth());
		int height = Math.min(2, recipe.getHeight());

		// Create a mapping of needed ingredients to how many we need
		Map<String, Integer> neededIngredients = new HashMap<>();
		for (int row = 0; row < height; row++) {
			for (int col = 0; col < width; col++) {
				int patternIndex = row * 3 + col; // Altoclef-style 3x3 pattern but only using top-left 2x2
				if (patternIndex >= recipe.getPattern().length) continue;

				String ingredient = recipe.getPattern()[patternIndex];
				if (ingredient == null || ingredient.equals("null") || ingredient.isEmpty()) {
					continue; // This crafting slot should be empty
				}

				// Convert ingredient name to full Minecraft ID if needed
				String ingredientId = ingredient;
				if (!ingredientId.startsWith("minecraft:")) {
					ingredientId = "minecraft:" + ingredientId;
				}

				// Count how many of this ingredient we need
				neededIngredients.put(ingredientId, neededIngredients.getOrDefault(ingredientId, 0) + 1);
			}
		}

		// For each needed ingredient, place it in the appropriate crafting slot
		int slotIdx = 0;
		for (int row = 0; row < height; row++) {
			for (int col = 0; col < width; col++) {
				int patternIndex = row * 3 + col;
				if (patternIndex >= recipe.getPattern().length) continue;

				String ingredient = recipe.getPattern()[patternIndex];
				if (ingredient == null || ingredient.equals("null") || ingredient.isEmpty()) {
					slotIdx++;
					continue; // This crafting slot should be empty
				}

				// Convert ingredient name to full Minecraft ID if needed
				String ingredientId = ingredient;
				if (!ingredientId.startsWith("minecraft:")) {
					ingredientId = "minecraft:" + ingredientId;
				}

				// Calculate which crafting grid slot this corresponds to
				int craftingSlotIndex = 1 + col + (row * 2); // 1,2,3,4 for the 2x2 grid
				if (craftingSlotIndex > 4) continue; // Safety check

				// Find the slot in inventory with this item
				Slot ingredientSlot = findItemInInventory(sc, ingredientId);
				if (ingredientSlot == null) {
					setDebugState("Could not find ingredient: " + ingredientId + " for slot " + craftingSlotIndex);
					return false;
				}

				// Actually move the item from the inventory slot to the crafting slot
				// This requires using the inventory system to perform the slot operations
				try {
					// First, pick up the item from the inventory slot
					inventory.clickSlot(ingredientSlot.slot, 0, 0); // Left click to pick up
					
					// Then, place it in the crafting grid slot
					inventory.clickSlot(craftingSlotIndex, 0, 0); // Left click to place in crafting slot
					
					setDebugState("Moved item from inventory slot " + ingredientSlot.slot + " to crafting slot " + craftingSlotIndex);
				} catch (Exception e) {
					setDebugState("Failed to move item: " + e.getMessage());
					return false;
				}
				
				slotIdx++;
			}
		}

		return true;
	}

	private Slot findItemInInventory(ServerConnection sc, String itemName) {
		InventoryData inv = sc.getData(InventoryData.class);
		if (inv == null) return null;

		PlayerInventoryScreen inventory = inv.getInventory();

		// Search main inventory (slots 9-35) for the item
		for (int i = 9; i <= 35; i++) {
			Slot slot = inventory.getSlot(i);
			if (slot.hasItem() && slot.getItem().getName().equals(itemName)) {
				return slot;
			}
		}

		return null;
	}

	private void craftItem(ServerConnection sc, PlayerInventoryScreen inventory) {
		// Click the crafting output slot (index 0 in player inventory is the crafting result)
		Slot outputSlot = inventory.getSlot(0);
		if (outputSlot.hasItem()) {
			// Click this slot to pick up the crafted item
			try {
				inventory.clickSlot(0, 0, 0); // Left click to pick up the crafted item
				// Now we need to place it somewhere in the inventory
				// For now, we'll just drop it or place it in the first available slot
				// Find an empty slot in the main inventory (slots 9-35)
				int emptySlot = -1;
				for (int i = 9; i <= 35; i++) {
					Slot slot = inventory.getSlot(i);
					if (!slot.hasItem()) {
						emptySlot = i;
						break;
					}
				}
				
				if (emptySlot != -1) {
					inventory.clickSlot(emptySlot, 0, 0); // Place the item in the empty slot
					setDebugState("Crafted item placed in slot " + emptySlot);
				} else {
					// No empty slots, maybe drop the item?
					setDebugState("No empty inventory slots, need to handle crafted item");
				}
			} catch (Exception e) {
				setDebugState("Failed to craft item: " + e.getMessage());
			}
		} else {
			setDebugState("No item in crafting output slot");
		}
	}

	@Override
	public boolean isEqualResource(ResourceTask other) {
		if (other instanceof CraftInInventoryTask o) {
			return targetItem.equals(o.targetItem);
		}
		return false;
	}

	@Override
	public String toDebugStringName() {
		return "CraftInInventoryTask";
	}
}