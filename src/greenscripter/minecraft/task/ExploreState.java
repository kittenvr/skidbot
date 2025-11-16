/*
package greenscripter.minecraft.task;

import greenscripter.minecraft.ServerConnection;
import greenscripter.minecraft.play.data.PositionData;
import greenscripter.minecraft.play.statemachine.PlayerState;
import greenscripter.minecraft.utils.Vector;

public class ExploreState extends PlayerState {

	private int explorationX;
	private int explorationZ;

	public ExploreState(int explorationX, int explorationZ) {
		this.explorationX = explorationX;
		this.explorationZ = explorationZ;
	}

	@Override
	public String toString() {
		return "ExploreState to " + explorationX + ", " + explorationZ;
	}

	public void onInit(ServerConnection sc) {
		// Move to the exploration position
		PositionData pos = sc.getData(PositionData.class);
		if (pos != null) {
			// Just move to the exploration coordinates for now
			// In a real implementation, this would use proper pathfinding
			pos.setPosRotation(sc, new Vector(explorationX, pos.pos.y, explorationZ), pos.pitch, pos.yaw);
		}
	}
}*/
