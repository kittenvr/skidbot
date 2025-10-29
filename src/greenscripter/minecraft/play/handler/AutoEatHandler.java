package greenscripter.minecraft.play.handler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import greenscripter.minecraft.AsyncSwarmController;
import greenscripter.minecraft.ServerConnection;
import greenscripter.minecraft.packet.c2s.play.UseItemPacket;
import greenscripter.minecraft.play.data.InventoryData;
import greenscripter.minecraft.play.data.PlayerData;
import greenscripter.minecraft.play.data.WorldData;
import greenscripter.minecraft.play.inventory.ItemId;
import greenscripter.minecraft.play.inventory.Slot;

/**
 * Handler that automatically feeds bots when their hunger drops to or below 20
 * Uses simple UseItemPacket - the server handles the eating timing automatically
 */
public class AutoEatHandler extends PlayHandler {
    
    private static final int HUNGER_THRESHOLD = 20;
    private static final int CHECK_INTERVAL_SECONDS = 3; // Check every 3 seconds
    private static final int COOLDOWN_SECONDS = 5; // Wait 5 seconds between eating attempts
    
    private final AsyncSwarmController controller;
    private final ScheduledExecutorService scheduler;
    private volatile boolean enabled;
    private final Map<ServerConnection, Long> lastEatTime = new ConcurrentHashMap<>();
    
