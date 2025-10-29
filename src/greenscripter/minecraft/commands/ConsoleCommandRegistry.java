package greenscripter.minecraft.commands;

import java.util.HashMap;
import java.util.Map;

import greenscripter.minecraft.AsyncSwarmController;
import greenscripter.minecraft.ServerConnection;


public class ConsoleCommandRegistry {

	private final Map<String, ConsoleCommand> commands = new HashMap<>();
	private final Map<String, String> aliases = new HashMap<>();

	public void register(ConsoleCommand command) {
		if (command == null) {
			throw new IllegalArgumentException("ConsoleCommand cannot be null");
		}

		commands.put(command.name.toLowerCase(), command);
		for (String alias : command.aliases) {
			aliases.put(alias.toLowerCase(), command.name.toLowerCase());
		}
	}

	public ConsoleCommand get(String commandName) {
		if (commandName == null) return null;

		String name = commandName.toLowerCase();

		ConsoleCommand command = commands.get(name);
		if (command != null) {
			return command;
		}

		String aliasTarget = aliases.get(name);
		if (aliasTarget != null) {
			return commands.get(aliasTarget);
		}

		return null;
	}

	public void executeConsoleCommand(AsyncSwarmController controller, String message) {
		if (!message.startsWith("!")) return;

		String command = message.substring(1);
		String[] args = command.split(" ", -1); // Use -1 to keep empty trailing strings
		String commandName = args[0];

		String[] actualArgs = args;

		ConsoleCommand cmd = get(commandName);
		if (cmd == null) {
			System.out.println("[System] Error: Command \"" + commandName + "\" doesn't exist");
			return;
		}

		try {
			// Execute command for all server connections
			for (ServerConnection sc : controller.getAlive()) {
				cmd.execute(sc, actualArgs, null, this);
			}
		} catch (Exception e) {
			System.out.println("[System] Error executing command " + commandName + ": " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	public void executeTargetedConsoleCommand(AsyncSwarmController controller, String message, String targetUsername) {
		if (!message.startsWith("!")) return;

		String command = message.substring(1);
		String[] args = command.split(" ", -1); // Use -1 to keep empty trailing strings
		String commandName = args[0];

		String[] actualArgs = args;

		ConsoleCommand cmd = get(commandName);
		if (cmd == null) {
			System.out.println("[System] Error: Command \"" + commandName + "\" doesn't exist");
			return;
		}

		try {
			// Execute command only for the targeted server connection
			for (ServerConnection sc : controller.getAlive()) {
				if (sc.name.equals(targetUsername)) {
					cmd.execute(sc, actualArgs, targetUsername, this);
					return; // Found and executed on the target, exit
				}
			}
			System.out.println("[System] Bot " + targetUsername + " not found");
		} catch (Exception e) {
			System.out.println("[System] Error executing command " + commandName + ": " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	public void executeConsoleCommandForConnection(ServerConnection sc, String message) {
		if (!message.startsWith("!")) return;

		String command = message.substring(1);
		String[] args = command.split(" ", -1); // Use -1 to keep empty trailing strings
		String commandName = args[0];

		String[] actualArgs = args;

		ConsoleCommand cmd = get(commandName);
		if (cmd == null) {
			System.out.println("[System] Error: Command \"" + commandName + "\" doesn't exist");
			return;
		}

		try {
			// Execute command for a specific server connection only
			cmd.execute(sc, actualArgs, null, this);
		} catch (Exception e) {
			System.out.println("[System] Error executing command " + commandName + ": " + e.getMessage());
			e.printStackTrace();
		}
	}

	private String[] prependCommandName(String commandName, String[] args) {
		String[] newArgs = new String[args.length + 1];
		newArgs[0] = commandName;
		System.arraycopy(args, 0, newArgs, 1, args.length);
		return newArgs;
	}

	public ConsoleCommand[] getAllCommands() {
		return commands.values().toArray(new ConsoleCommand[0]);
	}
	
	public Map<String, ConsoleCommand> getCommands() {
		return new HashMap<>(commands);
	}
	
	public Map<String, String> getAliases() {
		return new HashMap<>(aliases);
	}
}