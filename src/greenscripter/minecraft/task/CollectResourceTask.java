/*
package greenscripter.minecraft.task;

import greenscripter.minecraft.ServerConnection;
import greenscripter.minecraft.play.data.InventoryData;
import greenscripter.minecraft.play.inventory.ItemUtils;
import greenscripter.minecraft.play.inventory.Slot;
import greenscripter.minecraft.resources.Dimension;
import greenscripter.minecraft.util.ItemTarget;

public class CollectResourceTask extends ResourceTask {

	private final ItemTarget[] targets;
	private final String[] blockIdsToMine;
	private Dimension requiredDimension;
	private boolean started = false;
	private boolean mineIfPresent = false;
	private boolean forceDimension = false;
	private Task currentSubTask = null;

	public CollectResourceTask(ItemTarget target) {
		super(new ItemTarget[]{target});
		this.targets = new ItemTarget[]{target};
		this.blockIdsToMine = null; // Will be set through mineIfPresent
		this.requiredDimension = null;
	}

	public CollectResourceTask(ItemTarget... targets) {
		super(targets);
		this.targets = targets;
		this.blockIdsToMine = null;
		this.requiredDimension = null;
	}

	// Constructor that allows mining blocks if present
	public CollectResourceTask(ItemTarget target, String[] blockIdsToMine) {
		super(new ItemTarget[]{target});
		this.targets = new ItemTarget[]{target};
		this.blockIdsToMine = blockIdsToMine;
		this.requiredDimension = null;
	}

	@Override
	public boolean isFinished(ServerConnection sc) {
		InventoryData inv = sc.getData(InventoryData.class);
		if (inv == null) return false;

		for (ItemTarget target : targets) {
			int currentCount = ItemUtils.countItems(target.getItemName(), inv.getInvIt());
			if (currentCount < target.getTargetCount()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public void onResourceStart(ServerConnection sc) {
		setDebugState("Starting resource collection for: " + java.util.Arrays.toString(targets));
		started = true;
	}

	@Override
	public void onResourceStop(ServerConnection sc, Task interruptTask) {
		if (currentSubTask != null) {
			currentSubTask.onStop(sc);
		}
		setDebugState("Stopped resource collection for: " + java.util.Arrays.toString(targets));
	}

	@Override
	public Task onResourceTick(ServerConnection sc) {
		InventoryData inv = sc.getData(InventoryData.class);
		if (inv == null) {
			setDebugState("Inventory data not available");
			return this;
		}

		// Check if all targets are met
		boolean allMet = true;
		for (ItemTarget target : targets) {
			int currentCount = ItemUtils.countItems(target.getItemName(), inv.getInvIt());
			if (currentCount < target.getTargetCount()) {
				allMet = false;
				setDebugState("Collecting: " + target.getItemName() + " - " + currentCount + "/" + target.getTargetCount());
				break;
			}
		}

		if (allMet) {
			setDebugState("All resources collected: " + java.util.Arrays.toString(targets));
			return null; // Task completed
		} else {
			// Check if we're running a subtask
			if (currentSubTask != null) {
				Task subResult = currentSubTask.onTick(sc);
				
				if (currentSubTask.isFinished(sc)) {
					// Subtask finished, check if we now have all items
					currentSubTask = null; // Clear the subtask
					// Loop back to check if all targets are met
				} else {
					// Continue with current subtask
					return this;
				}
			}
			
			// No active subtask, determine what to do next
			if (currentSubTask == null) {
				// Find the first unmet target and process it
				for (ItemTarget target : targets) {
					int currentCount = ItemUtils.countItems(target.getItemName(), inv.getInvIt());
					if (currentCount < target.getTargetCount()) {
						// Create a task to collect this missing item
						currentSubTask = new CollectItemTask(target);
						currentSubTask.onStart(sc);
						setDebugState("Starting subtask to collect: " + target);
						break;
					}
				}
			}
			
			return this;
		}
	}

	// Method to set mining blocks if they're present nearby
	public CollectResourceTask mineIfPresent(String[] blockIds) {
		this.mineIfPresent = true;
		return this;
	}

	// Method to force a specific dimension
	public CollectResourceTask forceDimension(Dimension dimension) {
		this.forceDimension = true;
		this.requiredDimension = dimension;
		return this;
	}

	@Override
	public boolean isEqualResource(ResourceTask other) {
		if (other instanceof CollectResourceTask o) {
			return java.util.Arrays.equals(targets, o.targets);
		}
		return false;
	}

	@Override
	public String toDebugStringName() {
		return "CollectResourceTask";
	}
}*/
