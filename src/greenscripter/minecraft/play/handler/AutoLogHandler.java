package greenscripter.minecraft.play.handler;

import java.util.List;

import greenscripter.minecraft.ServerConnection;
import greenscripter.minecraft.play.data.InventoryData;
import greenscripter.minecraft.play.inventory.ItemId;

public class AutoLogHandler extends PlayHandler {

	private boolean hasTotem = false; // Track if totem is currently in offhand
	private long lastCheckTime = 0;
	
	public void handlePacket(Object p, ServerConnection sc) {
		// Check if a totem was used by monitoring for specific packets that indicate totem usage
		// This could be entity equipment changes or other packets indicating offhand item use
	}
	
	public void tick(ServerConnection sc) {
		// Request inventory update for desync checking
		InventoryData inv = sc.getData(InventoryData.class);
		if (inv != null) {
			inv.rerequestInventory();
		}

		// Check inventory to see if totem is still in offhand
		if (inv != null && inv.inv != null) {
			var offhand = inv.inv.getOffhand();
			int totemId = ItemId.get("minecraft:totem_of_undying");
			
			// Check if totem was in offhand in the previous check but is now gone
			if (hasTotem && (!offhand.present || offhand.itemId != totemId)) {
				// Totem was used, disconnect the bot
				System.out.println("[Bot " + sc.name + "] Totem of undying used, disconnecting...");
				// Perform instant disconnect by closing the socket
				try {
					if (sc.socket != null) {
						sc.socket.close();
					}
				} catch (Exception e) {
					System.err.println("[Bot " + sc.name + "] Error disconnecting: " + e.getMessage());
				}
			}
			
			// Update the hasTotem status for next tick
			hasTotem = offhand.present && offhand.itemId == totemId;
			lastCheckTime = System.currentTimeMillis();
		}
	}
	
	public boolean handlesTick() {
		return true;
	}
	
	public java.util.List<Integer> handlesPackets() {
		return java.util.List.of(); // No specific packets to handle
	}
	
	public void handleDisconnect(ServerConnection sc) {
		System.out.println("[Bot " + sc.name + "] Disconnected by AutoLogHandler");
	}
	
	// Method to be called by AutoTotemHandler when totem is placed in offhand
	public void totemPlaced(ServerConnection sc) {
		// Update the status that we now have a totem in offhand
		hasTotem = true;
	}
}