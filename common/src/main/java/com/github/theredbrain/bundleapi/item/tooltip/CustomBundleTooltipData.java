package com.github.theredbrain.bundleapi.item.tooltip;

import com.github.theredbrain.bundleapi.component.type.CustomBundleContentsComponent;
import net.minecraft.item.tooltip.TooltipData;

public record CustomBundleTooltipData(CustomBundleContentsComponent contents) implements TooltipData {
}
