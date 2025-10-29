package greenscripter.minecraft.commands;

import java.util.List;
import java.util.Locale;

import greenscripter.minecraft.ServerConnection;
import greenscripter.minecraft.play.data.InventoryData;
import greenscripter.minecraft.play.inventory.ItemId;
import greenscripter.minecraft.play.inventory.Slot;

public class CountCommand extends ConsoleCommand {

	public CountCommand() {
		super("count", "!count <itemname> - Count how many of a specific item are in inventory", java.util.List.of("itemcount"));
	}

	@Override
	public void execute(ServerConnection serverConnection, String[] args, String targetUsername, ConsoleCommandRegistry registry) {
		// If a target username was specified, only apply to that specific bot
		if (targetUsername != null && !serverConnection.name.equals(targetUsername)) {
			return;
		}
		
		if (args.length < 2) {
			System.out.println("[Bot " + serverConnection.name + "] Usage: !count <itemname>");
			System.out.println("[Bot " + serverConnection.name + "] Example: !count diamond");
			System.out.println("[Bot " + serverConnection.name + "] Example: !count minecraft:diamond_sword");
			return;
		}

		String itemName = args[1].toLowerCase();
		// Add minecraft: prefix if not present for proper matching
		if (!itemName.contains(":")) {
			itemName = "minecraft:" + itemName;
		}

		InventoryData inv = serverConnection.getData(InventoryData.class);
		if (inv == null) {
			System.out.println("[Bot " + serverConnection.name + "] Inventory data not available");
			return;
		}

		// Search through all slots in the inventory
		int totalCount = 0;
		for (Slot slot : inv.inv.slots) {
			if (slot.present && slot.getItemId() != null && slot.getItemId().toLowerCase().contains(itemName)) {
				totalCount += slot.itemCount;
			}
		}

		if (totalCount > 0) {
			System.out.println("[Bot " + serverConnection.name + "] Found " + totalCount + "x " + itemName);
		} else {
			System.out.println("[Bot " + serverConnection.name + "] Found 0x " + itemName);
		}
	}
}