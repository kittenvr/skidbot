package greenscripter.minecraft.task;

import greenscripter.minecraft.ServerConnection;
import greenscripter.minecraft.play.data.InventoryData;
import greenscripter.minecraft.play.inventory.Slot;

public abstract class Task {

	protected String debugState = "Starting task";
	protected Task subTask = null;

	public abstract boolean isFinished(ServerConnection sc);

	public abstract void onStart(ServerConnection sc);

	public abstract void onStop(ServerConnection sc);

	public abstract Task onTick(ServerConnection sc);

	public void setDebugState(String state) {
		this.debugState = state;
	}

	public String getDebugState() {
		return debugState;
	}

	public Task getSubTask() {
		return subTask;
	}

	public void setSubTask(Task task) {
		this.subTask = task;
	}
	
	// Helper methods for common operations
	protected int getItemCount(ServerConnection sc, String itemId) {
		InventoryData inv = sc.getData(InventoryData.class);
		if (inv != null) {
			return inv.inv.slots.length > 0 ? 
				java.util.Arrays.stream(inv.inv.slots)
				.filter(s -> s.present && s.getItemId() != null && s.getItemId().equals(itemId))
				.mapToInt(s -> s.itemCount)
				.sum() : 0;
		}
		return 0;
	}
}