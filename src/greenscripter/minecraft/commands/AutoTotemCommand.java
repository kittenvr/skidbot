package greenscripter.minecraft.commands;

import greenscripter.minecraft.ServerConnection;
import greenscripter.minecraft.play.data.AutoTotemData;
import greenscripter.minecraft.play.data.InventoryData;
import greenscripter.minecraft.play.inventory.Slot;

public class AutoTotemCommand extends ConsoleCommand {

	public AutoTotemCommand() {
		super("autototem", "!autototem [on|off] - Enable or disable automatic totem management", java.util.List.of("totem"));
	}

	@Override
	public void execute(ServerConnection serverConnection, String[] args, String targetUsername, ConsoleCommandRegistry registry) {
		// If a target username was specified, only apply to that specific bot
		if (targetUsername != null && !serverConnection.name.equals(targetUsername)) {
			return;
		}

		if (args.length < 2) {
			// Toggle the autototem behavior or show current status
			AutoTotemData data = serverConnection.getData(AutoTotemData.class);
			boolean currentStatus = data.enabled;
			
			data.enabled = !currentStatus;
			
			System.out.println("[Bot " + serverConnection.name + "] AutoTotem: " + (data.enabled ? "ENABLED" : "DISABLED"));
		} else {
			String action = args[1].toLowerCase();
			
			if (action.equals("on") || action.equals("enable")) {
				AutoTotemData data = serverConnection.getData(AutoTotemData.class);
				data.enabled = true;
				System.out.println("[Bot " + serverConnection.name + "] AutoTotem: ENABLED");
			} else if (action.equals("off") || action.equals("disable")) {
				AutoTotemData data = serverConnection.getData(AutoTotemData.class);
				data.enabled = false;
				System.out.println("[Bot " + serverConnection.name + "] AutoTotem: DISABLED");
			} else if (action.equals("status")) {
				AutoTotemData data = serverConnection.getData(AutoTotemData.class);
				System.out.println("[Bot " + serverConnection.name + "] AutoTotem Status: " + (data.enabled ? "ENABLED" : "DISABLED"));
				
				// Also show offhand status
				InventoryData inv = serverConnection.getData(InventoryData.class);
				if (inv != null) {
					var offhand = inv.inv.getOffhand();
					if (offhand.present) {
						System.out.println("[Bot " + serverConnection.name + "] Offhand: " + offhand.itemCount + "x " + offhand.getItemId());
					} else {
						System.out.println("[Bot " + serverConnection.name + "] Offhand: Empty");
					}
				}
			} else {
				System.out.println("[Bot " + serverConnection.name + "] Usage: !autototem [on|off|status]");
			}
		}
	}
}