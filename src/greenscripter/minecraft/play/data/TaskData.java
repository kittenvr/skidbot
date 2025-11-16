/*
package greenscripter.minecraft.play.data;

import greenscripter.minecraft.ServerConnection;
import greenscripter.minecraft.play.handler.PlayTickHandler;
import greenscripter.minecraft.task.TaskManager;
import greenscripter.minecraft.task.TaskStateMachine;

public class TaskData implements PlayData {

	private TaskManager taskManager;
	private TaskStateMachine taskStateMachine;

	@Override
	public void init(ServerConnection sc) {
		taskManager = new TaskManager();
		taskManager.setServerConnection(sc);
		
		taskStateMachine = new TaskStateMachine(sc);
		taskManager.setTaskStateMachine(taskStateMachine);
		
		// Add a PlayTickHandler to regularly tick the task manager and update the state machine
		sc.addPlayHandler(new PlayTickHandler(serverConnection -> {
			taskManager.tick();
			// Update the task state machine to execute the current task
			taskStateMachine.update();
		}));
	}

	public TaskManager getTaskManager() {
		return taskManager;
	}
	
	public TaskStateMachine getTaskStateMachine() {
		return taskStateMachine;
	}
}*/
