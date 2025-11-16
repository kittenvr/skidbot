/*
package greenscripter.minecraft.task;

import java.util.Comparator;

import greenscripter.minecraft.ServerConnection;
import greenscripter.minecraft.gameinfo.BlockStates;
import greenscripter.minecraft.play.data.InventoryData;
import greenscripter.minecraft.play.data.PositionData;
import greenscripter.minecraft.play.data.WorldData;
import greenscripter.minecraft.play.inventory.ItemUtils;
import greenscripter.minecraft.play.statemachine.BreakBlockState;
import greenscripter.minecraft.play.statemachine.PathfindState;
import greenscripter.minecraft.play.statemachine.PlayerState;
import greenscripter.minecraft.util.ItemTarget;
import greenscripter.minecraft.utils.Position;
import greenscripter.minecraft.world.WorldSearch;
import greenscripter.statemachine.ThrownReturn;

public class TaskStateMachine extends greenscripter.minecraft.play.statemachine.PlayerMachine {

	private Task currentTask;
	private boolean hasActiveState = false;

	public TaskStateMachine(ServerConnection t) {
		super(t);
	}

	public void setTask(Task task) {
		this.currentTask = task;
		// Reset the machine but keep it ready for new states from the task
		if (this.getState() != null) {
			// Clear the state stack when setting a new task
			while (this.getState() != null) {
				this.popPrepare();
			}
		}
		hasActiveState = false;
	}

	public void update() {
		if (currentTask != null) {
			// Check if the current task is complete
			boolean taskComplete = false;
			if (currentTask instanceof ResourceTask resourceTask) {
				taskComplete = resourceTask.isFinished(value);
			} else {
				// For other types of tasks
				taskComplete = currentTask.isFinished(value);
			}
			
			if (taskComplete) {
				hasActiveState = false;
				return; // Task is complete, but state machine might still be executing final actions
			}
			
			// If we don't have an active state from this task, look for actions to take
			if (!hasActiveState) {
				// Handle different task types
				if (currentTask instanceof MineBlockTask mineTask) {
					ServerConnection sc = this.value;
					WorldData world = sc.getData(WorldData.class);
					PositionData posData = sc.getData(PositionData.class);
					InventoryData inv = sc.getData(InventoryData.class);
					
					if (world != null && world.world != null && posData != null && inv != null) {
						// Get the target item count to see if we're done
						int currentCount = ItemUtils.countItems(mineTask.getTarget().getItemName(), inv.getInvIt());
						if (currentCount >= mineTask.getTarget().getTargetCount()) {
							// Task is complete
							return;
						}
						
						// Find the target block to mine, similar to how GearBot does it
						String[] blockIds = mineTask.getBlockIds();
						boolean[] blockSet = BlockStates.getBlockSetOf(blockIds);
						WorldSearch search = world.world.worlds.getSearchFor(null, blockSet, false, true);
						
						synchronized (search.results) {
							var close = search.results.stream()
								.filter(t -> t.dimension.equals(world.world.id))
								.min(Comparator.comparingDouble(t -> {
									if (!t.blocks.isEmpty()) {
										Position blockPos = t.blocks.get(0);
										return posData.pos.squaredDistanceTo(blockPos.x, blockPos.y, blockPos.z);
									}
									return Double.MAX_VALUE;
								}));
								
							if (close.isPresent()) {
								WorldSearch.SearchResult result = close.get();
								// Remove the result from the list to prevent other tasks from claiming it
								search.results.remove(result);
								// Sort blocks by height (to mine from highest to lowest)
								result.blocks.sort(Comparator.comparingInt((Position p) -> -p.y));
								
								// Push the BreakBlockState to mine the block
								try {
									this.push(new BreakBlockState(null, result.blocks.get(0))); // Use null for indicator server for now
									hasActiveState = true;
								} catch (ThrownReturn e) {
									hasActiveState = true; // Still consider it active
								}
							} else {
								// No blocks found in current search - implement exploration behavior
								// Move to a random nearby location to explore new chunks
								
								// Generate an exploration target away from current position
								int explorationX = (int)posData.pos.x + (int)(Math.random() * 100 - 50);
								int explorationZ = (int)posData.pos.z + (int)(Math.random() * 100 - 50);
								
								// Create an exploration state to move to the new area
								ExploreState exploreState = new ExploreState(explorationX, explorationZ);
								try {
									this.push(exploreState);
									hasActiveState = true;
								} catch (ThrownReturn e) {
									hasActiveState = true;
								}
							}
						}
					}
				} else {
					// For other types of tasks, execute their logic via onTick
					// This allows the task to determine its own next steps
					Task subTask = currentTask.getSubTask();
					if (subTask != null) {
						// Execute the subtask first
						Task subTaskResult = subTask.onTick(value);
						
						// Check if we have specific state machine operations needed
						// For now, this will let the subtask logic work, which is the main point
					} else {
						// Execute the main task logic
						Task taskResult = currentTask.onTick(value);
						
						// Check if it returned a subtask to handle
						if (taskResult != null && taskResult != currentTask) {
							currentTask.setSubTask(taskResult);
						}
					}
				}
			}
		}
	}
	
	// Helper method to check if we have an active state from task execution
	public boolean hasActiveTaskState() {
		return hasActiveState;
	}
}*/
