/*
package greenscripter.minecraft.task;

import greenscripter.minecraft.ServerConnection;

public abstract class ResourceTask extends Task {

	private final Object[] targets;

	public ResourceTask(Object[] targets) {
		this.targets = targets;
	}

	public Object[] getTargets() {
		return targets;
	}

	@Override
	public void onStart(ServerConnection sc) {
		// Default implementation
		onResourceStart(sc);
	}

	@Override
	public void onStop(ServerConnection sc) {
		// Default implementation
		onResourceStop(sc, null);
	}

	// Abstract methods that subclasses must implement
	public abstract void onResourceStart(ServerConnection sc);
	public abstract void onResourceStop(ServerConnection sc, Task interruptTask);
	public abstract Task onResourceTick(ServerConnection sc);
	public abstract boolean isEqualResource(ResourceTask other);
	public abstract String toDebugStringName();
	
	// Main onTick implementation that calls onResourceTick
	@Override
	public final Task onTick(ServerConnection sc) {
		return onResourceTick(sc);
	}
}*/
