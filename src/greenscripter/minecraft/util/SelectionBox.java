package greenscripter.minecraft.util;

import greenscripter.minecraft.utils.Position;

public class SelectionBox {
	private Position pos1;
	private Position pos2;
	private boolean isSet1 = false;
	private boolean isSet2 = false;
	
	public void setPos1(Position pos) {
		this.pos1 = pos;
		isSet1 = true;
	}
	
	public void setPos2(Position pos) {
		this.pos2 = pos;
		isSet2 = true;
	}
	
	public boolean isComplete() {
		return isSet1 && isSet2;
	}
	
	public Position getPos1() {
		return pos1;
	}
	
	public Position getPos2() {
		return pos2;
	}
	
	public Position[] getAllBlockPositions() {
		if (!isComplete()) {
			return new Position[0];
		}
		
		// Calculate the min and max coordinates for the selection
		int minX = Math.min((int) pos1.x, (int) pos2.x);
		int minY = Math.min((int) pos1.y, (int) pos2.y);
		int minZ = Math.min((int) pos1.z, (int) pos2.z);
		int maxX = Math.max((int) pos1.x, (int) pos2.x);
		int maxY = Math.max((int) pos1.y, (int) pos2.y);
		int maxZ = Math.max((int) pos1.z, (int) pos2.z);
		
		// Calculate total number of blocks
		int totalBlocks = (maxX - minX + 1) * (maxY - minY + 1) * (maxZ - minZ + 1);
		Position[] positions = new Position[totalBlocks];
		
		int index = 0;
		for (int x = minX; x <= maxX; x++) {
			for (int y = minY; y <= maxY; y++) {
				for (int z = minZ; z <= maxZ; z++) {
					positions[index++] = new Position(x, y, z);
				}
			}
		}
		
		return positions;
	}
	
	public void clear() {
		pos1 = null;
		pos2 = null;
		isSet1 = false;
		isSet2 = false;
	}
	
	@Override
	public String toString() {
		if (!isSet1 && !isSet2) {
			return "No selection set";
		} else if (!isSet2) {
			return "First position set: " + pos1;
		} else {
			return "Selection: " + pos1 + " to " + pos2 + 
				   " (" + getAllBlockPositions().length + " blocks)";
		}
	}
}