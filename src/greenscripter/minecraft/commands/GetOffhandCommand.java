package greenscripter.minecraft.commands;

import java.util.List;

import greenscripter.minecraft.ServerConnection;
import greenscripter.minecraft.play.data.InventoryData;
import greenscripter.minecraft.play.inventory.Slot;

public class GetOffhandCommand extends ConsoleCommand {

	public GetOffhandCommand() {
		super("getoffhand", "!getoffhand - Get the item in the offhand slot", java.util.List.of("offhand"));
	}

	@Override
	public void execute(ServerConnection serverConnection, String[] args, String targetUsername, ConsoleCommandRegistry registry) {
		// If a target username was specified, only apply to that specific bot
		if (targetUsername != null && !serverConnection.name.equals(targetUsername)) {
			return;
		}

		InventoryData inv = serverConnection.getData(InventoryData.class);
		if (inv == null) {
			System.out.println("[Bot " + serverConnection.name + "] Inventory data not available");
			return;
		}

		Slot offhandSlot = inv.inv.getOffhand();
		if (offhandSlot.present) {
			System.out.println("[Bot " + serverConnection.name + "] Offhand: " + offhandSlot.itemCount + "x " + offhandSlot.getItemId());
		} else {
			System.out.println("[Bot " + serverConnection.name + "] Offhand: Empty");
		}
	}
}