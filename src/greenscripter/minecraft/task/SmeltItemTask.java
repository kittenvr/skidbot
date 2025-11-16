package greenscripter.minecraft.task;

import java.util.ArrayList;
import java.util.List;

import greenscripter.minecraft.ServerConnection;
import greenscripter.minecraft.play.data.InventoryData;
import greenscripter.minecraft.play.inventory.ItemUtils;
import greenscripter.minecraft.resources.SmeltTarget;
import greenscripter.minecraft.util.ItemTarget;

public class SmeltItemTask extends ResourceTask {

	private final SmeltTarget target;
	private Task smeltingTask = null;

	public SmeltItemTask(SmeltTarget target) {
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
		setDebugState("Starting smelting of " + target.getResult() + " from " + target.getInput());
	}

	@Override
	public void onResourceStop(ServerConnection sc, Task interruptTask) {
		if (smeltingTask != null) {
			smeltingTask.onStop(sc);
		}
		setDebugState("Stopped smelting of " + target.getResult());
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
			return null; // Task completed
		} else {
			// Check if we have the required input materials
			int inputCount = ItemUtils.countItems(target.getInput().getItemName(), inv.getInvIt());
			if (inputCount <= 0) {
				setDebugState("Need input materials to smelt " + target.getResult().getItemName());
				// For a complete implementation, we would return tasks to gather materials
				// For now, return this to continue monitoring
				return this;
			} else {
				// Decide which smelting method to use - for now, just use furnace
				if (smeltingTask == null) {
					smeltingTask = new SmeltInFurnaceTask(target);
					smeltingTask.onStart(sc);
				}
				
				Task subTask = smeltingTask.onTick(sc);
				if (subTask != null) {
					return subTask;
				}
				
				// If current smelting task is done, check if we need to continue
				if (smeltingTask.isFinished(sc)) {
					int newCount = ItemUtils.countItems(target.getResult().getItemName(), inv.getInvIt());
					if (newCount >= target.getResult().getTargetCount()) {
						return null; // Fully completed
					} else {
						// Continue with another round of smelting
						smeltingTask = new SmeltInFurnaceTask(target);
						smeltingTask.onStart(sc);
						return this;
					}
				}
				
				return this;
			}
		}
	}

	@Override
	public boolean isEqualResource(ResourceTask other) {
		if (other instanceof SmeltItemTask o) {
			return target.getResult().equals(o.target.getResult());
		}
		return false;
	}

	@Override
	public String toDebugStringName() {
		return "SmeltItemTask";
	}
}