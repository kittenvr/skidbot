package greenscripter.minecraft.commands;

import greenscripter.minecraft.ServerConnection;

public class ExampleCommand extends ConsoleCommand {

	public ExampleCommand() {
		super("example", "!example - Send 'Hello, World!' message", java.util.List.of("ex"));
	}

	@Override
	public void execute(ServerConnection serverConnection, String[] args, String targetUsername, ConsoleCommandRegistry registry) {
		if (serverConnection != null && (targetUsername == null || serverConnection.name.equals(targetUsername))) {
			// If called on a specific connection that matches target (or no target specified)
			System.out.println("[" + serverConnection.name + "] Hello, World!");
		} else if (serverConnection == null && targetUsername != null) {
			// If called globally but with a target, this should be handled by the registry
			// This shouldn't happen as the registry handles filtering
			System.out.println("[System] Hello, World! to user: " + targetUsername);
		} else if (targetUsername == null) {
			// If called globally without target
			System.out.println("[System] Hello, World!");
		}
	}
}