package com.github.theredbrain.bundleapi.item.tooltip;

import com.github.theredbrain.bundleapi.component.type.CustomBundleContentsComponent;
import net.minecraft.item.tooltip.TooltipData;
import net.minecraft.text.Text;

/**
 * @param contents         the bundle's contents
 * @param emptyDescription the hint text drawn inside the tooltip while the bundle is empty
 *                         (vanilla's "Can hold a mixed stack of items" by default)
 */
public record CustomBundleTooltipData(CustomBundleContentsComponent contents, Text emptyDescription) implements TooltipData {
	/**
	 * Vanilla's empty-bundle hint, used by every bundle that does not provide its own.
	 */
	public static final Text DEFAULT_EMPTY_DESCRIPTION = Text.translatable("item.minecraft.bundle.empty.description");

	/**
	 * @deprecated Kept for binary compatibility, uses {@link #DEFAULT_EMPTY_DESCRIPTION}.
	 */
	@Deprecated
	public CustomBundleTooltipData(CustomBundleContentsComponent contents) {
		this(contents, DEFAULT_EMPTY_DESCRIPTION);
	}
}
