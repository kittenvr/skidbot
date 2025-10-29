package greenscripter.minecraft.commands;

import java.util.Map;

import greenscripter.minecraft.ServerConnection;

public class HelpCommand extends ConsoleCommand {

	public HelpCommand() {
		super("help", "!help - Show this help message", java.util.List.of("h", "?"));
	}

	@Override
	public void execute(ServerConnection serverConnection, String[] args, String targetUsername, ConsoleCommandRegistry registry) {
		System.out.println("Available commands:");
		
		// Display all registered commands
		Map<String, ConsoleCommand> commands = registry.getCommands();
		for (Map.Entry<String, ConsoleCommand> entry : commands.entrySet()) {
			ConsoleCommand cmd = entry.getValue();
			String aliases = cmd.aliases.isEmpty() ? "" : " (aliases: " + String.join(", ", cmd.aliases) + ")";
			System.out.println("  !" + cmd.name + " - " + cmd.usage + aliases);
		}
		System.out.println("Use !command to execute a command");
		System.out.println("Use :username !command to target a specific bot");
	}
}