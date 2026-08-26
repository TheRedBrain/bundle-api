package com.github.theredbrain.bundleapi.mixin.client.gui.tooltip;

import com.github.theredbrain.bundleapi.client.gui.tooltip.CustomBundleTooltipComponent;
import com.github.theredbrain.bundleapi.item.tooltip.CustomBundleTooltipData;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientTooltipComponent.class)
public interface TooltipComponentMixin {

	@Inject(
			method = "create(Lnet/minecraft/world/inventory/tooltip/TooltipComponent;)Lnet/minecraft/client/gui/screens/inventory/tooltip/ClientTooltipComponent;",
			at = @At("HEAD"),
			cancellable = true
	)
	private static void of(TooltipComponent data, CallbackInfoReturnable<ClientTooltipComponent> cir) {
		if (data instanceof CustomBundleTooltipData customBundleTooltipData) {
			cir.setReturnValue(new CustomBundleTooltipComponent(customBundleTooltipData.contents(), customBundleTooltipData.emptyDescription()));
			cir.cancel();
		}
	}

}
