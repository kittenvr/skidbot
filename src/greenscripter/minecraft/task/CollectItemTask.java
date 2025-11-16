package greenscripter.minecraft.task;

import java.util.Arrays;

import greenscripter.minecraft.ServerConnection;
import greenscripter.minecraft.play.data.InventoryData;
import greenscripter.minecraft.play.inventory.ItemUtils;
import greenscripter.minecraft.play.inventory.Slot;
import greenscripter.minecraft.resources.TaskCatalogue;
import greenscripter.minecraft.util.ItemTarget;

public class CollectItemTask extends ResourceTask {

	private final ItemTarget target;
	private boolean started = false;
	private int collectedCount = 0;
	private Task currentSubTask = null;
	private long lastInventoryCheck = 0;
	private static final long INVENTORY_CHECK_INTERVAL = 1000; // Check every second

	public CollectItemTask(ItemTarget target) {
		super(new ItemTarget[]{target});
		this.target = target;
	}

	@Override
	public boolean isFinished(ServerConnection sc) {
		InventoryData inv = sc.getData(InventoryData.class);
		if (inv == null) return false;

		int currentCount = ItemUtils.countItems(target.getItemName(), inv.getInvIt());
		return currentCount >= target.getTargetCount();
	}

	@Override
	public void onResourceStart(ServerConnection sc) {
		setDebugState("Starting collection of " + target);
		started = true;
	}

	@Override
	public void onResourceStop(ServerConnection sc, Task interruptTask) {
		if (currentSubTask != null) {
			currentSubTask.onStop(sc);
		}
		setDebugState("Stopped collection of " + target);
	}

	@Override
	public Task onResourceTick(ServerConnection sc) {
		InventoryData inv = sc.getData(InventoryData.class);
		if (inv == null) {
			setDebugState("Inventory data not available");
			return this;
		}

		// Check inventory periodically
		long currentTime = System.currentTimeMillis();
		if (currentTime - lastInventoryCheck > INVENTORY_CHECK_INTERVAL) {
			lastInventoryCheck = currentTime;
		}

		int currentCount = ItemUtils.countItems(target.getItemName(), inv.getInvIt());
		collectedCount = currentCount;

		if (currentCount >= target.getTargetCount()) {
			setDebugState("Successfully collected " + currentCount + "/" + target.getTargetCount() + " " + target.getItemName());
			return null; // Task completed
		} else {
			// Check if we have a subtask running
			if (currentSubTask != null) {
				// Run the subtask
				Task subResult = currentSubTask.onTick(sc);
				
				// If subtask is not finished, continue with it
				if (!currentSubTask.isFinished(sc)) {
					return this;
				}
				
				// Subtask finished, check if we have enough items now
				currentCount = ItemUtils.countItems(target.getItemName(), inv.getInvIt());
				if (currentCount >= target.getTargetCount()) {
					setDebugState("Successfully collected " + currentCount + "/" + target.getTargetCount() + " " + target.getItemName() + " via subtask");
					return null;
				} else {
					// Subtask finished but we need more items - look for another approach
					currentSubTask = null;
				}
			}
			
			// No active subtask, determine what to do next
			if (currentSubTask == null) {
				currentSubTask = determineCollectionTask(sc);
				if (currentSubTask != null) {
					currentSubTask.onStart(sc);
					setDebugState("Starting collection subtask: " + currentSubTask.getDebugState());
					return this;
				} else {
					// If we can't determine a task, just wait
					setDebugState("Collecting " + target.getItemName() + ": " + currentCount + "/" + target.getTargetCount() + 
						" - waiting for items to appear or to determine collection strategy");
					return this;
				}
			}
			
			return this;
		}
	}

	private Task determineCollectionTask(ServerConnection sc) {
		// This is where we'd implement the logic to figure out how to get the item
		String itemId = target.getItemName();
		
		if (target.isCatalogueItem()) {
			// Use the TaskCatalogue to get the appropriate task
			// This would handle complex dependencies like crafting chains
			return TaskCatalogue.getItemTask(target);
		} else {
			// Determine based on item ID what strategy to use
			if (itemId.contains("_log") || itemId.equals("minecraft:cobblestone") || 
				itemId.contains("_ore") || itemId.equals("minecraft:dirt") || 
				itemId.equals("minecraft:stone") || itemId.contains("_block")) {
				// This looks like a block that needs to be mined
				String[] blockIds = {itemId.replace("minecraft:", "minecraft:")}; // Same ID
				return new MineBlockTask(target, blockIds, null); // Need to determine mining requirement
			} else if (itemId.contains("_pickaxe") || itemId.contains("_shovel") || 
					  itemId.contains("_sword") || itemId.contains("_axe") || 
					  itemId.contains("_hoe") || itemId.contains("_helmet") || 
					  itemId.contains("_chestplate") || itemId.contains("_leggings") || 
					  itemId.contains("_boots")) {
				// This looks like equipment that needs to be crafted
				// Return a task to craft this item using the catalogue
				return TaskCatalogue.getItemTask(target);
			} else {
				// For other items, try to find them in the world or craft them
				// Return a task based on catalogue definition
				return TaskCatalogue.getItemTask(target);
			}
		}
	}

	@Override
	public boolean isEqualResource(ResourceTask other) {
		if (other instanceof CollectItemTask o) {
			return target.equals(o.target);
		}
		return false;
	}

	@Override
	public String toDebugStringName() {
		return "CollectItemTask";
	}
}