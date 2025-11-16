package greenscripter.minecraft;

import java.util.List;

import java.io.File;
import java.nio.file.Files;

import com.google.gson.Gson;

import greenscripter.minecraft.commands.AutoEatCommand;
import greenscripter.minecraft.commands.AutoTotemCommand;
import greenscripter.minecraft.commands.ConsoleCommandRegistry;
import greenscripter.minecraft.commands.StackedCommand;
import greenscripter.minecraft.packet.c2s.play.ClientInfoPacket;
import greenscripter.minecraft.play.data.PlayData;
import greenscripter.minecraft.play.data.WorldData;
import greenscripter.minecraft.play.handler.PlayHandler;
import greenscripter.minecraft.play.handler.PlayTickHandler;
import greenscripter.minecraft.play.handler.WorldPlayHandler;
import greenscripter.minecraft.utils.ConsoleInputHandler;

/*
 * Minecraft Bot - Central entry point
 * Based on BrewBot structure, similar to index.js
 */
public class MinecraftBot {

	public static void main(String[] args) throws Exception {
		// Load accounts
		AccountList accounts = new Gson().fromJson(Files.readString(new File("accountlist.json").toPath()), AccountList.class);
		System.out.println("Accounts: " + accounts);

		// Setup basic handlers
		List<PlayHandler> handlers = ServerConnection.getStandardHandlers();
		WorldPlayHandler worldHandler = new WorldPlayHandler();
		handlers.removeIf(p -> p instanceof WorldPlayHandler);
		handlers.add(worldHandler);

		// Create the controller
		AsyncSwarmController controller = new AsyncSwarmController("localhost", 25565, handlers);
		//controller.bungeeMode = true; // Enable bungee mode

		controller.joinCallback = sc -> {
			if (sc.id % 10 == 0) {
				sc.sendPacket(new ClientInfoPacket(10));
			} else {
				sc.sendPacket(new ClientInfoPacket(2));
			}
			sc.setData(MinecraftBotGlobalData.class, new MinecraftBotGlobalData());
		};

		controller.namesToUUIDs = accounts::getUUID;
		controller.botNames = accounts::getName;

		// Initialize the console command system
		ConsoleCommandRegistry commandRegistry = new ConsoleCommandRegistry();
		registerAllCommands(commandRegistry); // Register all available commands

		// Set the command registry in the controller for future use
		controller.commandRegistry = commandRegistry;

		controller.start();
		controller.connect(accounts.size(), 1000); // Connect all accounts from accountlist.json

		// Initialize the AutoLogHandler (needed by AutoTotemHandler)
		greenscripter.minecraft.play.handler.AutoLogHandler autoLogHandler = new greenscripter.minecraft.play.handler.AutoLogHandler();

		// Initialize the AutoTotemHandler with the AutoLogHandler
		greenscripter.minecraft.play.handler.AutoTotemHandler autoTotemHandler = new greenscripter.minecraft.play.handler.AutoTotemHandler(autoLogHandler);

		// Add both handlers to the global handlers
		handlers.add(autoTotemHandler);
		handlers.add(autoLogHandler);

		// Initialize the AutoEatHandler with the controller
		greenscripter.minecraft.play.handler.AutoEatHandler autoEatHandler = new greenscripter.minecraft.play.handler.AutoEatHandler(controller);

		// Set the static reference for the command to access the handler
		greenscripter.minecraft.commands.AutoEatCommand.autoEatHandler = autoEatHandler;

		// Start the console input handler
		ConsoleInputHandler inputHandler = new ConsoleInputHandler(controller, commandRegistry);
		inputHandler.start();
		
		System.out.println("Minecraft Bot started with console commands and auto-eat enabled");
		System.out.println("Connected to localhost:25565 with bungeeMode enabled");
		System.out.println("Using " + accounts.size() + " accounts from accountlist.json");
	}

	private static void registerAllCommands(ConsoleCommandRegistry commandRegistry) {
		// Register all available console commands here
		commandRegistry.register(new greenscripter.minecraft.commands.HelpCommand());
		commandRegistry.register(new greenscripter.minecraft.commands.ExampleCommand());
		commandRegistry.register(new greenscripter.minecraft.commands.ListBotsCommand());
		commandRegistry.register(new greenscripter.minecraft.commands.GoToCommand());
		commandRegistry.register(new greenscripter.minecraft.commands.AutoEatCommand());
		commandRegistry.register(new greenscripter.minecraft.commands.AutoTotemCommand());
		commandRegistry.register(new greenscripter.minecraft.commands.GetOffhandCommand());
		commandRegistry.register(new greenscripter.minecraft.commands.CountCommand());
		commandRegistry.register(new greenscripter.minecraft.commands.GetCommand());
		commandRegistry.register(new greenscripter.minecraft.commands.StackedCommand());
	}
	
	static class MinecraftBotGlobalData implements PlayData {
		// Global data for the bot if needed
	}
}