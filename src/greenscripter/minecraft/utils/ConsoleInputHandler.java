package greenscripter.minecraft.utils;

import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import greenscripter.minecraft.AsyncSwarmController;
import greenscripter.minecraft.ServerConnection;
import greenscripter.minecraft.commands.ConsoleCommandRegistry;

public class ConsoleInputHandler {

	private AsyncSwarmController controller; // Store the controller
	private ConsoleCommandRegistry commandRegistry;
	private ExecutorService executor = Executors.newSingleThreadExecutor();
	
	public ConsoleInputHandler(AsyncSwarmController controller, ConsoleCommandRegistry commandRegistry) {
		this.controller = controller;
		this.commandRegistry = commandRegistry;
	}

	public void start() {
		CompletableFuture.runAsync(this::runInputLoop, executor);
	}

	private void runInputLoop() {
		try (Scanner scanner = new Scanner(System.in)) {
			System.out.print("~$ "); // Prompt similar to the JavaScript version

			while (true) {
				String line = scanner.nextLine();
				
				// Check if input is a targeted console command (format: :botName !command)
				if (line.startsWith(":")) {
					String[] parts = line.substring(1).split(" ", 2);
					if (parts.length >= 2) {
						String targetBotName = parts[0];
						String command = parts[1];
						if (command.startsWith("!")) {
							// Execute targeted command using the controller
							commandRegistry.executeTargetedConsoleCommand(controller, command, targetBotName);
						} else {
							System.out.println("[System] For targeted chat messages, use format: :botName !command");
						}
					} else {
						System.out.println("[System] Invalid format. Use :botName !command");
					}
				}
				// Check if input is a console command
				else if (line.startsWith("!")) {
					// Execute console command using the controller
					commandRegistry.executeConsoleCommand(controller, line);
				} else if (!line.trim().isEmpty()) {
					// For regular text (not starting with ! or :), just echo to console
					System.out.println("[System] Use !command for console commands or :botName !command for targeted commands");
				}
				
				System.out.print("~$ "); // Print prompt again
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void shutdown() {
		executor.shutdown();
	}
}