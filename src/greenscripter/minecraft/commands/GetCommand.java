package greenscripter.minecraft.commands;

import java.util.Arrays;
import java.util.List;

import greenscripter.minecraft.ServerConnection;
import greenscripter.minecraft.play.data.TaskData;
import greenscripter.minecraft.resources.TaskCatalogue;
import greenscripter.minecraft.task.CollectResourceTask;
import greenscripter.minecraft.task.MultiResourceTask;
import greenscripter.minecraft.task.ResourceTask;
import greenscripter.minecraft.task.TaskChain;
import greenscripter.minecraft.util.ItemTarget;

public class GetCommand extends ConsoleCommand {

	public GetCommand() {
		super("get", "!get <item> [count] - Collects the specified item using the TaskCatalogue system", List.of("grab", "collect"));
	}

	@Override
	public void execute(ServerConnection serverConnection, String[] args, String targetUsername, ConsoleCommandRegistry registry) {
		// If a target username was specified, only apply to that specific bot
		if (targetUsername != null && !serverConnection.name.equals(targetUsername)) {
			return;
		}

		if (args.length == 0) {
			System.out.println("[Bot " + serverConnection.name + "] Usage: !get <item> [count]");
			System.out.println("[Bot " + serverConnection.name + "] Example: !get diamond 5");
			System.out.println("[Bot " + serverConnection.name + "] Example: !get diamond_pickaxe");
			return;
		}

		String itemName;
		int count = 1;
		
		// Debug output
		System.out.println("[DEBUG] Args received: " + java.util.Arrays.toString(args) + ", targetUser: " + targetUsername);
		
		// Skip the first argument since it's the command name ("get")
		// So args[1] is the first real argument, args[2] is the second, etc.
		
		if (args.length > 2) {
			// Format: !get count item_name
			// args[1] = count, args[2] = item_name
			try {
				count = Integer.parseInt(args[1]);
				itemName = args[2];
			} catch (NumberFormatException e) {
				// args[1] is not a number, so format might be: !get item_name count
				try {
					count = Integer.parseInt(args[2]);
					itemName = args[1];
				} catch (NumberFormatException e2) {
					// Neither is a number, treat both as potential item names
					System.out.println("[Bot " + serverConnection.name + "] Invalid count: " + args[1] + " or " + args[2]);
					return;
				}
			}
		} else if (args.length == 2) {
			// Format: !get item_name (count defaults to 1)
			itemName = args[1];
		} else {
			// No item specified - show help
			System.out.println("[Bot " + serverConnection.name + "] Usage: !get <item> [count] or !get <count> <item>");
			return;
		}

		System.out.println("[Bot " + serverConnection.name + "] Getting " + count + "x " + itemName);

		// Initialize the TaskCatalogue (this would normally happen at startup)
		TaskCatalogue.init();

		// Check if the item exists in the catalogue
		if (!TaskCatalogue.taskExists(itemName)) {
			System.out.println("[Bot " + serverConnection.name + "] Item '" + itemName + "' is not in the task catalogue.");
			System.out.println("[Bot " + serverConnection.name + "] Available items: " + 
				String.join(", ", TaskCatalogue.resourceNames()).substring(0, Math.min(100, String.join(", ", TaskCatalogue.resourceNames()).length())) + 
				(String.join(", ", TaskCatalogue.resourceNames()).length() > 100 ? "..." : ""));
			return;
		}

		// Get the task manager for this bot
		TaskData taskData = serverConnection.getData(TaskData.class);
		if (taskData == null) {
			System.out.println("[Bot " + serverConnection.name + "] Task system not available");
			return;
		}

		// Get the appropriate task from the TaskCatalogue
		ResourceTask collectTask = TaskCatalogue.getItemTask(itemName, count);

		if (collectTask == null) {
			System.out.println("[Bot " + serverConnection.name + "] Unable to create task for item: " + itemName);
			return;
		}

		System.out.println("[Bot " + serverConnection.name + "] Starting collection of " + count + "x " + itemName + " using TaskCatalogue...");

		// Set the task
		taskData.getTaskManager().setTask(collectTask);

		System.out.println("[Bot " + serverConnection.name + "] Get task initiated. Check status with task system.");
	}
}