package greenscripter.minecraft.play.handler;

import java.io.IOException;

import greenscripter.minecraft.ServerConnection;
import greenscripter.minecraft.play.data.AutoTotemData;
import greenscripter.minecraft.play.data.InventoryData;
import greenscripter.minecraft.play.inventory.ItemId;
import greenscripter.minecraft.play.inventory.ItemUtils;

public class AutoTotemHandler extends PlayHandler {

    private int totem = ItemId.get("minecraft:totem_of_undying");
    private long joinTime = 0;
    private boolean initialized = false;
    private AutoLogHandler autoLogHandler;

    public AutoTotemHandler(AutoLogHandler autoLogHandler) {
        this.autoLogHandler = autoLogHandler;
    }

    public void tick(ServerConnection sc) throws IOException {
        // Check if autototem is enabled
        AutoTotemData data = sc.getData(AutoTotemData.class);
        if (data != null && !data.enabled) {
            return; // Skip if disabled
        }
        
        // Initialize join time when first tick happens
        if (!initialized) {
            joinTime = System.currentTimeMillis();
            initialized = true;
        }
        
        InventoryData inv = sc.getData(InventoryData.class);
        if (inv == null || inv.inv == null) return;
        
        var offhand = inv.inv.getOffhand();
        if (!offhand.present || offhand.itemId != totem) {
            var totems = ItemUtils.getSlotsMatching(ItemUtils.matchesId(totem), inv.getInvIt());
            if (!totems.isEmpty()) {
                // Move totem to offhand
                inv.swapOffhand(totems.getFirst());
                
                long currentTime = System.currentTimeMillis();
                // If 5 seconds have passed since join, notify autolog
                if (currentTime - joinTime >= 5000) {
                    if (autoLogHandler != null) {
                        autoLogHandler.totemPlaced(sc);
                    }
                }
                // If less than 5 seconds, don't notify autolog
            }
        }
    }
    
    public boolean handlesTick() {
        return true;
    }
    
    public java.util.List<Integer> handlesPackets() {
        return java.util.List.of(); // No specific packets to handle
    }
    
    public void handleDisconnect(ServerConnection sc) {
        System.out.println("[Bot " + sc.name + "] AutoTotemHandler disconnected");
    }
}