package com.github.theredbrain.bundleapi.mixin.client.gui.tooltip;

import com.github.theredbrain.bundleapi.client.gui.tooltip.CustomBundleTooltipComponent;
import com.github.theredbrain.bundleapi.item.tooltip.CustomBundleTooltipData;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ClientTooltipComponent.class)
public interface ClientTooltipComponentMixin {

	@WrapMethod(
			method = "create(Lnet/minecraft/world/inventory/tooltip/TooltipComponent;)Lnet/minecraft/client/gui/screens/inventory/tooltip/ClientTooltipComponent;"
	)
	private static ClientTooltipComponent bundleapi$wrap_create(TooltipComponent tooltipData, Operation<ClientTooltipComponent> original) {
		if (tooltipData instanceof CustomBundleTooltipData customBundleTooltipData) {
			return new CustomBundleTooltipComponent(customBundleTooltipData.contents());
		}
		return original.call(tooltipData);
	}

}
