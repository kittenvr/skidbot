/*
package greenscripter.minecraft.task;

import java.util.Comparator;

import greenscripter.minecraft.ServerConnection;
import greenscripter.minecraft.gameinfo.BlockStates;
import greenscripter.minecraft.play.data.PositionData;
import greenscripter.minecraft.play.data.WorldData;
import greenscripter.minecraft.play.statemachine.PathfindState;
import greenscripter.minecraft.utils.Position;
import greenscripter.minecraft.world.WorldSearch;
import greenscripter.minecraft.play.statemachine.PlayerState;
import greenscripter.statemachine.StateTickPredicate;

public class GoToClosestBlockTask extends Task {

	private final String[] blockIds;
	private boolean started = false;
	private boolean pathfinding = false;
	private Position targetPosition = null;

	public GoToClosestBlockTask(String[] blockIds) {
		this.blockIds = blockIds;
	}

	@Override
	public boolean isFinished(ServerConnection sc) {
		// This is finished when we reach the target block
		PositionData posData = sc.getData(PositionData.class);
		if (posData == null || targetPosition == null) return false;

		// Check if we're close enough to the target (within 3 blocks)
		double distance = posData.pos.distanceTo(targetPosition);
		return distance <= 3.0;
	}

	@Override
	public void onStart(ServerConnection sc) {
		setDebugState("Starting path to closest block: " + String.join(", ", blockIds));
		started = true;
	}

	@Override
	public void onStop(ServerConnection sc) {
		pathfinding = false;
		setDebugState("Stopped pathfinding to block");
	}

	@Override
	public Task onTick(ServerConnection sc) {
		WorldData worldData = sc.getData(WorldData.class);
		PositionData posData = sc.getData(PositionData.class);

		if (worldData == null || posData == null || worldData.world == null) {
			setDebugState("World or Position data not available");
			return this;
		}

		if (targetPosition != null) {
			// We're already pathfinding to a specific position, continue
			double distance = posData.pos.distanceTo(targetPosition);
			setDebugState("Pathfinding to " + targetPosition + ", distance: " + String.format("%.2f", distance));
			
			if (distance <= 3.0) {
				// We've reached the target
				setDebugState("Reached target block at: " + targetPosition);
				return null;
			}
			
			return this;
		} else {
			// Find the closest block of the required type
			boolean[] blockSet = BlockStates.getBlockSetOf(blockIds);
			WorldSearch search = worldData.world.worlds.getSearchFor(null, blockSet, false, true);

			Position closestBlock = null;
			synchronized (search.results) {
				var close = search.results.stream()
					.filter(t -> t.dimension.equals(worldData.world.id))
					.min(Comparator.comparingDouble(t -> {
						if (!t.blocks.isEmpty()) {
							Position blockPos = t.blocks.get(0);
							return posData.pos.squaredDistanceTo(blockPos.x, blockPos.y, blockPos.z);
						}
						return Double.MAX_VALUE;
					}));

				if (close.isPresent()) {
					WorldSearch.SearchResult result = close.get();
					// Get the first block from the result (closest one)
					if (!result.blocks.isEmpty()) {
						closestBlock = result.blocks.get(0);
					}
				}
			}

			if (closestBlock != null) {
				// Found a block, now pathfind to it
				targetPosition = closestBlock;
				setDebugState("Found target block at: " + closestBlock + ", pathfinding...");
				
				// In a real implementation, we would push a PathfindState to the bot's state machine
				// For now we'll just mark that we're pathfinding
				pathfinding = true;
				
				// Create pathfinding state and push it to the player state machine
				PathfindState pathState = new PathfindState(
					worldData.exec, 
					worldData.pathFinder, 
					posData.pos, 
					targetPosition
				);
				
				// When pathfinding is complete, this task should continue
				pathState.travelComplete = e -> { */
/* Do nothing, task will continue on next tick *//*
 };
				pathState.travelFailed = e -> {
					setDebugState("Pathfinding failed, target may have been removed");
					targetPosition = null; // Reset to search again
				};
				pathState.noPath = e -> {
					setDebugState("No path found to target block");
					targetPosition = null; // Reset to search again
				};
				
				// Add the state to the player's state machine
				sc.getData(PlayerState.class).push(pathState);
				
				return this;
			} else {
				setDebugState("No " + String.join("/", blockIds) + " found nearby, waiting...");
				return this;
			}
		}
	}

	@Override
	public boolean isEqual(Task other) {
		if (other instanceof GoToClosestBlockTask o) {
			return java.util.Arrays.equals(blockIds, o.blockIds);
		}
		return false;
	}

	@Override
	public String toDebugString() {
		return "GoToClosestBlockTask: " + String.join(", ", blockIds);
	}
}*/
