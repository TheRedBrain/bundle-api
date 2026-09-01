package com.github.theredbrain.bundleapi.mixin.client.gui.tooltip;

import com.github.theredbrain.bundleapi.client.gui.tooltip.CustomBundleTooltipComponent;
import com.github.theredbrain.bundleapi.item.tooltip.CustomBundleTooltipData;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.item.tooltip.TooltipData;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(TooltipComponent.class)
public interface TooltipComponentMixin {

	@WrapMethod(
			method = "of(Lnet/minecraft/item/tooltip/TooltipData;)Lnet/minecraft/client/gui/tooltip/TooltipComponent;"
	)
	private static TooltipComponent bundleapi$wrap_of(TooltipData tooltipData, Operation<TooltipComponent> original) {
		if (tooltipData instanceof CustomBundleTooltipData customBundleTooltipData) {
			return new CustomBundleTooltipComponent(customBundleTooltipData.contents());
		}
		return original.call(tooltipData);
	}

}
