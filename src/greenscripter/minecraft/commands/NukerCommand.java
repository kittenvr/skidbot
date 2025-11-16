package greenscripter.minecraft.commands;

import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import greenscripter.minecraft.ServerConnection;
import greenscripter.minecraft.play.data.PositionData;
import greenscripter.minecraft.play.data.WorldData;
import greenscripter.minecraft.util.SelectionBox;
import greenscripter.minecraft.utils.Direction;
import greenscripter.minecraft.utils.Position;
import greenscripter.minecraft.packet.c2s.play.PlayerActionPacket;

public class NukerCommand extends ConsoleCommand {

	private static SelectionBox sharedSelection = new SelectionBox(); // Shared among all bots
	private static boolean selectionSet1 = false;
	private static boolean selectionSet2 = false;
	private static Position[] sharedBlockPositions = null;
	private static AtomicInteger currentBlockIndex = new AtomicInteger(0);
	private static final Object lock = new Object();
	private static boolean isInitialized = false;

	public NukerCommand() {
		super("nuker", "!nuker [set1|set2|show|start] - Nuker command for breaking blocks in a selected area",
			List.of("nuke", "breakarea", "blocknuker"));
	}

	@Override
	public void execute(ServerConnection serverConnection, String[] args, String targetUsername, ConsoleCommandRegistry registry) {
		if (args.length < 2) {
			System.out.println("[System] Usage: !nuker [set1|set2|show|start]");
			System.out.println("[System]   !nuker set1 <x> <y> <z> - Set first corner of selection");
			System.out.println("[System]   !nuker set2 <x> <y> <z> - Set second corner of selection");
			System.out.println("[System]   !nuker show - Show current selection");
			System.out.println("[System]   !nuker start - Start breaking blocks in the selected area");
			return;
		}

		String subCommand = args[1].toLowerCase();

		switch (subCommand) {
			case "set1":
				// Set first position with provided coordinates (shared for all bots)
				if (args.length < 5) {
					System.out.println("[System] Usage: !nuker set1 <x> <y> <z>");
					return;
				}
				try {
					int x1 = Integer.parseInt(args[2]);
					int y1 = Integer.parseInt(args[3]);
					int z1 = Integer.parseInt(args[4]);
					Position pos1 = new Position(x1, y1, z1);
					sharedSelection.setPos1(pos1);
					selectionSet1 = true;
					System.out.println("[Bot " + serverConnection.name + "] First position set to: " + pos1 + " for all bots");
				} catch (NumberFormatException e) {
					System.out.println("[System] Invalid coordinates format. Usage: !nuker set1 <x> <y> <z>");
					return;
				}
				break;

			case "set2":
				// Set second position with provided coordinates (shared for all bots)
				if (args.length < 5) {
					System.out.println("[System] Usage: !nuker set2 <x> <y> <z>");
					return;
				}
				try {
					int x2 = Integer.parseInt(args[2]);
					int y2 = Integer.parseInt(args[3]);
					int z2 = Integer.parseInt(args[4]);
					Position pos2 = new Position(x2, y2, z2);
					sharedSelection.setPos2(pos2);
					selectionSet2 = true;
					System.out.println("[Bot " + serverConnection.name + "] Second position set to: " + pos2 + " for all bots");
				} catch (NumberFormatException e) {
					System.out.println("[System] Invalid coordinates format. Usage: !nuker set2 <x> <y> <z>");
					return;
				}
				break;

			case "show":
				System.out.println("[Bot " + serverConnection.name + "] Current selection: " + sharedSelection);
				break;

			case "start":
				if (!sharedSelection.isComplete()) {
					System.out.println("[Bot " + serverConnection.name + "] Selection not complete. Both positions must be set first.");
					System.out.println("[Bot " + serverConnection.name + "] Position 1 set: " + selectionSet1 + ", Position 2 set: " + selectionSet2);
					return;
				}

				// Get all block positions in the selection
				Position[] blockPositions = sharedSelection.getAllBlockPositions();

				if (blockPositions.length == 0) {
					System.out.println("[Bot " + serverConnection.name + "] No blocks to break in selection.");
					return;
				}

				System.out.println("[Bot " + serverConnection.name + "] Starting to break " + blockPositions.length + " blocks...");

				// Initialize shared state for all bots (only first bot to run does this)
				synchronized(lock) {
					if (!isInitialized) {
						sharedBlockPositions = blockPositions;
						currentBlockIndex.set(0);
						isInitialized = true;
						System.out.println("[Bot " + serverConnection.name + "] Initialized nuker for " + blockPositions.length + " blocks");
					}
				}

				// Start the nuker process for this bot
				startNuking(serverConnection);
				break;

			case "clear":
				sharedSelection.clear();
				selectionSet1 = false;
				selectionSet2 = false;
				// Reset shared state (only first bot should do this)
				if (serverConnection.id == 0) { // Assuming bot with id 0 is the first
					synchronized(lock) {
						sharedBlockPositions = null;
						currentBlockIndex.set(0);
						isInitialized = false;
					}
					System.out.println("[Bot " + serverConnection.name + "] Nuker state cleared");
				}
				System.out.println("[Bot " + serverConnection.name + "] Selection cleared.");
				break;

			default:
				System.out.println("[System] Unknown subcommand: " + subCommand);
				System.out.println("[System] Usage: !nuker [set1|set2|show|start|clear]");
				break;
		}
	}

