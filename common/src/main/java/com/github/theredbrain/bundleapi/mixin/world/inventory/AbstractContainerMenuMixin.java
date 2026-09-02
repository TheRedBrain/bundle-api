package com.github.theredbrain.bundleapi.mixin.world.inventory;

import com.github.theredbrain.bundleapi.BundleAPI;
import com.github.theredbrain.bundleapi.component.type.CustomBundleContentsComponent;
import com.github.theredbrain.bundleapi.registry.BundleAPIDataComponentTypes;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerMenu.class)
public abstract class AbstractContainerMenuMixin {

//	@Inject(method = "setSelectedBundleItemIndex(II)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/BundleItem;toggleSelectedItem(Lnet/minecraft/world/item/ItemStack;I)V"))
//	private static void bundleapi$setSelectedBundleItemIndex(int i, int j, CallbackInfo ci, @Local(name = "itemStack") ItemStack itemStack) {
	@WrapOperation(method = "setSelectedBundleItemIndex(II)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/BundleItem;toggleSelectedItem(Lnet/minecraft/world/item/ItemStack;I)V"))
	public void bundleapi$wrap_toggleSelectedItem(ItemStack itemStack, int i, Operation<Void> original) {

		BundleAPI.LOGGER.info("bundleapi$wrap_toggleSelectedItem");
		CustomBundleContentsComponent customBundleContentsComponent = itemStack.get(BundleAPIDataComponentTypes.CUSTOM_BUNDLE_CONTENTS_COMPONENT);
		if (customBundleContentsComponent != null) {
			BundleAPI.LOGGER.info("customBundleContentsComponent != null");
			CustomBundleContentsComponent.Mutable mutable = new CustomBundleContentsComponent.Mutable(customBundleContentsComponent);
			mutable.toggleSelectedItem(i);
			itemStack.set(BundleAPIDataComponentTypes.CUSTOM_BUNDLE_CONTENTS_COMPONENT, mutable.toImmutable());
		} else {
			original.call(itemStack, i);
		}
	}

}