    public AutoEatHandler(AsyncSwarmController controller) {
        this.controller = controller;
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "AutoEat-Handler");
            t.setDaemon(true);
            return t;
        });
        this.enabled = true; // On by default for convenience
        start();
    }
    
    /**
     * Start the auto-eat handler
     */
    private void start() {
        System.out.println("Auto-eat handler initialized (enabled by default). Use 'autoeat' to toggle.");
        
        scheduler.scheduleAtFixedRate(() -> {
            if (!enabled) {
                return;
            }
            
            try {
                var aliveBots = controller.getAlive();
                for (int i = 0; i < aliveBots.size(); i++) {
                    ServerConnection sc = aliveBots.get(i);
                    
                    // Check if bot needs food and try to feed it
                    if (needsFood(sc) && feedBot(sc)) {
                        String botName = getBotName(i);
                        PlayerData playerData = sc.getData(PlayerData.class);
                        int hunger = playerData != null ? playerData.food : 0;
                        System.out.printf("Auto-eat for %s (hunger: %d/20)%n", botName, hunger);
                    }
                }
            } catch (Exception e) {
                System.err.println("Error in auto-eat handler: " + e.getMessage());
            }
        }, 0, CHECK_INTERVAL_SECONDS, TimeUnit.SECONDS);
    }
    
    /**
     * Enable auto-eating
     */
    public void enable() {
        enabled = true;
        System.out.println("Auto-eat handler enabled");
    }
    
    /**
     * Disable auto-eating
     */
    public void disable() {
        enabled = false;
        System.out.println("Auto-eat handler disabled");
    }
    
    /**
     * Toggle auto-eating on/off
     */
    public void toggle() {
        if (enabled) {
            disable();
        } else {
            enable();
        }
    }
    
    /**
     * Check if auto-eating is enabled
     */
    public boolean isEnabled() {
        return enabled;
    }
    
    /**
     * Shutdown the handler
     */
    public void shutdown() {
        enabled = false;
        scheduler.shutdown();
    }
    
    /**
     * Check if a bot needs food (hunger at or below 20)
     */
    private boolean needsFood(ServerConnection sc) {
        PlayerData playerData = sc.getData(PlayerData.class);
        return playerData != null && playerData.food <= HUNGER_THRESHOLD;
    }
    
    /**
     * Check if a bot can actually eat (hunger is not full)
     */
    private boolean canEat(ServerConnection sc) {
        PlayerData playerData = sc.getData(PlayerData.class);
        return playerData != null && playerData.food < 20; // Can only eat if not at full hunger
    }
    
    /**
     * Try to feed a bot - includes proper timing to prevent race conditions
     */
    private boolean feedBot(ServerConnection sc) {
        // Check cooldown
        long currentTime = System.currentTimeMillis();
        Long lastEat = lastEatTime.get(sc);
        if (lastEat != null && (currentTime - lastEat) < COOLDOWN_SECONDS * 1000) {
            return false; // Still in cooldown
        }
        
        InventoryData inv = sc.getData(InventoryData.class);
        if (inv == null || inv.inv == null) {
            return false;
        }
        // First, try to eat food already in main hand
        Slot mainHandSlot = inv.getMainHandSlot();
        if (mainHandSlot.present && isFoodItem(mainHandSlot)) {
            return eatFood(sc);
        }
        // Check other hotbar slots for food
        for (int i = 0; i < 9; i++) {
            Slot hotbarSlot = inv.getActiveScreen().getHotbarSlot(i);
            if (hotbarSlot.present && isFoodItem(hotbarSlot)) {
                // Switch to this hotbar slot but DON'T eat immediately
                // Wait for server to process the hotbar change
                inv.setHotbarSlot(i);
                // Set cooldown to allow eating in 2 seconds
                lastEatTime.put(sc, currentTime - (COOLDOWN_SECONDS * 1000) + 2000);
                return false; // Will try to eat on next tick
            }
        }
        // No food in hotbar, look for food in inventory and move it to hotbar
        Slot bestFood = findBestFood(inv);
        if (bestFood != null) {
            // Find an empty hotbar slot or the current main hand slot
            int targetHotbarSlot = inv.hotbarSlot;
            
            // If main hand is not empty, try to find an empty hotbar slot
            if (mainHandSlot.present) {
                for (int i = 0; i < 9; i++) {
                    Slot hotbarSlot = inv.getActiveScreen().getHotbarSlot(i);
                    if (!hotbarSlot.present) {
                        targetHotbarSlot = i;
                        break;
                    }
                }
            }
            
            // Move food to the target hotbar slot but DON'T eat immediately
            // Wait for server to process the inventory changes
            inv.swapSlots(bestFood, targetHotbarSlot);
            inv.setHotbarSlot(targetHotbarSlot);
            // Set cooldown to allow eating in 2 seconds
            lastEatTime.put(sc, currentTime - (COOLDOWN_SECONDS * 1000) + 2000);
            return false; // Will try to eat on next tick
        }
        return false; // No food available
    }
    
    /**
     * Check if an item is food
     */
    private boolean isFoodItem(Slot slot) {
        if (!slot.present) {
            return false;
        }
        var itemInfo = slot.getItemInfo();
        return itemInfo != null && itemInfo.isFood;
    }
    
    /**
     * Find the best food item in inventory (highest hunger restoration)
     */
    private Slot findBestFood(InventoryData inv) {
        Slot bestFood = null;
        int bestHunger = 0;
        var invIterator = inv.getInvIt();
        while (invIterator.hasNext()) {
            Slot slot = invIterator.next();
            if (slot.present && isFoodItem(slot)) {
                var itemInfo = slot.getItemInfo();
                if (itemInfo.hunger > bestHunger) {
                    bestHunger = itemInfo.hunger;
                    bestFood = slot;
                }
            }
        }
        return bestFood;
    }
    
    /**
     * Actually eat the food in the main hand
     */
    private boolean eatFood(ServerConnection sc) {
        try {
            InventoryData inv = sc.getData(InventoryData.class);
            WorldData world = sc.getData(WorldData.class);
            PlayerData playerData = sc.getData(PlayerData.class);
            
            if (inv == null || world == null || playerData == null) return false;
            
            // Double-check that we actually have food in main hand before eating
            Slot mainHandSlot = inv.getMainHandSlot();
            if (!mainHandSlot.present || !isFoodItem(mainHandSlot)) {
                return false;
            }
            
            // Check if bot can actually eat (not at full hunger)
            if (playerData.food >= 20) {
                return false; // Can't eat when at full hunger
            }
            
            String itemName = ItemId.itemRegistry.get(mainHandSlot.itemId);
            System.out.printf("Attempting to eat %s (hunger: %d/20)%n", 
                itemName != null ? itemName : "unknown_food", playerData.food);
            
            // Try the eating packet with more debugging
            try {
                // Create the packet manually to debug
                UseItemPacket eatPacket = new UseItemPacket(UseItemPacket.MAIN_HAND, world.breakSeq++);
                eatPacket.pitch = playerData.pos.pitch;
                eatPacket.yaw = playerData.pos.yaw;
                System.out.printf("Sending UseItemPacket: hand=%d, sequence=%d, pitch=%.2f, yaw=%.2f%n", 
                    eatPacket.hand, eatPacket.sequence, eatPacket.pitch, eatPacket.yaw);
                
                sc.sendPacket(eatPacket);
                System.out.println("UseItemPacket sent successfully");
                
            } catch (Exception packetError) {
                System.err.println("Error sending UseItemPacket: " + packetError.getMessage());
                packetError.printStackTrace();
                return false;
            }
            
            // Update last eat time
            lastEatTime.put(sc, System.currentTimeMillis());
            
            return true;
        } catch (Exception e) {
            System.err.println("Error eating food: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Get bot name for display
     */
    private String getBotName(int index) {
        if (controller.botNames != null) {
            String name = controller.botNames.apply(index);
            return name != null ? name : "Bot " + index;
        }
        return "Bot " + index;
    }
}