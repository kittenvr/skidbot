package greenscripter.minecraft.commands;

import java.util.List;

import greenscripter.minecraft.ServerConnection;

public class AutoEatCommand extends ConsoleCommand {

	// Static reference to the shared AutoEatHandler instance (set by the main class)
	public static greenscripter.minecraft.play.handler.AutoEatHandler autoEatHandler;
	
	public AutoEatCommand() {
		super("autoeat", "!autoeat - Toggle the auto-eat handler on/off", List.of("eat"), true);
	}

	@Override
	public void execute(ServerConnection serverConnection, String[] args, String targetUsername, ConsoleCommandRegistry registry) {
		if (autoEatHandler != null) {
			autoEatHandler.toggle();
		} else {
			System.out.println("[System] AutoEatHandler not available - make sure you're running with AutoEatBot");
		}
	}
}