package greenscripter.minecraft.task;

import greenscripter.minecraft.ServerConnection;

public class TaskManager {

	private Task currentTask = null;
	private ServerConnection serverConnection;
	private TaskStateMachine taskStateMachine = null;

	public void setServerConnection(ServerConnection sc) {
		this.serverConnection = sc;
	}

	public void setTask(Task task) {
		if (currentTask != null) {
			currentTask.onStop(serverConnection);
		}

		currentTask = task;
		if (currentTask != null) {
			currentTask.onStart(serverConnection);
			// If this task needs state machine control, set it on the state machine
			if (taskStateMachine != null) {
				taskStateMachine.setTask(task);
			}
		}
	}

	public void tick() {
		if (currentTask == null) {
			return;
		}

		// Execute current task
		Task subTask = currentTask.getSubTask();
		if (subTask != null) {
			// Execute subtask first
			Task result = subTask.onTick(serverConnection);

			if (result == null || subTask.isFinished(serverConnection)) {
				currentTask.setSubTask(null);
			}
		} else {
			// Execute main task
			Task result = currentTask.onTick(serverConnection);

			if (result != null && result != currentTask) {
				currentTask.setSubTask(result);
			}
		}

		// Check if current task is finished
		if (currentTask.isFinished(serverConnection)) {
			currentTask.onStop(serverConnection);
			currentTask = null;
			// Clear the task from state machine as well
			if (taskStateMachine != null) {
				taskStateMachine.setTask(null);
			}
		}
	}

	public void setTaskStateMachine(TaskStateMachine stateMachine) {
		this.taskStateMachine = stateMachine;
	}

	public boolean hasTask() {
		return currentTask != null;
	}

	public String getCurrentTaskDebugState() {
		if (currentTask == null) {
			return "No active task";
		}
		return currentTask.getDebugState();
	}
}