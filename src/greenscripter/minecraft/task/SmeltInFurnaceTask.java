/*
package greenscripter.minecraft.task;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import greenscripter.minecraft.ServerConnection;
import greenscripter.minecraft.play.data.InventoryData;
import greenscripter.minecraft.play.data.WorldData;
import greenscripter.minecraft.play.inventory.ItemUtils;
import greenscripter.minecraft.play.inventory.OpenedScreen;
import greenscripter.minecraft.play.inventory.PlayerInventoryScreen;
import greenscripter.minecraft.play.inventory.Slot;
import greenscripter.minecraft.resources.SmeltTarget;
import greenscripter.minecraft.util.ItemTarget;
import greenscripter.minecraft.utils.Position;

public class SmeltInFurnaceTask extends ResourceTask {

	private final SmeltTarget target;
	private boolean started = false;
	private Task goToFurnaceTask = null;
	private long lastSmeltCheck = 0;
	private static final long SMELT_CHECK_INTERVAL = 2000; // 2 seconds
	private boolean furnaceOpened = false;
	private Position furnacePosition = null;

	public SmeltInFurnaceTask(SmeltTarget target) {
		super(new ItemTarget[]{new ItemTarget(target.getResult().getItemName(), target.getResult().getTargetCount())});
		this.target = target;
	}

	@Override
	public boolean isFinished(ServerConnection sc) {
		InventoryData inv = sc.getData(InventoryData.class);
		if (inv == null) return false;

		int currentCount = ItemUtils.countItems(target.getResult().getItemName(), inv.getInvIt());
		return currentCount >= target.getResult().getTargetCount();
	}

	@Override
	public void onResourceStart(ServerConnection sc) {
		setDebugState("Starting smelting of " + target.getResult() + " from " + target.getInput());
		started = true;
	}

	@Override
	public void onResourceStop(ServerConnection sc, Task interruptTask) {
		if (goToFurnaceTask != null && goToFurnaceTask.isActive()) {
			goToFurnaceTask.onStop(sc);
		}
		// Close furnace if it's open
		if (furnaceOpened) {
			InventoryData inv = sc.getData(InventoryData.class);
			if (inv != null) {
				inv.closeScreen();
			}
			furnaceOpened = false;
		}
		setDebugState("Stopped smelting of " + target.getResult());
	}

	@Override
	public Task onResourceTick(ServerConnection sc) {
		InventoryData inv = sc.getData(InventoryData.class);
		WorldData world = sc.getData(WorldData.class);
		if (inv == null || world == null) {
			setDebugState("Inventory or World data not available");
			return this;
		}

		PlayerInventoryScreen playerInv = inv.getInventory();

		int currentCount = ItemUtils.countItems(target.getResult().getItemName(), inv.getInvIt());

		if (currentCount >= target.getResult().getTargetCount()) {
			setDebugState("Successfully smelted " + currentCount + "/" + target.getResult().getTargetCount() + " " + target.getResult().getItemName());
			// Close furnace if it's open
			if (furnaceOpened) {
				inv.closeScreen();
				furnaceOpened = false;
			}
			return null; // Task completed
		}

		// Check if we have the required input materials
		int inputCount = ItemUtils.countItems(target.getInput().getItemName(), inv.getInvIt());
		if (inputCount <= 0) {
			setDebugState("Need input materials to smelt " + target.getResult().getItemName() + " from " + target.getInput().getItemName());
			// Close furnace if it's open since we can't continue
			if (furnaceOpened) {
				inv.closeScreen();
				furnaceOpened = false;
			}
			// For now, we'll continue monitoring and assume other tasks are gathering materials
			return this;
		}

		// Check if we have fuel
		int fuelCount = getFuelCount(inv);
		if (fuelCount <= 0) {
			setDebugState("Need fuel to smelt " + target.getResult().getItemName());
			// Close furnace if it's open since we can't continue
			if (furnaceOpened) {
				inv.closeScreen();
				furnaceOpened = false;
			}
			// For now, we'll continue monitoring and assume other tasks are gathering fuel
			return this;
		}

		// Check if we can find a furnace to use
		if (goToFurnaceTask == null || goToFurnaceTask.isFinished(sc)) {
			// Create task to go to furnace
			goToFurnaceTask = new GoToClosestBlockTask(new String[]{"minecraft:furnace"});
			goToFurnaceTask.onStart(sc);
		}

		// Execute the pathfinding task if needed
		Task subTask = goToFurnaceTask.onTick(sc);
		if (subTask != null) {
			return subTask; // Return the pathfinding task to be executed
		}

		if (goToFurnaceTask.isFinished(sc)) {
			// We've reached the furnace, now try to open and use it
			setDebugState("At furnace, smelting " + target.getResult().getItemName() + ": " + currentCount + "/" + target.getResult().getTargetCount()
				+ " using " + inputCount + " " + target.getInput().getItemName() + " with " + fuelCount + " fuel");

			// Find the furnace block near us and interact with it to open
			if (!furnaceOpened) {
				furnacePosition = findFurnaceNearby(sc);
				if (furnacePosition != null) {
					// Interact with the furnace to open it
					world.useItemOn(sc, 0, furnacePosition, 1); // 0 = main hand, 1 = face (may need adjustment)
					furnaceOpened = true;
					setDebugState("Opening furnace at " + furnacePosition);
					return this; // Need to wait for the container to open
				} else {
					setDebugState("Could not find furnace block nearby");
					return this;
				}
			} else {
				// Furnace should be open, now manage the smelting process
				return manageSmeltingProcess(sc, inv);
			}
		} else {
			setDebugState("Going to furnace to smelt " + target.getResult().getItemName() + ": " + currentCount + "/" + target.getResult().getTargetCount());
		}

		// Periodically check if items have been smelted (simulated)
		long currentTime = System.currentTimeMillis();
		if (currentTime - lastSmeltCheck >= SMELT_CHECK_INTERVAL) {
			lastSmeltCheck = currentTime;
			// In a real implementation, we'd check furnace output slots and manage smelting process
		}

		return this;
	}

	private Position findFurnaceNearby(ServerConnection sc) {
		// This would search nearby blocks for a furnace
		// For now we'll use a simplified approach based on the GoToClosestBlockTask's target
		// In a real implementation, we would search the local area around the player
		return null; // Placeholder - in real implementation, this would find the furnace we pathed to
	}

	private Task manageSmeltingProcess(ServerConnection sc, InventoryData inv) {
		// Check if we actually have the furnace window open
		OpenedScreen screen = inv.getScreen();
		if (screen == null || !screen.isInitialized()) {
			// Wait for screen to open
			setDebugState("Waiting for furnace screen to open...");
			return this;
		}

		// Furnace slots: 0 = input, 1 = fuel, 2 = output
		// Check if we need to add more input materials
		Slot inputSlot = screen.getOtherSlot(0); // First slot in furnace (input)
		int neededInput = target.getInput().getTargetCount();
		int currentInputInFurnace = inputSlot != null && inputSlot.hasItem() ? inputSlot.count : 0;
		
		// We want to keep smelting the target amount, so we need to consider how much we've already smelted
		int currentSmelted = ItemUtils.countItems(target.getResult().getItemName(), inv.getInvIt());
		int remainingToSmelt = target.getResult().getTargetCount() - currentSmelted;
		
		if (currentInputInFurnace == 0) {
			// Add input to furnace
			if (addInputToSlot(sc, inv, 0)) {
				setDebugState("Added input to furnace");
			} else {
				setDebugState("Failed to add input to furnace");
			}
		}
		
		// Check if we need to add more fuel
		Slot fuelSlot = screen.getOtherSlot(1); // Second slot in furnace (fuel)
		int currentFuelInFurnace = fuelSlot != null && fuelSlot.hasItem() ? fuelSlot.count : 0;
		
		if (currentFuelInFurnace == 0) {
			// Add fuel to furnace
			if (addFuelToSlot(sc, inv, 1)) {
				setDebugState("Added fuel to furnace");
			} else {
				setDebugState("Failed to add fuel to furnace");
			}
		}
		
		// Extract output if available
		Slot outputSlot = screen.getOtherSlot(2); // Third slot in furnace (output)
		if (outputSlot != null && outputSlot.hasItem()) {
			// Extract the smelted item
			extractOutput(sc, screen, 2);
			setDebugState("Extracted smelted output from furnace");
		}

		// Periodically check smelting progress (simulated)
		long currentTime = System.currentTimeMillis();
		if (currentTime - lastSmeltCheck >= SMELT_CHECK_INTERVAL) {
			lastSmeltCheck = currentTime;
			// In a real implementation, we'd check actual furnace progress
		}

		return this;
	}
	
	private boolean addInputToSlot(ServerConnection sc, InventoryData inv, int furnaceSlot) {
		OpenedScreen screen = inv.getScreen();
		if (screen == null) return false;
		
		PlayerInventoryScreen playerInv = inv.getInventory();
		
		// Find the input item in our inventory
		int inputSlot = findItemInInventory(playerInv, target.getInput().getItemName());
		if (inputSlot == -1) {
			setDebugState("Could not find input item " + target.getInput().getItemName() + " in inventory");
			return false;
		}
		
		try {
			// Click on the input item in our inventory
			playerInv.clickSlot(inputSlot, 0, 0); // Pick up the item
			
			// Click on the furnace slot to place the item
			screen.clickSlot(furnaceSlot, 0, 0); // Place in furnace slot
			
			return true;
		} catch (Exception e) {
			setDebugState("Error adding input to furnace: " + e.getMessage());
			return false;
		}
	}

	private boolean addFuelToSlot(ServerConnection sc, InventoryData inv, int furnaceSlot) {
		OpenedScreen screen = inv.getScreen();
		if (screen == null) return false;
		
		PlayerInventoryScreen playerInv = inv.getInventory();
		
		// Find fuel in our inventory (coal or charcoal)
		String[] fuelTypes = {"minecraft:coal", "minecraft:charcoal"};
		int fuelSlot = -1;
		for (String fuelType : fuelTypes) {
			fuelSlot = findItemInInventory(playerInv, fuelType);
			if (fuelSlot != -1) break;
		}
		
		if (fuelSlot == -1) {
			setDebugState("Could not find fuel in inventory");
			return false;
		}
		
		try {
			// Click on the fuel item in our inventory
			playerInv.clickSlot(fuelSlot, 0, 0); // Pick up the fuel
			
			// Click on the furnace fuel slot to place the fuel
			screen.clickSlot(furnaceSlot, 0, 0); // Place in furnace fuel slot
			
			return true;
		} catch (Exception e) {
			setDebugState("Error adding fuel to furnace: " + e.getMessage());
			return false;
		}
	}
	
	private void extractOutput(ServerConnection sc, OpenedScreen screen, int furnaceOutputSlot) {
		try {
			// Click on the output slot to pick up the smelted item
			screen.clickSlot(furnaceOutputSlot, 0, 0);
			
			// Find an empty slot in the player's inventory to place the item
			InventoryData inv = sc.getData(InventoryData.class);
			if (inv != null) {
				PlayerInventoryScreen playerInv = inv.getInventory();
				
				// Look for an empty slot in the main inventory area (slots 9-35)
				int emptySlot = -1;
				for (int i = 9; i <= 35; i++) {
					Slot slot = playerInv.getSlot(i);
					if (!slot.hasItem()) {
						emptySlot = i;
						break;
					}
				}
				
				if (emptySlot != -1) {
					// Place the smelted item in the empty inventory slot
					playerInv.clickSlot(emptySlot, 0, 0);
				} else {
					// No empty slots - need to handle this case (maybe drop item?)
					setDebugState("No empty inventory slots to place smelted item");
					// For now, just drop the item by clicking outside the window
					screen.clickSlot(-999, 0, 0); // Click outside window to drop
				}
			}
		} catch (Exception e) {
			setDebugState("Error extracting output from furnace: " + e.getMessage());
		}
	}

	private int findItemInInventory(PlayerInventoryScreen playerInv, String itemName) {
		// Search main inventory (slots 9-35) for the item
		for (int i = 9; i <= 35; i++) {
			Slot slot = playerInv.getSlot(i);
			if (slot.hasItem() && slot.getItem().getName().equals(itemName)) {
				return slot.slot;
			}
		}
		// Also search hotbar (slots 36-44 in the player inventory screen)
		for (int i = 36; i <= 44; i++) {
			Slot slot = playerInv.getSlot(i);
			if (slot.hasItem() && slot.getItem().getName().equals(itemName)) {
				return slot.slot;
			}
		}
		return -1; // Not found
	}

	private int getFuelCount(InventoryData inv) {
		// Simplified fuel calculation - in reality, you'd need to identify fuel items
		// and calculate burn time, not just count items
		int coalCount = ItemUtils.countItems("minecraft:coal", inv.getInvIt());
		int charcoalCount = ItemUtils.countItems("minecraft:charcoal", inv.getInvIt());
		// In a real implementation, we'd multiply by burn time values

		return coalCount + charcoalCount;
	}

	@Override
	public boolean isEqualResource(ResourceTask other) {
		if (other instanceof SmeltInFurnaceTask o) {
			return target.getResult().equals(o.target.getResult());
		}
		return false;
	}

	@Override
	public String toDebugStringName() {
		return "SmeltInFurnaceTask";
	}
}*/
