package greenscripter.minecraft.resources;

import greenscripter.minecraft.util.ItemTarget;

public class SmeltTarget {
	private final ItemTarget result;
	private final ItemTarget input;
	private final ItemTarget[] optionalInputs;

	public SmeltTarget(ItemTarget result, ItemTarget input, ItemTarget... optionalInputs) {
		this.result = result;
		this.input = input;
		this.optionalInputs = optionalInputs;
	}

	public ItemTarget getResult() {
		return result;
	}

	public ItemTarget getInput() {
		return input;
	}

	public ItemTarget[] getOptionalInputs() {
		return optionalInputs;
	}
}