	private void startNuking(ServerConnection sc) {
		// Schedule a repeating task to handle block breaking
		java.util.concurrent.ScheduledExecutorService scheduler =
			java.util.concurrent.Executors.newSingleThreadScheduledExecutor();

		// Create a runnable task to process blocks for this bot
		java.util.concurrent.ScheduledFuture<?> task = scheduler.scheduleAtFixedRate(() -> {
			if (sharedBlockPositions == null) {
				System.out.println("[Bot " + sc.name + "] No blocks to break, stopping");
				scheduler.shutdown();
				return;
			}

			// Get next block to break using atomic counter (thread-safe)
			int blockIndex = currentBlockIndex.getAndIncrement();

			if (blockIndex >= sharedBlockPositions.length) {
				System.out.println("[Bot " + sc.name + "] All blocks have been processed");
				scheduler.shutdown();
				return;
			}

			Position blockPos = sharedBlockPositions[blockIndex];
			// Process this block
			processBlock(sc, blockPos);
		}, 0, 100, java.util.concurrent.TimeUnit.MILLISECONDS); // Run every 100ms
	}

	private void processBlock(ServerConnection sc, Position blockPos) {
		// Get world data
		WorldData worldData = sc.getData(WorldData.class);
		if (worldData == null || worldData.world == null) {
			return;
		}

		// Check if block still exists
		int blockId = worldData.world.getBlock(blockPos.x, blockPos.y, blockPos.z);
		if (blockId == 0 || blockId == 16544) {
			// Block is already broken, skip
			return;
		}

		// Calculate distance to block (center of block)
		PositionData posData = sc.getData(PositionData.class);
		greenscripter.minecraft.utils.Vector playerPos = new greenscripter.minecraft.utils.Vector(posData.pos.x, posData.pos.y, posData.pos.z);
		greenscripter.minecraft.utils.Vector blockCenter = new greenscripter.minecraft.utils.Vector(blockPos.x + 0.5, blockPos.y + 0.5, blockPos.z + 0.5);
		double distance = playerPos.distanceTo(blockCenter);

		if (distance > 4.5) {
			// If the block is too far, use pathfinding to go to it
			// We'll use the same approach as in GoToCommand
			goToBlock(sc, blockPos);
			// Don't try to break the block yet - wait for the next cycle after navigation
			return;
		}

		// Select appropriate tool for this block type (basic autotool)
		greenscripter.minecraft.util.ToolUtils.selectBestToolForBlock(sc, blockPos, worldData);

		// Send START_MINING once to initiate breaking
		PlayerActionPacket startPacket = new PlayerActionPacket();
		startPacket.status = PlayerActionPacket.START_MINING;
		startPacket.pos = blockPos;
		startPacket.face = (byte) Direction.UP.ordinal(); // Top face
		startPacket.sequence = 0;

		sc.sendPacket(startPacket);

		// Send FINISH_MINING to continue the breaking process (spam this until block breaks)
		PlayerActionPacket finishPacket = new PlayerActionPacket();
		finishPacket.status = PlayerActionPacket.FINISH_MINING;
		finishPacket.pos = blockPos;
		finishPacket.face = (byte) Direction.UP.ordinal(); // Top face
		finishPacket.sequence = 0;

		sc.sendPacket(finishPacket);

		System.out.println("[Bot " + sc.name + "] Breaking block at " + blockPos);
	}


