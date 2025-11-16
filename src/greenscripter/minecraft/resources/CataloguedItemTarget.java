package greenscripter.minecraft.resources;

import greenscripter.minecraft.util.ItemTarget;

public class CataloguedItemTarget extends ItemTarget {

	private final String catalogueName;
	private final boolean isCatalogueItem;

	public CataloguedItemTarget(String itemId, int targetCount, boolean isCatalogueItem) {
		super(itemId, targetCount);
		this.catalogueName = isCatalogueItem ? itemId : null;
		this.isCatalogueItem = isCatalogueItem;
	}

	public boolean isCatalogueItem() {
		return isCatalogueItem;
	}

	public String getCatalogueName() {
		return catalogueName;
	}
}