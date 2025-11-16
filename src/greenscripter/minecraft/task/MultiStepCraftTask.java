/*
package greenscripter.minecraft.task;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import greenscripter.minecraft.ServerConnection;
import greenscripter.minecraft.play.data.InventoryData;
import greenscripter.minecraft.play.inventory.ItemUtils;
import greenscripter.minecraft.resources.CraftingRecipe;
import greenscripter.minecraft.resources.RecipeTarget;
import greenscripter.minecraft.util.ItemTarget;

public class MultiStepCraftTask extends ResourceTask {

	private final ItemTarget targetItem;
	private final List<RecipeTarget> recipeChain;
	private final Stack<Task> craftingTasks;
	private int currentStep = 0;
	
	public MultiStepCraftTask(List<RecipeTarget> recipeChain) {
		// The final target is the last recipe in the chain
		RecipeTarget finalRecipe = recipeChain.get(recipeChain.size() - 1);
		this.targetItem = new ItemTarget(finalRecipe.getItemName(), finalRecipe.getCount());
		this.recipeChain = recipeChain;
		this.craftingTasks = new Stack<>();
		// Build the stack of tasks in reverse order (last to first) so we can pop them in correct order
		for (int i = recipeChain.size() - 1; i >= 0; i--) {
			RecipeTarget recipe = recipeChain.get(i);
			this.craftingTasks.push(new CraftItemTask(recipe));
		}
	}

	@Override
	public boolean isFinished(ServerConnection sc) {
		InventoryData inv = sc.getData(InventoryData.class);
		if (inv == null) return false;

		int currentCount = ItemUtils.countItems(targetItem.getItemName(), inv.getInvIt());
		return currentCount >= targetItem.getTargetCount();
	}

	@Override
	public void onResourceStart(ServerConnection sc) {
		setDebugState("Starting multi-step crafting for " + targetItem);
	}

	@Override
	public void onResourceStop(ServerConnection sc, Task interruptTask) {
		setDebugState("Stopped multi-step crafting for " + targetItem);
	}

	@Override
	public Task onResourceTick(ServerConnection sc) {
		InventoryData inv = sc.getData(InventoryData.class);
		if (inv == null) {
			setDebugState("Inventory data not available");
			return this;
		}

		// Check if we've achieved our final goal
		int currentCount = ItemUtils.countItems(targetItem.getItemName(), inv.getInvIt());
		if (currentCount >= targetItem.getTargetCount()) {
			setDebugState("Successfully completed multi-step crafting: " + currentCount + "/" + targetItem.getTargetCount() + " " + targetItem.getItemName());
			return null;
		}

		// Execute the next task in the chain
		if (!craftingTasks.isEmpty()) {
			Task currentTask = craftingTasks.peek(); // Don't pop until it's complete
			if (currentTask != null) {
				if (!currentTask.isActive()) {
					currentTask.onStart(sc);
				}
				
				Task subTask = currentTask.onTick(sc);
				if (subTask != null) {
					return subTask; // Return any sub-task that needs to be executed
				}
				
				// If the current task is finished, remove it from the stack and move to the next
				if (currentTask.isFinished(sc)) {
					currentTask.onStop(sc);
					craftingTasks.pop();
					setDebugState("Completed step in multi-step crafting, " + craftingTasks.size() + " steps remaining");
				}
			}
		}

		// If we've completed all tasks but still don't have the target, continue monitoring
		return this;
	}

	@Override
	public boolean isEqualResource(ResourceTask other) {
		if (other instanceof MultiStepCraftTask o) {
			return targetItem.equals(o.targetItem);
		}
		return false;
	}

	@Override
	public String toDebugStringName() {
		return "MultiStepCraftTask";
	}
}*/
