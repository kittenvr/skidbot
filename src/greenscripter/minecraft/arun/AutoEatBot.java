package greenscripter.minecraft.arun;

import java.util.List;

import java.io.File;
import java.nio.file.Files;

import com.google.gson.Gson;

import greenscripter.minecraft.AccountList;
import greenscripter.minecraft.AsyncSwarmController;
import greenscripter.minecraft.ServerConnection;
import greenscripter.minecraft.commands.AutoEatCommand;
import greenscripter.minecraft.commands.ConsoleCommandRegistry;
import greenscripter.minecraft.play.handler.AutoEatHandler;
import greenscripter.minecraft.play.handler.PlayHandler;
import greenscripter.minecraft.play.handler.WorldPlayHandler;
import greenscripter.minecraft.utils.ConsoleInputHandler;

public class AutoEatBot {

	public static void main(String[] args) throws Exception {
		AccountList accounts = new Gson().fromJson(Files.readString(new File("accountlist.json").toPath()), AccountList.class);
		System.out.println("Accounts: " + accounts);

		List<PlayHandler> handlers = ServerConnection.getStandardHandlers();
		WorldPlayHandler worldHandler = new WorldPlayHandler();
		handlers.removeIf(p -> p instanceof WorldPlayHandler);
		handlers.add(worldHandler);

		AsyncSwarmController controller = new AsyncSwarmController("localhost", 25565, handlers);

		controller.namesToUUIDs = accounts::getUUID;
		controller.botNames = accounts::getName;

		controller.start();
		controller.connect(accounts.size(), 600);

		// Initialize the AutoEatHandler with the controller
		AutoEatHandler autoEatHandler = new AutoEatHandler(controller);

		// Set the static reference for the command
		greenscripter.minecraft.commands.AutoEatCommand.autoEatHandler = autoEatHandler;

		// Initialize the console command system
		ConsoleCommandRegistry commandRegistry = new ConsoleCommandRegistry();
		registerAllCommands(commandRegistry, autoEatHandler); // Register all available commands

		// Start the console input handler
		ConsoleInputHandler inputHandler = new ConsoleInputHandler(controller, commandRegistry);
		inputHandler.start();
	}

	private static void registerAllCommands(ConsoleCommandRegistry commandRegistry, AutoEatHandler autoEatHandler) {
		// Register all available console commands here
		commandRegistry.register(new greenscripter.minecraft.commands.HelpCommand());
		commandRegistry.register(new greenscripter.minecraft.commands.ExampleCommand());
		commandRegistry.register(new greenscripter.minecraft.commands.ListBotsCommand());
		commandRegistry.register(new greenscripter.minecraft.commands.GoToCommand());
		commandRegistry.register(new greenscripter.minecraft.commands.AutoEatCommand());
		commandRegistry.register(new greenscripter.minecraft.commands.GetOffhandCommand());
		commandRegistry.register(new greenscripter.minecraft.commands.CountCommand());
	}
}