	private void goToBlock(ServerConnection sc, Position blockPos) {
		// Get bot position and world data
		PositionData posData = sc.getData(PositionData.class);
		WorldData worldData = sc.getData(WorldData.class);

		if (worldData == null || worldData.world == null) {
			System.out.println("[Bot " + sc.name + "] World data not available for pathfinding to block " + blockPos);
			return;
		}

		// Use the same approach as TreeBot: get pathfinder from local data if available
		// Otherwise create a new one
		greenscripter.minecraft.world.PathFinder pathfinder = new greenscripter.minecraft.world.PathFinder();
		pathfinder.world = worldData.world;
		pathfinder.infiniteVClipAllowed = false;
		pathfinder.timeout = 200;

		// Get current position
		greenscripter.minecraft.utils.Position startPos = new greenscripter.minecraft.utils.Position(posData.pos.x, posData.pos.y, posData.pos.z);

		// Make target position near the block but not inside it
		// Find a position adjacent to the block that the player can stand in
		greenscripter.minecraft.utils.Position targetPos = findAccessiblePosition(blockPos, worldData);

		// Use an executor service for this pathfinding operation
		java.util.concurrent.ExecutorService exec = java.util.concurrent.Executors.newSingleThreadExecutor();
		greenscripter.minecraft.play.statemachine.PathfindState pathfindState =
			new greenscripter.minecraft.play.statemachine.PathfindState(
				exec,
				pathfinder,
				startPos,
				targetPos
			);

		// Add event handlers for pathfinding completion
		pathfindState.noPath = e -> {
			System.out.println("[Bot " + sc.name + "] Could not find path to block " + blockPos);
		};

		pathfindState.travelComplete = e -> {
			System.out.println("[Bot " + sc.name + "] Reached position near block: " + blockPos);
			// After reaching the block, try to break it now
			WorldData worldData2 = e.value.getData(WorldData.class);
			if (worldData2 != null && worldData2.world != null) {
				// Check if block still exists
				int blockId = worldData2.world.getBlock((int)blockPos.x, (int)blockPos.y, (int)blockPos.z);
				if (blockId != 0 && blockId != 16544) { // If it's not air
					// Select appropriate tool for this block type
					greenscripter.minecraft.util.ToolUtils.selectBestToolForBlock(e.value, blockPos, worldData2);

					// Send break packets
					greenscripter.minecraft.packet.c2s.play.PlayerActionPacket startPacket =
						new greenscripter.minecraft.packet.c2s.play.PlayerActionPacket();
					startPacket.status = greenscripter.minecraft.packet.c2s.play.PlayerActionPacket.START_MINING;
					startPacket.pos = blockPos;
					startPacket.face = (byte) Direction.UP.ordinal(); // Top face
					startPacket.sequence = 0;
					e.value.sendPacket(startPacket);

					greenscripter.minecraft.packet.c2s.play.PlayerActionPacket finishPacket =
						new greenscripter.minecraft.packet.c2s.play.PlayerActionPacket();
					finishPacket.status = greenscripter.minecraft.packet.c2s.play.PlayerActionPacket.FINISH_MINING;
					finishPacket.pos = blockPos;
					finishPacket.face = (byte) Direction.UP.ordinal(); // Top face
					finishPacket.sequence = 0;
					e.value.sendPacket(finishPacket);
				}
			}
		};

		pathfindState.travelFailed = e -> {
			System.out.println("[Bot " + sc.name + "] Failed to reach position near block: " + blockPos);
		};

		// Create the state machine for this bot
		greenscripter.minecraft.play.statemachine.PlayerMachine stateMachine =
			new greenscripter.minecraft.play.statemachine.PlayerMachine(sc);

		// Push the pathfinding state to the state machine
		try {
			stateMachine.push(pathfindState);
		} catch (greenscripter.statemachine.ThrownReturn e) {
			// ThrownReturn is expected in the state machine system
		}

		// Create a PlayTickHandler that will tick our state machine
		greenscripter.minecraft.play.handler.PlayTickHandler pathfindingTickHandler =
			new greenscripter.minecraft.play.handler.PlayTickHandler(sc2 -> {
				if (sc2 == sc) { // Only tick for this specific connection
					stateMachine.tick();
				}
			});

		// Add the handler to the connection so it gets ticked
		sc.addPlayHandler(pathfindingTickHandler);

		System.out.println("[Bot " + sc.name + "] Starting pathfinding to block " + blockPos);
	}

