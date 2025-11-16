package greenscripter.minecraft.task;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import greenscripter.minecraft.ServerConnection;
import greenscripter.minecraft.play.data.InventoryData;
import greenscripter.minecraft.play.data.WorldData;
import greenscripter.minecraft.play.inventory.ItemUtils;
import greenscripter.minecraft.play.inventory.PlayerInventoryScreen;
import greenscripter.minecraft.play.inventory.Slot;
import greenscripter.minecraft.resources.SmeltTarget;
import greenscripter.minecraft.util.ItemTarget;

public class AdvancedSmeltTask extends ResourceTask {

	private final SmeltTarget target;
	private Task currentSubTask = null;
	private boolean started = false;

	public AdvancedSmeltTask(SmeltTarget target) {
		super(new ItemTarget[]{new ItemTarget(target.getResult().getItemName(), target.getResult().getTargetCount())});
		this.target = target;
	}

	@Override
	public boolean isFinished(ServerConnection sc) {
		InventoryData inv = sc.getData(InventoryData.class);
		if (inv == null) return false;

		int currentCount = ItemUtils.countItems(target.getResult().getItemName(), inv.getInvIt());
		return currentCount >= target.getResult().getTargetCount();
	}

	@Override
	public void onResourceStart(ServerConnection sc) {
		setDebugState("Starting advanced smelting of " + target.getResult() + " from " + target.getInput());
		started = true;
	}

	@Override
	public void onResourceStop(ServerConnection sc, Task interruptTask) {
		if (currentSubTask != null && currentSubTask.isActive()) {
			currentSubTask.onStop(sc);
		}
		setDebugState("Stopped advanced smelting of " + target.getResult());
	}

	@Override
	public Task onResourceTick(ServerConnection sc) {
		InventoryData inv = sc.getData(InventoryData.class);
		if (inv == null) {
			setDebugState("Inventory data not available");
			return this;
		}

		int currentCount = ItemUtils.countItems(target.getResult().getItemName(), inv.getInvIt());

		if (currentCount >= target.getResult().getTargetCount()) {
			setDebugState("Successfully smelted " + currentCount + "/" + target.getResult().getTargetCount() + " " + target.getResult().getItemName());
			return null;
		}

		// Calculate how many more we need to smelt
		int needed = target.getResult().getTargetCount() - currentCount;
		setDebugState("Smelting: " + currentCount + "/" + target.getResult().getTargetCount() + ", need " + needed + " more");

		// Check if we have sufficient input materials and fuel
		int inputCount = ItemUtils.countItems(target.getInput().getItemName(), inv.getInvIt());
		int fuelCount = getFuelCount(sc); // Simplified fuel calculation

		if (inputCount <= 0) {
			setDebugState("Need input materials to continue smelting");
			// In a complete implementation, we'd return tasks to gather materials
			return this;
		}

		if (fuelCount <= 0) {
			setDebugState("Need fuel to continue smelting");
			// In a complete implementation, we'd return tasks to gather fuel
			return this;
		}

		// For now, just use the basic smelting task
		// In a full implementation, this would interact with furnaces directly
		if (currentSubTask == null || currentSubTask.isFinished(sc)) {
			// Set up the next batch of smelting
			currentSubTask = new SmeltInFurnaceTask(target);
			currentSubTask.onStart(sc);
		}

		Task subTask = currentSubTask.onTick(sc);
		if (subTask != null) {
			return subTask;
		}

		// If current sub-task is finished but we still need more, continue
		if (currentSubTask.isFinished(sc)) {
			int newCount = ItemUtils.countItems(target.getResult().getItemName(), inv.getInvIt());
			if (newCount < target.getResult().getTargetCount()) {
				// Need to continue smelting
				currentSubTask = new SmeltInFurnaceTask(target);
				currentSubTask.onStart(sc);
			} else {
				return null; // All done
			}
		}

		return this;
	}

	private int getFuelCount(ServerConnection sc) {
		// Simplified fuel calculation - in reality, you'd need to identify fuel items
		// and calculate burn time, not just count items
		InventoryData inv = sc.getData(InventoryData.class);
		if (inv == null) return 0;

		// Just count some common fuel items for this example
		int coalCount = ItemUtils.countItems("minecraft:coal", inv.getInvIt());
		int charcoalCount = ItemUtils.countItems("minecraft:charcoal", inv.getInvIt());
		// In a real implementation, we'd multiply by burn time values

		return coalCount + charcoalCount;
	}

	@Override
	public boolean isEqualResource(ResourceTask other) {
		if (other instanceof AdvancedSmeltTask o) {
			return target.getResult().equals(o.target.getResult());
		}
		return false;
	}

	@Override
	public String toDebugStringName() {
		return "AdvancedSmeltTask";
	}
}