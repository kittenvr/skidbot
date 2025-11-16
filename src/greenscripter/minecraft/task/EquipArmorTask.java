/*
package greenscripter.minecraft.task;

import greenscripter.minecraft.ServerConnection;
import greenscripter.minecraft.play.data.InventoryData;
import greenscripter.minecraft.play.inventory.ItemUtils;
import greenscripter.minecraft.play.inventory.PlayerInventoryScreen;
import greenscripter.minecraft.play.inventory.Slot;
import greenscripter.minecraft.util.ItemTarget;

public class EquipArmorTask extends Task {
	
	private final ItemTarget[] armorTargets;
	private int currentTargetIndex = 0;
	private boolean equipPhase = false;
	
	public EquipArmorTask(ItemTarget... armorTargets) {
		this.armorTargets = armorTargets;
	}
	
	@Override
	public boolean isFinished(ServerConnection sc) {
		// Check if all targeted armor pieces are equipped
		InventoryData inv = sc.getData(InventoryData.class);
		if (inv == null) return false;
		
		PlayerInventoryScreen playerInv = inv.inv;
		Slot[] armorSlots = playerInv.getArmor(); // Gets [helmet, chestplate, leggings, boots]
		
		boolean allEquipped = true;
		for (ItemTarget target : armorTargets) {
			boolean targetFound = false;
			
			// Check if this target is equipped
			for (int i = 0; i < armorSlots.length; i++) {
				Slot slot = armorSlots[i];
				if (slot.present && slot.getItemId() != null && slot.getItemId().equals(target.getItemName())) {
					targetFound = true;
					break;
				}
			}
			
			if (!targetFound) {
				allEquipped = false;
				break;
			}
		}
		
		return allEquipped && currentTargetIndex >= armorTargets.length;
	}
	
	@Override
	public void onStart(ServerConnection sc) {
		setDebugState("Starting armor equipping process");
	}
	
	@Override
	public void onStop(ServerConnection sc) {
		setDebugState("Stopped armor equipping process");
	}
	
	@Override
	public Task onTick(ServerConnection sc) {
		InventoryData inv = sc.getData(InventoryData.class);
		if (inv == null) {
			setDebugState("Inventory data not available");
			return null;
		}
		
		if (!equipPhase) {
			// First phase: check if we have the required items
			boolean allItemsPresent = true;
			for (ItemTarget target : armorTargets) {
				int count = ItemUtils.countItems(target.getItemName(), inv.getInvIt());
				if (count < 1) { // Need at least one of each
					allItemsPresent = false;
					setDebugState("Waiting for: " + target.getItemName());
					break;
				}
			}
			
			if (allItemsPresent) {
				equipPhase = true;
				setDebugState("All armor pieces available, starting equip phase");
			}
			
			return this; // Continue checking
		} else {
			// Equip phase: equip available armor pieces
			if (currentTargetIndex < armorTargets.length) {
				ItemTarget target = armorTargets[currentTargetIndex];
				
				PlayerInventoryScreen playerInv = inv.inv;
				for (int i = 0; i < playerInv.slots.length; i++) {
					Slot slot = playerInv.slots[i];
					if (slot.present && slot.getItemId() != null && slot.getItemId().equals(target.getItemName())) {
						// Found the armor piece, try to equip it
						setDebugState("Found " + target.getItemName() + " in slot " + i + ", attempting to equip");
						inv.shiftClickSlot(slot);
						currentTargetIndex++; // Move to next armor piece after this one is equipped
						break;
					}
				}
				
				return this; // Continue until all armor is equipped
			} else {
				// All armor pieces processed, check if they're all equipped
				if (isFinished(sc)) {
					setDebugState("All armor successfully equipped");
					return null; // Task complete
				} else {
					// Wait for armor to be equipped (could be affected by server-side delays)
					setDebugState("Waiting for armor to be equipped");
					return this;
				}
			}
		}
	}
}*/
