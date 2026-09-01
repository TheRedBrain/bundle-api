package com.github.theredbrain.bundleapi.mixin.world.inventory;

import com.github.theredbrain.bundleapi.component.type.CustomBundleContentsComponent;
import com.github.theredbrain.bundleapi.registry.BundleAPIDataComponentTypes;
import com.github.theredbrain.bundleapi.world.inventory.DuckAbstractContainerMenuMixin;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AbstractContainerMenu.class)
public class AbstractContainerMenuMixin implements DuckAbstractContainerMenuMixin {

	@WrapOperation(method = "setSelectedBundleItemIndex(II)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/BundleItem;toggleSelectedItem(Lnet/minecraft/world/item/ItemStack;I)V"))
	public void bundleapi$wrap_toggleSelectedItem(ItemStack itemStack, int i, Operation<Void> original) {

		CustomBundleContentsComponent customBundleContentsComponent = itemStack.get(BundleAPIDataComponentTypes.CUSTOM_BUNDLE_CONTENTS_COMPONENT);
		if (customBundleContentsComponent != null) {
			CustomBundleContentsComponent.Mutable mutable = new CustomBundleContentsComponent.Mutable(customBundleContentsComponent);
			mutable.toggleSelectedItem(i);
			itemStack.set(BundleAPIDataComponentTypes.CUSTOM_BUNDLE_CONTENTS_COMPONENT, mutable.toImmutable());
		} else {
			original.call(itemStack, i);
		}
	}

}
