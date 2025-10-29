package greenscripter.minecraft.commands;

import java.util.List;


import greenscripter.minecraft.ServerConnection;

import greenscripter.minecraft.play.data.PositionData;
import greenscripter.minecraft.play.data.WorldData;
import greenscripter.minecraft.play.handler.PlayTickHandler;
import greenscripter.minecraft.play.statemachine.PathfindState;
import greenscripter.minecraft.play.statemachine.PlayerMachine;
import greenscripter.minecraft.utils.Position;
import greenscripter.minecraft.world.PathFinder;

public class GoToCommand extends ConsoleCommand {

	public GoToCommand() {
		super("goto", "!goto <x> <y> <z> [axis] [spacing] - Pathfind to specified coordinates, optionally in formation", java.util.List.of("pathfind", "pf"));
	}

	@Override
	public void execute(ServerConnection serverConnection, String[] args, String targetUsername, ConsoleCommandRegistry registry) {
		// Check if args have at least 4 elements (command name, x, y, z)
		if (args.length < 4) {
			System.out.println("[System] Usage: !goto <x> <y> <z> [axis] [spacing]");
			System.out.println("[System] Example: !goto 100 64 200");
			System.out.println("[System] Example with formation: !goto 100 64 200 x 5 (spreads bots on x-axis with 5 block spacing)");
			return;
		}
		
		// Parse coordinates
		double x, y, z;
		try {
			x = Double.parseDouble(args[1]);
			y = Double.parseDouble(args[2]);
			z = Double.parseDouble(args[3]);
		} catch (NumberFormatException e) {
			System.out.println("[System] Invalid coordinates format. Usage: !goto <x> <y> <z> [axis] [spacing]");
			return;
		}
		
		Position basePos = new Position(x, y, z);
		
		// Parse optional axis and spacing
		String axis = null; // Default: no formation
		double spacing = 1.0;
		
		if (args.length >= 5) {
			axis = args[4].toLowerCase();
			if (!axis.equals("x") && !axis.equals("y") && !axis.equals("z")) {
				System.out.println("[System] Invalid axis. Use x, y, or z. Usage: !goto <x> <y> <z> [axis] [spacing]");
				return;
			}
		}
		
		if (args.length >= 6) {
			try {
				spacing = Double.parseDouble(args[5]);
			} catch (NumberFormatException e) {
				System.out.println("[System] Invalid spacing value. Usage: !goto <x> <y> <z> [axis] [spacing]");
				return;
			}
		}
		
		// If a target username was specified, only apply to that specific bot
		if (targetUsername != null) {
			if (serverConnection != null && serverConnection.name.equals(targetUsername)) {
				Position targetPos = calculateFormationPosition(basePos, axis, spacing, 0); // offset 0 for single bot
				startPathfinding(serverConnection, targetPos);
			}
		} else {
			// Apply to the current server connection
			if (serverConnection != null) {
				Position targetPos;
				if (axis == null) {
					// No formation, go to base position
					targetPos = basePos;
				} else {
					// Calculate position based on this bot's ID for formation
					// For now, use the connection ID as the offset in the formation
					targetPos = calculateFormationPosition(basePos, axis, spacing, serverConnection.id);
				}
				startPathfinding(serverConnection, targetPos);
			} else {
				System.out.println("[System] No server connection available for pathfinding");
			}
		}
	}
	
	private Position calculateFormationPosition(Position basePos, String axis, double spacing, double offset) {
		Position pos = basePos.copy();
		// Position the bot in the formation based on the offset
		if (axis != null) {
			switch (axis) {
				case "x":
					pos.x += offset * spacing;
					break;
				case "y":
					pos.y += offset * spacing;
					break;
				case "z":
					pos.z += offset * spacing;
					break;
			}
		}
		return pos;
	}
	
	private void startPathfinding(ServerConnection sc, Position targetPos) {
		// Get bot position and world data
		PositionData posData = sc.getData(PositionData.class);
		WorldData worldData = sc.getData(WorldData.class);
		
		if (worldData == null || worldData.world == null) {
			System.out.println("[Bot " + sc.name + "] World data not available for pathfinding");
			return;
		}
		
		// Create a temporary pathfinder for this operation
		PathFinder pathfinder = new PathFinder();
		pathfinder.world = worldData.world;
		pathfinder.infiniteVClipAllowed = false;
		pathfinder.timeout = 200;
		
		// Get current position
		Position startPos = new Position(posData.pos.x, posData.pos.y, posData.pos.z);
		
		// For this standalone command, we'll use a simple approach without requiring the bot's specific executor setup
		// Use a temporary executor for this pathfinding operation
		java.util.concurrent.ExecutorService tempExecutor = java.util.concurrent.Executors.newSingleThreadExecutor();
		
		PathfindState pathfindState = new PathfindState(tempExecutor, pathfinder, startPos, targetPos);
		
		// Add event handlers for pathfinding completion
		pathfindState.noPath = e -> {
			System.out.println("[Bot " + sc.name + "] Could not find path to " + targetPos);
		};
		
		pathfindState.travelComplete = e -> {
			System.out.println("[Bot " + sc.name + "] Reached destination: " + targetPos);
		};
		
		pathfindState.travelFailed = e -> {
			System.out.println("[Bot " + sc.name + "] Failed to reach destination: " + targetPos);
		};
		
		// Create the state machine for this bot
		PlayerMachine stateMachine = new PlayerMachine(sc);
		
		// Push the pathfinding state to the state machine
		try {
			stateMachine.push(pathfindState);
		} catch (greenscripter.statemachine.ThrownReturn e) {
			// ThrownReturn is expected in the state machine system, we can ignore it here
			// or log it if needed for debugging
		}
		
		// Create a PlayTickHandler that will tick our state machine
		PlayTickHandler pathfindingTickHandler = new PlayTickHandler(sc2 -> {
			if (sc2 == sc) { // Only tick for this specific connection
				stateMachine.tick();
			}
		});
		
		// Add the handler to the connection so it gets ticked
		sc.addPlayHandler(pathfindingTickHandler);
		
		System.out.println("[Bot " + sc.name + "] Starting pathfinding to " + targetPos);
	}
}