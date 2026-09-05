package com.github.theredbrain.bundleapi.item.tooltip;

import com.github.theredbrain.bundleapi.component.type.CustomBundleContentsComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

/**
 * @param contents         the bundle's contents
 * @param emptyDescription the hint text drawn inside the tooltip while the bundle is empty
 *                         (vanilla's "Can hold a mixed stack of items" by default)
 */
public record CustomBundleTooltipData(CustomBundleContentsComponent contents, Component emptyDescription) implements TooltipComponent {
	/**
	 * Vanilla's empty-bundle hint, used by every bundle that does not provide its own.
	 */
	public static final Component DEFAULT_EMPTY_DESCRIPTION = Component.translatable("item.minecraft.bundle.empty.description");

	/**
	 * @deprecated Kept for binary compatibility, uses {@link #DEFAULT_EMPTY_DESCRIPTION}.
	 */
	@Deprecated
	public CustomBundleTooltipData(CustomBundleContentsComponent contents) {
		this(contents, DEFAULT_EMPTY_DESCRIPTION);
	}
}
