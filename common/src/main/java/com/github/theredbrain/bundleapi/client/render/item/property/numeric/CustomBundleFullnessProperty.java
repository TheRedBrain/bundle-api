package com.github.theredbrain.bundleapi.client.render.item.property.numeric;

import com.github.theredbrain.bundleapi.component.type.CustomBundleContentsComponent;
import com.github.theredbrain.bundleapi.registry.BundleAPIDataComponentTypes;
import com.mojang.serialization.MapCodec;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.item.property.numeric.NumericProperty;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.ItemStack;
import net.minecraft.util.HeldItemContext;
import org.jspecify.annotations.Nullable;

@Environment(EnvType.CLIENT)
public record CustomBundleFullnessProperty() implements NumericProperty {
	public static final MapCodec<CustomBundleFullnessProperty> CODEC = MapCodec.unit(new CustomBundleFullnessProperty());

	@Override
	public float getValue(ItemStack stack, @Nullable ClientWorld world, @Nullable HeldItemContext context, int seed) {
		CustomBundleContentsComponent customBundleContentsComponent = stack.getOrDefault(BundleAPIDataComponentTypes.CUSTOM_BUNDLE_CONTENTS_COMPONENT, CustomBundleContentsComponent.DEFAULT);
		return customBundleContentsComponent.getOccupancy().floatValue();
	}

	@Override
	public MapCodec<CustomBundleFullnessProperty> getCodec() {
		return CODEC;
	}
}
