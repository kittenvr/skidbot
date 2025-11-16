package greenscripter.minecraft.util;

import java.util.Arrays;
import java.util.Objects;

public class ItemTarget {

	private final String itemName; // Minecraft item identifier like "minecraft:diamond_helmet" or catalogued name
	private final int targetCount;
	private final boolean isCatalogueItem;

	public ItemTarget(String itemName, int targetCount) {
		this.itemName = itemName;
		this.targetCount = targetCount;
		this.isCatalogueItem = false; // Assume it's a direct item ID by default
	}

	public ItemTarget(String itemName) {
		this(itemName, 1);
	}
	
	// Constructor for catalogue items
	public ItemTarget(String catalogueName, int targetCount, boolean isCatalogue) {
		this.itemName = catalogueName;
		this.targetCount = targetCount;
		this.isCatalogueItem = isCatalogue;
	}

	public String getItemName() {
		return itemName;
	}

	public int getTargetCount() {
		return targetCount;
	}
	
	public boolean isCatalogueItem() {
		return isCatalogueItem;
	}
	
	public String getCatalogueName() {
		return isCatalogueItem ? itemName : null;
	}

	@Override
	public String toString() {
		return itemName + " x" + targetCount + (isCatalogueItem ? " (catalogue)" : "");
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null || getClass() != obj.getClass()) return false;
		ItemTarget that = (ItemTarget) obj;
		return targetCount == that.targetCount && 
		       isCatalogueItem == that.isCatalogueItem && 
		       Objects.equals(itemName, that.itemName);
	}

	@Override
	public int hashCode() {
		return Objects.hash(itemName, targetCount, isCatalogueItem);
	}
}