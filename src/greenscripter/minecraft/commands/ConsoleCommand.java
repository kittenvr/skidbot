package greenscripter.minecraft.commands;

import java.util.List;

import greenscripter.minecraft.ServerConnection;

/**
 * Base class for console commands
 */
public abstract class ConsoleCommand {

	public final String name;
	public final String usage;
	public final List<String> aliases;
	public final boolean singleBotOnly;

	public ConsoleCommand(String name, String usage, List<String> aliases) {
		this(name, usage, aliases, false);
	}

	public ConsoleCommand(String name, String usage, List<String> aliases, boolean singleBotOnly) {
		this.name = name;
		this.usage = usage;
		this.aliases = aliases != null ? aliases : List.of();
		this.singleBotOnly = singleBotOnly;
	}

	/**
	 * Execute the command
	 * @param serverConnection The server connection instance
	 * @param args Command arguments
	 * @param targetUsername Target username (null if command applies to all bots)
	 * @param registry The command registry (for commands that need to query available commands)
	 */
	public abstract void execute(ServerConnection serverConnection, String[] args, String targetUsername, ConsoleCommandRegistry registry);

	/**
	 * Get command usage string
	 */
	public String getUsage() {
		return "!" + this.name;
	}
}