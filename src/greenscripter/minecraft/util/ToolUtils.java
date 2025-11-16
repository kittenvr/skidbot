package greenscripter.minecraft.util;

import greenscripter.minecraft.ServerConnection;
import greenscripter.minecraft.gameinfo.BlockStates;
import greenscripter.minecraft.gameinfo.RegistryTags;
import greenscripter.minecraft.play.data.InventoryData;
import greenscripter.minecraft.play.data.WorldData;
import greenscripter.minecraft.play.inventory.ItemId;
import greenscripter.minecraft.play.inventory.Slot;
import greenscripter.minecraft.utils.Position;

public class ToolUtils {

	public static void selectBestToolForBlock(ServerConnection sc, Position blockPos, WorldData worldData) {
		// Get inventory data
		InventoryData invData = sc.getData(InventoryData.class);
		if (invData == null) return; // No inventory data available

		// Get block information using block ID to get block state
		int blockId = worldData.world.getBlock(blockPos.x, blockPos.y, blockPos.z);
		BlockStates.BlockState blockState = BlockStates.getState(blockId);
		
		if (blockState == null) return; // Can't determine block type

		String blockName = blockState.block(); // Get the block name

		// Iterate through hotbar slots to find the best tool based on block type
		int bestToolSlot = -1;
		int currentToolPriority = -1; 

		for (int slotIndex = 0; slotIndex < 9; slotIndex++) { // Check hotbar slots
			Slot slot = invData.inv.getHotbarSlot(slotIndex);
			if (slot != null && slot.present) {
				String itemName = ItemId.get(slot.itemId);

				// Determine tool priority based on block type and tool type
				int priority = getToolPriorityForBlockType(itemName, blockName);

				if (priority > currentToolPriority) {
					currentToolPriority = priority;
					bestToolSlot = slotIndex;
				}
			}
		}

		// If we found a suitable tool, equip it
		if (bestToolSlot != -1) {
			invData.setHotbarSlot(bestToolSlot);
		}
	}
	
	public static int getToolPriorityForBlockType(String toolName, String blockName) {
		// Check if the block matches certain tags and use appropriate tools
		// Higher priority number means better tool for this block type

		// Check if block is in logs tag (need axe)
		if (RegistryTags.matchesBlockTag("minecraft:logs", blockName)) {
			if (toolName.contains("_axe")) {
				if (toolName.contains("netherite")) return 10;
				if (toolName.contains("diamond")) return 9;
				if (toolName.contains("iron")) return 8;
				if (toolName.contains("stone")) return 7;
				if (toolName.contains("wooden") || toolName.contains("golden")) return 6;
				return 5; // Any axe is better than non-axe for logs
			}
			return 1; // Non-axe for logs gets low priority
		}
		
		// Check if block is in leaves tag (any tool is fine, hand is ok)
		if (RegistryTags.matchesBlockTag("minecraft:leaves", blockName)) {
			if (toolName.contains("_shears")) return 8; // Shears are best for leaves
			if (toolName.contains("_hoe")) return 7; // Hoes are good for leaves
			if (toolName.contains("netherite")) return 6;
			if (toolName.contains("diamond")) return 5;
			if (toolName.contains("iron")) return 4;
			if (toolName.contains("stone")) return 3;
			if (toolName.contains("wooden") || toolName.contains("golden")) return 2;
			return 1; // Any tool is better than hand
		}
		
		// Check if block is in mineable/pickaxe tag (need pickaxe)
		if (RegistryTags.matchesBlockTag("minecraft:mineable/pickaxe", blockName)) {
			if (toolName.contains("_pickaxe")) {
				if (toolName.contains("netherite")) return 15;
				if (toolName.contains("diamond")) return 14;
				if (toolName.contains("iron")) return 13;
				if (toolName.contains("stone")) return 12;
				if (toolName.contains("wooden") || toolName.contains("golden")) return 11;
				return 10; // Any pickaxe is better than non-pickaxe for pickaxe blocks
			}
			return 1; // Non-pickaxe for pickaxe blocks gets very low priority
		}
		
		// Check if block is in mineable/shovel tag (need shovel)
		if (RegistryTags.matchesBlockTag("minecraft:mineable/shovel", blockName)) {
			if (toolName.contains("_shovel")) {
				if (toolName.contains("netherite")) return 10;
				if (toolName.contains("diamond")) return 9;
				if (toolName.contains("iron")) return 8;
				if (toolName.contains("stone")) return 7;
				if (toolName.contains("wooden") || toolName.contains("golden")) return 6;
				return 5; // Any shovel is better than non-shovel for shovel blocks
			}
			return 1; // Non-shovel for shovel blocks gets low priority
		}
		
		// Check if block is in mineable/axe tag (need axe)
		if (RegistryTags.matchesBlockTag("minecraft:mineable/axe", blockName)) {
			if (toolName.contains("_axe")) {
				if (toolName.contains("netherite")) return 10;
				if (toolName.contains("diamond")) return 9;
				if (toolName.contains("iron")) return 8;
				if (toolName.contains("stone")) return 7;
				if (toolName.contains("wooden") || toolName.contains("golden")) return 6;
				return 5; // Any axe is better than non-axe for axe blocks
			}
			return 1; // Non-axe for axe blocks gets low priority
		}
		
		// Default priority for other blocks - just use best available tool
		if (toolName.contains("netherite")) return 5;
		if (toolName.contains("diamond")) return 4;
		if (toolName.contains("iron")) return 3;
		if (toolName.contains("stone")) return 2;
		if (toolName.contains("wooden") || toolName.contains("golden")) return 1;
		return 0; // Not a tool
	}
}