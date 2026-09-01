package com.github.theredbrain.bundleapi.item.tooltip;

import com.github.theredbrain.bundleapi.component.type.CustomBundleContentsComponent;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

public record CustomBundleTooltipData(CustomBundleContentsComponent contents) implements TooltipComponent {
}
