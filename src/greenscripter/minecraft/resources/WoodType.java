package greenscripter.minecraft.resources;

public enum WoodType {
	OAK("oak"),
	SPRUCE("spruce"),
	BIRCH("birch"),
	JUNGLE("jungle"),
	ACACIA("acacia"),
	DARK_OAK("dark_oak"),
	MANGROVE("mangrove"),
	CHERRY("cherry"),
	CRIMSON("crimson"),
	WARPED("warped");

	public final String prefix;

	WoodType(String prefix) {
		this.prefix = prefix;
	}
}