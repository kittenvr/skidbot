/*
package greenscripter.minecraft.task;

import java.util.ArrayList;
import java.util.List;

import greenscripter.minecraft.ServerConnection;

public class TaskChain extends Task {
	
	private final List<Task> tasks;
	private int currentTaskIndex = 0;
	private boolean started = false;
	
	public TaskChain(Task... tasks) {
		this.tasks = new ArrayList<>();
		for (Task task : tasks) {
			this.tasks.add(task);
		}
	}
	
	public void addTask(Task task) {
		tasks.add(task);
	}
	
	@Override
	public boolean isFinished(ServerConnection sc) {
		return currentTaskIndex >= tasks.size();
	}
	
	@Override
	public void onStart(ServerConnection sc) {
		setDebugState("Starting task chain with " + tasks.size() + " tasks");
		started = true;
		if (tasks.size() > 0) {
			tasks.get(0).onStart(sc);
		}
	}
	
	@Override
	public void onStop(ServerConnection sc) {
		if (currentTaskIndex < tasks.size()) {
			tasks.get(currentTaskIndex).onStop(sc);
		}
		setDebugState("Stopped task chain");
	}
	
	@Override
	public Task onTick(ServerConnection sc) {
		if (currentTaskIndex >= tasks.size()) {
			return null; // All tasks completed
		}
		
		Task currentTask = tasks.get(currentTaskIndex);
		
		// Execute the current task
		Task subTaskResult = currentTask.onTick(sc);
		
		// If sub task is returned, execute it instead
		if (subTaskResult != null && subTaskResult != currentTask) {
			return subTaskResult;
		}
		
		// Check if current task is complete
		if (currentTask.isFinished(sc)) {
			setDebugState("Completed task: " + currentTask.getDebugState());
			currentTask.onStop(sc);
			
			// Move to next task
			currentTaskIndex++;
			if (currentTaskIndex < tasks.size()) {
				setDebugState("Starting next task: " + tasks.get(currentTaskIndex).getDebugState());
				tasks.get(currentTaskIndex).onStart(sc);
			} else {
				setDebugState("All tasks in chain completed");
			}
		}
		
		return this; // Continue with the chain
	}
}*/
