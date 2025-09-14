package com.github.theredbrain.bundleapi.mixin.client.gui.tooltip;

import com.github.theredbrain.bundleapi.client.gui.tooltip.CustomBundleTooltipComponent;
import com.github.theredbrain.bundleapi.item.tooltip.CustomBundleTooltipData;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.item.tooltip.TooltipData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TooltipComponent.class)
public interface TooltipComponentMixin {

	@Inject(
			method = "of(Lnet/minecraft/item/tooltip/TooltipData;)Lnet/minecraft/client/gui/tooltip/TooltipComponent;",
			at = @At("HEAD"),
			cancellable = true
	)
	private static void of(TooltipData data, CallbackInfoReturnable<TooltipComponent> cir) {
		if (data instanceof CustomBundleTooltipData customBundleTooltipData) {
			cir.setReturnValue(new CustomBundleTooltipComponent(customBundleTooltipData.contents()));
			cir.cancel();
		}
	}

}
