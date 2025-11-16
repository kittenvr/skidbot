/*
package greenscripter.minecraft.task;

import greenscripter.minecraft.ServerConnection;
import greenscripter.minecraft.play.data.InventoryData;
import greenscripter.minecraft.play.data.WorldData;
import greenscripter.minecraft.play.inventory.ItemUtils;
import greenscripter.minecraft.play.inventory.OpenedScreen;
import greenscripter.minecraft.play.inventory.PlayerInventoryScreen;
import greenscripter.minecraft.play.inventory.Slot;
import greenscripter.minecraft.util.ItemTarget;
import greenscripter.minecraft.utils.Position;

public class UseCraftingTableTask extends ResourceTask {

	private final ItemTarget target;
	private boolean started = false;
	private Task goToCraftingTableTask = null;
	private boolean tableOpened = false;
	private Position tablePosition = null;

	public UseCraftingTableTask(ItemTarget target) {
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
		setDebugState("Starting use of crafting table for " + target);
		started = true;
	}

	@Override
	public void onResourceStop(ServerConnection sc, Task interruptTask) {
		if (goToCraftingTableTask != null && goToCraftingTableTask.isActive()) {
			goToCraftingTableTask.onStop(sc);
		}
		// Close crafting table if it's open
		if (tableOpened) {
			InventoryData inv = sc.getData(InventoryData.class);
			if (inv != null) {
				inv.closeScreen();
			}
			tableOpened = false;
		}
		setDebugState("Stopped using crafting table for " + target);
	}

	@Override
	public Task onResourceTick(ServerConnection sc) {
		InventoryData inv = sc.getData(InventoryData.class);
		WorldData world = sc.getData(WorldData.class);
		if (inv == null || world == null) {
			setDebugState("Inventory or World data not available");
			return this;
		}

		PlayerInventoryScreen playerInv = inv.getInventory();
		int currentCount = ItemUtils.countItems(target.getItemName(), inv.getInvIt());

		if (currentCount >= target.getTargetCount()) {
			setDebugState("Successfully used crafting table for " + currentCount + "/" + target.getTargetCount() + " " + target.getItemName());
			// Close crafting table if it's open
			if (tableOpened) {
				inv.closeWindow();
				tableOpened = false;
			}
			return null; // Task completed
		}

		// Check if we need to go to a crafting table
		if (goToCraftingTableTask == null || goToCraftingTableTask.isFinished(sc)) {
			// Create task to go to crafting table if we haven't already or if previous attempt finished
			goToCraftingTableTask = new GoToClosestBlockTask(new String[]{"minecraft:crafting_table"});
			goToCraftingTableTask.onStart(sc);
		}

		// Execute the pathfinding task if needed
		Task subTask = goToCraftingTableTask.onTick(sc);
		if (subTask != null) {
			return subTask; // Return the pathfinding task to be executed
		}

		if (goToCraftingTableTask.isFinished(sc)) {
			// We've reached the crafting table, now try to open and use it
			setDebugState("Reached crafting table, now crafting " + target.getItemName() + ": " + currentCount + "/" + target.getTargetCount());

			if (!tableOpened) {
				tablePosition = findCraftingTableNearby(sc);
				if (tablePosition != null) {
					// Interact with the crafting table to open it
					world.useItemOn(sc, 0, tablePosition, 1); // 0 = main hand, 1 = face (may need adjustment)
					tableOpened = true;
					setDebugState("Opening crafting table at " + tablePosition);
					return this; // Need to wait for the container to open
				} else {
					setDebugState("Could not find crafting table block nearby");
					return this;
				}
			} else {
				// Crafting table should be open, now perform crafting
				// This would involve more complex logic to determine what needs to be crafted
				// and how to place items in the 3x3 crafting grid
				return manageCraftingProcess(sc, inv);
			}
		} else {
			setDebugState("Going to crafting table for " + target.getItemName() + ": " + currentCount + "/" + target.getTargetCount());
		}

		return this; // Continue
	}
	
	private Position findCraftingTableNearby(ServerConnection sc) {
		// This would search nearby blocks for a crafting table
		// For now we'll use a simplified approach
		return null; // Placeholder - in real implementation, this would find the table we pathed to
	}

	private Task manageCraftingProcess(ServerConnection sc, InventoryData inv) {
		// Check if we actually have the crafting table window open
		OpenedScreen screen = inv.getScreen();
		if (screen == null || !screen.isInitialized()) {
			// Wait for screen to open
			setDebugState("Waiting for crafting table screen to open...");
			return this;
		}

		// For now, we'll just return this indicating we're at the crafting table
		// In a complete implementation, we would handle the actual crafting process
		// within the crafting table interface (slots 1-9 in the crafting grid)
		
		// 3x3 crafting grid is typically slots 1-9 in the opened crafting table screen
		// Output appears in slot 0
		setDebugState("At crafting table, ready to craft " + target.getItemName());
		
		return this;
	}

	@Override
	public boolean isEqualResource(ResourceTask other) {
		if (other instanceof UseCraftingTableTask o) {
			return target.equals(o.target);
		}
		return false;
	}

	@Override
	public String toDebugStringName() {
		return "UseCraftingTableTask";
	}
}*/