	private greenscripter.minecraft.utils.Position findAccessiblePosition(Position blockPos, WorldData worldData) {
		// Try positions around the block in increasing distances until we find one that is accessible
		// and within breaking distance (about 4-5 blocks)
		int maxDistance = 5; // Max distance to search for a valid position

		for (int dist = 1; dist <= maxDistance; dist++) {
			// Check all positions at distance 'dist' from the block
			for (int dx = -dist; dx <= dist; dx++) {
				for (int dz = -dist; dz <= dist; dz++) {
					for (int dy = -dist; dy <= dist; dy++) {
						// Only check positions that are approximately at this distance
						if (Math.abs(dx) == dist || Math.abs(dy) == dist || Math.abs(dz) == dist) {
							int x = blockPos.x + dx;
							int y = blockPos.y + dy;
							int z = blockPos.z + dz;

							// Check if this position is not a solid block (i.e., is air or non-collidable)
							int blockId = worldData.world.getBlock(x, y, z);
							// Use BlockStates to check if the block doesn't collide
							if (greenscripter.minecraft.gameinfo.BlockStates.missingOrInSet(blockId, greenscripter.minecraft.gameinfo.BlockStates.noCollideIds)) {
								// Also check that this position is within breaking distance of the target block
								// When a player stands at (x, y, z), they stand at the center of the block, which is (x+0.5, y+1.62, z+0.5) approximately
								// But for pathfinding, the target is the center of the block at y+0.5 level
								greenscripter.minecraft.utils.Vector pos = new greenscripter.minecraft.utils.Vector(x + 0.5, y + 1.62, z + 0.5); // Where player stands
								greenscripter.minecraft.utils.Vector blockCenter = new greenscripter.minecraft.utils.Vector(
									blockPos.x + 0.5, blockPos.y + 0.5, blockPos.z + 0.5); // Center of target block
								double distance = pos.distanceTo(blockCenter);

								if (distance <= 4.5) { // Within breaking distance
									// This position is accessible and within breaking distance
									// The pathfinder will handle whether this is a valid destination
									return new greenscripter.minecraft.utils.Position(x, y, z);
								}
							}
						}
					}
				}
			}
		}

		// If no position within range is accessible, return the block position anyway
		// This might cause pathfinding to fail, but it's the best we can do
		return new greenscripter.minecraft.utils.Position(blockPos.x, blockPos.y, blockPos.z);
	}
}