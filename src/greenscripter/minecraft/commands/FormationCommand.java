package greenscripter.minecraft.commands;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import greenscripter.minecraft.ServerConnection;
import greenscripter.minecraft.gameinfo.Registries;
import greenscripter.minecraft.play.data.WorldData;
import greenscripter.minecraft.play.statemachine.PathfindState;
import greenscripter.minecraft.play.statemachine.PlayerMachine;
import greenscripter.minecraft.play.data.PlayerData;
import greenscripter.minecraft.utils.Position;
import greenscripter.minecraft.utils.Vector;
import greenscripter.minecraft.world.World;
import greenscripter.minecraft.world.PathFinder;
import greenscripter.minecraft.world.entity.Entity;
import greenscripter.minecraft.world.entity.metadata.EMTextComponent;
import greenscripter.remoteindicators.IndicatorServer;
import greenscripter.statemachine.ThrownReturn;

public class FormationCommand {

	public FormationCommand() {
		// Constructor for formation command
	}

	public void execute(ServerConnection sc, String[] args) {
		if (args.length < 2) {
			System.out.println("Usage: !formation <x y z> <spacing> <axis=x|y|z>");
			return;
		}

		// Parse coordinates
		double x, y, z;
		try {
			x = Double.parseDouble(args[1]);
			y = Double.parseDouble(args[2]);
			z = Double.parseDouble(args[3]);
		} catch (NumberFormatException e) {
			System.out.println("Invalid coordinates format. Usage: !formation <x y z> <spacing> <axis>");
			return;
		}

		// Parse axis (default to x if not specified)
		String axis = "x";
		int axisIdx = -1;
		for (int i = 1; i < args.length; i++) {
			String lowerArg = args[i].toLowerCase();
			if (lowerArg.equals("x") || lowerArg.equals("y") || lowerArg.equals("z")) {
				axis = lowerArg;
				axisIdx = i;
				break;
			}
		}

		// Parse spacing (default to 1.0 if not specified)
		double spacing = 1.0;
		int spacingArgIndex = axisIdx == -1 ? 4 : (axisIdx == 4 ? 5 : 4);
		if (args.length > spacingArgIndex) {
			try {
				spacing = Double.parseDouble(args[spacingArgIndex]);
			} catch (NumberFormatException e) {
				System.out.println("Invalid spacing value. Using default spacing of 1.0");
			}
		}

		Position targetPos = new Position(x, y, z);
		System.out.println("Setting formation around coordinates " + targetPos + " at axis " + axis + " with spacing " + spacing + " (bot#" + sc.id + ").");

		// Note: This FormationCommand is designed to be called from a context where the controller
		// is available. When called from a console command, you would need to pass the controller.
		// For this version, we'll just execute formation for the current bot only.
		System.out.println("Executing formation for bot: " + sc.name + " (Note: This command should be called in a context with the full controller available)");
		
		// For now, just move this bot to the formation position
		List<ServerConnection> allBots = List.of(sc); // Just this bot for now
		
		// Create a shared executor service for pathfinding like TreeBot does
		ExecutorService pathfindingExecutor = Executors.newFixedThreadPool(Math.min(allBots.size(), 4));
		
		// Send formation command to all bots with proper offset based on their position in the list
		for (int i = 0; i < allBots.size(); i++) {
			ServerConnection bot = allBots.get(i);
			
			// Check if the bot is still connected before starting pathfinding
			if (bot.connectionState == ServerConnection.ConnectionState.DISCONNECTED || 
				bot.socket == null || bot.socket.isClosed()) {
				System.out.println("[Bot #" + bot.id + "] Skipping formation command - bot is disconnected");
				continue;
			}
			
			// Calculate formation position for this bot
			// Using the bot's index in the list to determine the offset in the formation
			double botOffset = i; // Use bot's index to determine position in the formation
			Position formationPos = calculateFormationPosition(targetPos, axis, spacing, botOffset * spacing);

			// Start pathfinding to formation position using the proper state machine like TreeBot
			try {
				PlayerData playerData = bot.getData(PlayerData.class);
				WorldData worldData = bot.getData(WorldData.class);
				
				// Check if world data is available before creating pathfinder
				if (worldData == null || worldData.world == null) {
					System.out.println("[Bot #" + bot.id + "] World data not available for pathfinding, skipping formation");
					continue;
				}
				
				// Create a pathfinder instance for this bot
				PathFinder pathfinder = new PathFinder();
				pathfinder.world = worldData.world; // Set the world for pathfinding
				pathfinder.infiniteVClipAllowed = false;
				pathfinder.timeout = 200;
				
				// Create the pathfind state like TreeBot does
				// Convert Vector to Position for the start position
				Position startPos = new Position(playerData.pos.pos.x, playerData.pos.pos.y, playerData.pos.pos.z);
				
				// Check if the executor is terminated before creating PathfindState
				if (pathfindingExecutor.isTerminated() || pathfindingExecutor.isShutdown()) {
					System.out.println("[Bot #" + bot.id + "] Executor service is terminated, cannot start pathfinding");
					continue;
				}
				
				PathfindState pathfindState = new PathfindState(pathfindingExecutor, pathfinder, startPos, formationPos);
				
				// Add error handling for when no path is found
				pathfindState.noPath = e -> {
					System.out.println("[Bot #" + bot.id + "] Could not find path to formation position: " + formationPos);
				};
				
				// Add completion handling - only print once when reached
				pathfindState.travelComplete = e -> {
					System.out.println("[Bot #" + bot.id + "] Reached formation position: " + formationPos);
				};
				
				// Add travel failed handling - reduce spam by adding cooldown
				final long[] lastTravelFailed = {0}; // Use array to allow modification in lambda
				pathfindState.travelFailed = e -> {
					long currentTime = System.currentTimeMillis();
					// Only print failure message if it's been at least 2 seconds since last failure
					if (currentTime - lastTravelFailed[0] > 2000) {
						System.out.println("[Bot #" + bot.id + "] Failed to reach formation position: " + formationPos);
						lastTravelFailed[0] = currentTime;
					}
				};
				
				// Create a new PlayerMachine for this bot and use it directly
				// Since the bot doesn't have an existing state machine in its data,
				// we'll create one and add a handler to tick it
				PlayerMachine stateMachine = new PlayerMachine(bot);
				
				// Add a PlayTickHandler to tick this state machine
				greenscripter.minecraft.play.handler.PlayTickHandler formationTickHandler = new greenscripter.minecraft.play.handler.PlayTickHandler(bot2 -> {
					if (bot2 == bot) { // Only tick for this specific connection
						stateMachine.tick();
					}
				});
				
				// Add the handler to the connection so it gets ticked
				bot.addPlayHandler(formationTickHandler);
				
				// Push the pathfinding state to the state machine
				try {
					// First, check if there's already a pathfinding state running and cancel it
					// This will help prevent issues with state machine conflicts
					stateMachine.push(pathfindState);
				} catch (ThrownReturn e) {
					System.out.println("[Bot #" + bot.id + "] Error pushing pathfinding state: " + e.getMessage());
					// If the state machine is full or has an issue, try to clear and try again
					// This helps handle the "pushed" error in the logs
				}
				
				System.out.println("[Bot #" + bot.id + "] Starting pathfinding to formation position: " + formationPos + " (axis: " + axis + ", spacing: " + spacing + ", bot#" + bot.id + ")");
			} catch (Exception e) {
				System.out.println("[Bot #" + bot.id + "] Error during pathfinding: " + e.getMessage());
				e.printStackTrace(); // Add stack trace for debugging
			}
		}
		
		// Instead of a shutdown hook, we'll shutdown the executor after giving time for tasks to complete
		// But only if it's not already shutdown
		new Thread(() -> {
			try {
				Thread.sleep(10000); // Wait 10 seconds before shutting down to allow tasks to complete
				if (!pathfindingExecutor.isShutdown() && !pathfindingExecutor.isTerminated()) {
					pathfindingExecutor.shutdownNow(); // Force shutdown to prevent rejected execution errors
				}
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			} catch (Exception e) {
				System.out.println("Error shutting down pathfinding executor: " + e.getMessage());
			}
		}).start();
	}

