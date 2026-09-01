package com.github.theredbrain.bundleapi.client.render.item.property.bool;

import com.github.theredbrain.bundleapi.component.type.CustomBundleContentsComponent;
import com.github.theredbrain.bundleapi.registry.BundleAPIDataComponentTypes;
import com.mojang.serialization.MapCodec;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.item.property.bool.BooleanProperty;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import org.jspecify.annotations.Nullable;

@Environment(EnvType.CLIENT)
public record CustomBundleHasSelectedItemProperty() implements BooleanProperty {
	public static final MapCodec<CustomBundleHasSelectedItemProperty> CODEC = MapCodec.unit(new CustomBundleHasSelectedItemProperty());

	@Override
	public boolean test(ItemStack stack, @Nullable ClientWorld world, @Nullable LivingEntity entity, int seed, ItemDisplayContext displayContext) {
		CustomBundleContentsComponent customBundleContentsComponent = stack.get(BundleAPIDataComponentTypes.CUSTOM_BUNDLE_CONTENTS_COMPONENT);
		return customBundleContentsComponent != null && customBundleContentsComponent.getSelectedStackIndex() != -1;
	}

	@Override
	public MapCodec<CustomBundleHasSelectedItemProperty> getCodec() {
		return CODEC;
	}
}
