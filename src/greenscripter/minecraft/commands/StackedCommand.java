/*
package greenscripter.minecraft.commands;

import java.util.List;

import greenscripter.minecraft.ServerConnection;
import greenscripter.minecraft.play.data.TaskData;
import greenscripter.minecraft.resources.TaskCatalogue;
import greenscripter.minecraft.task.CollectResourceTask;
import greenscripter.minecraft.task.EquipArmorTask;
import greenscripter.minecraft.task.MultiResourceTask;
import greenscripter.minecraft.task.ResourceTask;
import greenscripter.minecraft.task.TaskChain;
import greenscripter.minecraft.util.ItemTarget;

public class StackedCommand extends ConsoleCommand {

	public StackedCommand() {
		super("stacked", "!stacked - Collects diamond armor, pickaxe, axe, and sword", List.of("diamondgear"));
	}

	@Override
	public void execute(ServerConnection serverConnection, String[] args, String targetUsername, ConsoleCommandRegistry registry) {
		// If a target username was specified, only apply to that specific bot
		if (targetUsername != null && !serverConnection.name.equals(targetUsername)) {
			return;
		}

		System.out.println("[Bot " + serverConnection.name + "] Starting diamond gear collection process...");
		System.out.println("[Bot " + serverConnection.name + "] Targeting diamond gear using TaskCatalogue system");

		// Initialize the TaskCatalogue (this would normally happen at startup)
		TaskCatalogue.init();

		// Create a multi-resource task to collect all required items using catalogue tasks
		MultiResourceTask collectTask = new MultiResourceTask(
			new ItemTarget("diamond_helmet", 1, true),
			new ItemTarget("diamond_chestplate", 1, true),
			new ItemTarget("diamond_leggings", 1, true),
			new ItemTarget("diamond_boots", 1, true),
			new ItemTarget("diamond_pickaxe", 1, true),
			new ItemTarget("diamond_axe", 1, true),
			new ItemTarget("diamond_sword", 1, true)
		);

		// After collection, equip the armor
		EquipArmorTask equipTask = new EquipArmorTask(
			new ItemTarget("minecraft:diamond_helmet", 1),
			new ItemTarget("minecraft:diamond_chestplate", 1),
			new ItemTarget("minecraft:diamond_leggings", 1),
			new ItemTarget("minecraft:diamond_boots", 1)
		);

		System.out.println("[Bot " + serverConnection.name + "] Initiating collection of diamond gear using TaskCatalogue...");

		// Chain collection and equipment tasks
		TaskChain taskChain = new TaskChain(collectTask, equipTask);

		// Get the task manager for this bot and set the task
		TaskData taskData = serverConnection.getData(TaskData.class);
		if (taskData == null) {
			System.out.println("[Bot " + serverConnection.name + "] Task system not available");
			return;
		}
		taskData.getTaskManager().setTask(taskChain);

		System.out.println("[Bot " + serverConnection.name + "] Diamond gear collection and equipment task chain initiated. Check status with task system.");
	}
}*/