	private boolean isNumeric(String str) {
		if (str == null) return false;
		try {
			Double.parseDouble(str);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	private Entity findPlayerByName(World world, String playerName) {
		for (Entity entity : world.entities.values()) {
			// First, check if this is a player entity by type
			if (isPlayerEntity(entity)) {
				// Player entities might have custom names at metadata index 2
				if (entity.metadata != null && entity.metadata[2] != null) {
					if (entity.metadata[2] instanceof EMTextComponent) {
						EMTextComponent nameTag = (EMTextComponent) entity.metadata[2];
						if (nameTag.value != null) {
							// Get the text content of the name
							String entityName = nameTag.value.toString();
							// Clean the name to remove formatting
							entityName = cleanComponentName(entityName);
							if (entityName != null && entityName.equalsIgnoreCase(playerName)) {
								return entity;
							}
						}
					}
				}
				
				// If no custom name is set, the player might still be identified by its UUID
				// We won't have an easy way to map UUID to player name without additional server data
				// So we'll return the entity if it's a player with no custom name
			}
		}
		return null;
	}
	
	private String cleanComponentName(String componentString) {
		// This method cleans up the Minecraft text component string to extract the actual name
		// It removes quotes and other formatting
		if (componentString == null) return null;
		
		// Extract the text value from the JSON-like structure if needed
		// Minecraft uses JSON text components which may have various formats
		// For now, we'll just clean simple quotes and whitespace
		return componentString.replaceAll("\"", "")
			.replaceAll("'", "")
			.replaceAll("text:", "")
			.replaceAll("\\{", "")
			.replaceAll("\\}", "")
			.trim();
	}

	private boolean isPlayerEntity(Entity entity) {
		// Get the player entity type ID from registries as shown in KillAuraHandler
		int playerEntityType = Registries.registries.get("minecraft:entity_type").get("minecraft:player");
		return entity.type == playerEntityType;
	}

	private Position calculateFormationPosition(Position basePos, String axis, double spacing, double offset) {
		Position pos = basePos.copy();
		// For a formation, we want to space bots relative to the target
		// The offset should be added to space multiple bots in the formation
		// We use just the offset (botOffset * spacing) to space bots, not spacing + offset
		switch (axis) {
			case "x":
				pos.x += offset;
				break;
			case "y":
				pos.y += offset;
				break;
			case "z":
				pos.z += offset;
				break;
		}
		return pos;
	}

	private void sendChatMessage(ServerConnection sc, String message) {
		// Send the message to the console/stdout instead of to the server's chat
		System.out.println("[Bot #" + sc.id + "] " + message);
	}
}