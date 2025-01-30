package com.github.theredbrain.bundleapi.mixin.client;

import com.github.theredbrain.bundleapi.BundleAPIClient;
import com.github.theredbrain.bundleapi.item.CustomBundleItem;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {
	@Inject(method = "run", at = @At("HEAD"))
	private void bundleapi$run(CallbackInfo ci) {
		for (CustomBundleItem customBundleItem : CustomBundleItem.instances) {
			BundleAPIClient.registerModelPredicateProviders(customBundleItem);
		}
	}
}