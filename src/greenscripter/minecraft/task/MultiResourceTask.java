/*
package greenscripter.minecraft.task;

import java.util.ArrayList;
import java.util.List;

import greenscripter.minecraft.ServerConnection;
import greenscripter.minecraft.util.ItemTarget;

public class MultiResourceTask extends ResourceTask {

	private final List<ResourceTask> subTasks = new ArrayList<>();
	private final List<ItemTarget> targets;
	private int currentTaskIndex = 0;
	private boolean initialized = false;
	private ResourceTask currentSubTask = null;

	public MultiResourceTask(ItemTarget... targets) {
		super(targets);
		this.targets = List.of(targets);

		// Create collection tasks for each target
		for (ItemTarget target : targets) {
			subTasks.add(new CollectItemTask(target));
		}
	}

	@Override
	public boolean isFinished(ServerConnection sc) {
		// Check if all subtasks are finished
		for (ResourceTask task : subTasks) {
			if (!task.isFinished(sc)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public void onResourceStart(ServerConnection sc) {
		setDebugState("Starting multi-resource collection for " + subTasks.size() + " items");
		initialized = true;
		if (!subTasks.isEmpty()) {
			subTasks.get(0).onResourceStart(sc);
		}
	}

	@Override
	public void onResourceStop(ServerConnection sc, Task interruptTask) {
		// Stop any currently running task
		if (currentSubTask != null) {
			currentSubTask.onResourceStop(sc, interruptTask);
		}
		for (ResourceTask task : subTasks) {
			if (!task.isFinished(sc)) {
				task.onResourceStop(sc, interruptTask);
			}
		}
		setDebugState("Stopped multi-resource collection");
	}

	@Override
	public Task onResourceTick(ServerConnection sc) {
		// Process each subtask until one returns a subtask to execute
		for (int i = 0; i < subTasks.size(); i++) {
			ResourceTask task = subTasks.get(i);
			
			if (!task.isFinished(sc)) {
				// Run this task
				Task subTaskResult = task.onResourceTick(sc);
				
				// Check if it has its own subtask to run
				if (task.getSubTask() != null) {
					Task taskSubResult = task.getSubTask().onTick(sc);
					if (taskSubResult != null && taskSubResult != task.getSubTask()) {
						task.setSubTask(taskSubResult);
					}
					if (task.getSubTask().isFinished(sc)) {
						task.setSubTask(null);
					}
					// If the subtask is running something, return that
					if (task.getSubTask() != null) {
						return this;
					}
				} else if (subTaskResult != null && subTaskResult != task) {
					task.setSubTask(subTaskResult);
					return this; // Continue with the subtask
				}
				
				// If the task finished, continue to next task
				if (task.isFinished(sc)) {
					setDebugState("Completed task: " + task.getDebugState());
					continue; // Move to next task
				} else {
					// This task is still running and needs attention
					setDebugState("Processing: " + task.getDebugState());
					return this; // Continue processing
				}
			}
		}
		
		// All tasks are completed
		return null;
	}

	@Override
	public boolean isEqualResource(ResourceTask other) {
		if (other instanceof MultiResourceTask o) {
			return subTasks.equals(o.subTasks);
		}
		return false;
	}

	@Override
	public String toDebugStringName() {
		return "MultiResourceTask";
	}
}*/
