/*
package greenscripter.minecraft.task;

import java.util.ArrayList;
import java.util.Comparator;

import greenscripter.minecraft.ServerConnection;
import greenscripter.minecraft.gameinfo.BlockStates;
import greenscripter.minecraft.play.data.InventoryData;
import greenscripter.minecraft.play.data.WorldData;
import greenscripter.minecraft.play.inventory.ItemUtils;
import greenscripter.minecraft.play.statemachine.BreakBlockState;
import greenscripter.minecraft.play.statemachine.PathfindState;
import greenscripter.minecraft.play.statemachine.PlayerState;
import greenscripter.minecraft.resources.MiningRequirement;
import greenscripter.minecraft.util.ItemTarget;
import greenscripter.minecraft.utils.Position;
import greenscripter.minecraft.world.WorldSearch;
import greenscripter.remoteindicators.IndicatorServer;
import greenscripter.statemachine.StateTickPredicate;

public class MineBlockTask extends ResourceTask {

	private final ItemTarget target;
	private final String[] blockIds;
	private final MiningRequirement miningRequirement;
	private boolean started = false;
	private boolean mining = false;

	public MineBlockTask(ItemTarget target, String[] blockIds, MiningRequirement miningRequirement) {
		super(new ItemTarget[]{target});
		this.target = target;
		this.blockIds = blockIds;
		this.miningRequirement = miningRequirement;
	}
	
	public MineBlockTask(ItemTarget target, String[] blockIds) {
		this(target, blockIds, null);
	}

	@Override
	public boolean isFinished(ServerConnection sc) {
		InventoryData inv = sc.getData(InventoryData.class);
		if (inv == null) return false;

		int currentCount = ItemUtils.countItems(target.getItemName(), inv.getInvIt());
		return currentCount >= target.getTargetCount();
	}

	@Override
	public void onResourceStart(ServerConnection sc) {
		setDebugState("Starting mining for " + target + " from blocks: " + String.join(", ", blockIds));
		started = true;
	}

	@Override
	public void onResourceStop(ServerConnection sc, Task interruptTask) {
		mining = false;
		setDebugState("Stopped mining for " + target);
	}

	@Override
	public Task onResourceTick(ServerConnection sc) {
		InventoryData inv = sc.getData(InventoryData.class);
		WorldData world = sc.getData(WorldData.class);
		
		if (inv == null || world == null || world.world == null) {
			setDebugState("Inventory or World data not available");
			return this;
		}

		int currentCount = ItemUtils.countItems(target.getItemName(), inv.getInvIt());

		if (currentCount >= target.getTargetCount()) {
			setDebugState("Successfully mined " + currentCount + "/" + target.getTargetCount() + " " + target.getItemName());
			return null; // Task completed
		} else {
			// Create a search for the target blocks
			boolean[] blockSet = BlockStates.getBlockSetOf(blockIds);
			WorldSearch search = world.world.worlds.getSearchFor(null, blockSet, false, true);
			
			// Find the closest block
			synchronized (search.results) {
				var close = search.results.stream()
					.filter(t -> t.dimension.equals(world.world.id))
					.min(Comparator.comparingDouble(t -> {
						if (!t.blocks.isEmpty()) {
							Position blockPos = t.blocks.get(0);
							return sc.getData(greenscripter.minecraft.play.data.PositionData.class).pos.squaredDistanceTo(blockPos.x, blockPos.y, blockPos.z);
						}
						return Double.MAX_VALUE;
					}));
					
				if (close.isPresent()) {
					WorldSearch.SearchResult result = close.get();
					// Remove the result from the list to prevent other tasks from claiming it
					search.results.remove(result);
					// Sort blocks by height (to mine from highest to lowest)
					result.blocks.sort(Comparator.comparingInt((Position p) -> -p.y));
					
					// In the actual implementation, we would push a BreakBlockState to the bot's state machine
					// For now, this is where we would integrate with the existing bot state machine system
					// This would be handled in the bot's main tick loop
					mining = true;
					setDebugState("Located block to mine at: " + result.blocks.get(0));
				}
			}

			if (mining) {
				setDebugState("Mining block: " + (currentCount) + "/" + target.getTargetCount());
			} else {
				setDebugState("Searching for blocks: " + String.join(", ", blockIds));
			}
			
			return this; // Continue mining
		}
	}

	@Override
	public String toDebugStringName() {
		return "MineBlockTask";
	}

	@Override
	public boolean isEqualResource(ResourceTask other) {
		if (other instanceof MineBlockTask o) {
			return target.equals(o.target) && java.util.Arrays.equals(blockIds, o.blockIds);
		}
		return false;
	}

	public boolean isMining() {
		return mining;
	}
	
	public String[] getBlockIds() {
		return blockIds;
	}
	
	public ItemTarget getTarget() {
		return target;
	}
}*/
