package greenscripter.minecraft.play.data;

import greenscripter.minecraft.ServerConnection;

public class AutoTotemData implements PlayData {
	
	public boolean enabled = true; // Default to true
	
	public void init(ServerConnection sc) {
		// Initialize with default value or load from config
		enabled = true;
	}
}