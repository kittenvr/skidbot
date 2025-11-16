/*
package greenscripter.minecraft.task;

import greenscripter.minecraft.ServerConnection;
import greenscripter.minecraft.play.data.InventoryData;
import greenscripter.minecraft.play.inventory.ItemUtils;
import greenscripter.minecraft.util.ItemTarget;

public class CollectRawMaterialsTask extends Task {
	
	private final ItemTarget rawMaterialTarget;
	private boolean started = false;
	
	public CollectRawMaterialsTask(ItemTarget rawMaterialTarget) {
		this.rawMaterialTarget = rawMaterialTarget;
	}
	
	@Override
	public boolean isFinished(ServerConnection sc) {
		InventoryData inv = sc.getData(InventoryData.class);
		if (inv == null) return true;
		
		int currentCount = ItemUtils.countItems(rawMaterialTarget.getItemName(), inv.getInvIt());
		return currentCount >= rawMaterialTarget.getTargetCount();
	}
	
	@Override
	public void onStart(ServerConnection sc) {
		setDebugState("Starting to collect raw materials: " + rawMaterialTarget);
		started = true;
	}
	
	@Override
	public void onStop(ServerConnection sc) {
		setDebugState("Stopped collecting raw materials: " + rawMaterialTarget);
	}
	
	@Override
	public Task onTick(ServerConnection sc) {
		InventoryData inv = sc.getData(InventoryData.class);
		if (inv == null) {
			setDebugState("Inventory data not available for raw material collection");
			return this;
		}
		
		int currentCount = ItemUtils.countItems(rawMaterialTarget.getItemName(), inv.getInvIt());
		
		if (currentCount >= rawMaterialTarget.getTargetCount()) {
			setDebugState("Successfully collected " + currentCount + "/" + rawMaterialTarget.getTargetCount() + " " + rawMaterialTarget.getItemName());
			return null; // Task completed
		} else {
			setDebugState("Collecting " + rawMaterialTarget.getItemName() + ": " + currentCount + "/" + rawMaterialTarget.getTargetCount());
			
			// Continue waiting for raw materials to appear in inventory
			// This task just waits for the raw materials to appear in inventory
			return this; // Continue waiting
		}
	}
}*/
