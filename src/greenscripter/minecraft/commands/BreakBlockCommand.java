package greenscripter.minecraft.commands;

import java.util.List;

import greenscripter.minecraft.ServerConnection;
import greenscripter.minecraft.play.data.PositionData;
import greenscripter.minecraft.play.data.WorldData;
import greenscripter.minecraft.util.ToolUtils;
import greenscripter.minecraft.utils.Direction;
import greenscripter.minecraft.utils.Position;
import greenscripter.minecraft.packet.c2s.play.PlayerActionPacket;

public class BreakBlockCommand extends ConsoleCommand {

	public BreakBlockCommand() {
		super("breakblock", "!breakblock <x> <y> <z> - Break a single block at the specified coordinates", 
			List.of("break", "bb"));
	}

	@Override
	public void execute(ServerConnection serverConnection, String[] args, String targetUsername, ConsoleCommandRegistry registry) {
		if (args.length < 4) {
			System.out.println("[System] Usage: !breakblock <x> <y> <z>");
			System.out.println("[System] Example: !breakblock 100 64 -50");
			return;
		}

		// Parse coordinates
		int x, y, z;
		try {
			x = Integer.parseInt(args[1]);
			y = Integer.parseInt(args[2]);
			z = Integer.parseInt(args[3]);
		} catch (NumberFormatException e) {
			System.out.println("[System] Invalid coordinates format. Usage: !breakblock <x> <y> <z>");
			return;
		}

		// Get world and position data
		PositionData posData = serverConnection.getData(PositionData.class);
		WorldData worldData = serverConnection.getData(WorldData.class);

		if (worldData == null || worldData.world == null) {
			System.out.println("[Bot " + serverConnection.name + "] World data not available");
			return;
		}

		// Create position object for the target block
		Position targetBlock = new Position(x, y, z);

		// Check if block exists at the target location
		int blockId = worldData.world.getBlock(x, y, z);
		if (blockId == 0 || blockId == 16544) { // Air block
			System.out.println("[Bot " + serverConnection.name + "] No block at " + targetBlock);
			return;
		}

		// Check distance to block (center of block)
		greenscripter.minecraft.utils.Vector playerPos = new greenscripter.minecraft.utils.Vector(posData.pos.x, posData.pos.y, posData.pos.z);
		greenscripter.minecraft.utils.Vector blockCenter = new greenscripter.minecraft.utils.Vector(targetBlock.x + 0.5, targetBlock.y + 0.5, targetBlock.z + 0.5);
		double distance = playerPos.distanceTo(blockCenter);

		if (distance > 4.5) {
			System.out.println("[Bot " + serverConnection.name + "] Block at " + targetBlock + " is too far (" + String.format("%.2f", distance) + " blocks away). Max range is 4.5 blocks.");
			return;
		}

		// Select the best tool for this block
		ToolUtils.selectBestToolForBlock(serverConnection, targetBlock, worldData);
		
		// Send START_MINING packet to initiate breaking
		PlayerActionPacket startPacket = new PlayerActionPacket();
		startPacket.status = PlayerActionPacket.START_MINING;
		startPacket.pos = targetBlock;
		startPacket.face = (byte) Direction.UP.ordinal(); // Top face
		startPacket.sequence = 0;

		serverConnection.sendPacket(startPacket);

		// Send FINISH_MINING packet immediately after
		PlayerActionPacket finishPacket = new PlayerActionPacket();
		finishPacket.status = PlayerActionPacket.FINISH_MINING;
		finishPacket.pos = targetBlock;
		finishPacket.face = (byte) Direction.UP.ordinal(); // Top face
		finishPacket.sequence = 0;

		serverConnection.sendPacket(finishPacket);

		System.out.println("[Bot " + serverConnection.name + "] Attempting to break block at " + targetBlock);
	}
}