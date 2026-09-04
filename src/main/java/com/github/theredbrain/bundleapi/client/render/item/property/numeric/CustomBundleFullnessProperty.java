package com.github.theredbrain.bundleapi.client.render.item.property.numeric;

import com.github.theredbrain.bundleapi.component.type.CustomBundleContentsComponent;
import com.github.theredbrain.bundleapi.registry.BundleAPIDataComponentTypes;
import com.mojang.serialization.MapCodec;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperty;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

@Environment(EnvType.CLIENT)
public record CustomBundleFullnessProperty() implements RangeSelectItemModelProperty {
	public static final MapCodec<CustomBundleFullnessProperty> CODEC = MapCodec.unit(new CustomBundleFullnessProperty());

	@Override
	public float get(ItemStack stack, @Nullable ClientLevel world, @Nullable ItemOwner context, int seed) {
		CustomBundleContentsComponent customBundleContentsComponent = stack.getOrDefault(BundleAPIDataComponentTypes.CUSTOM_BUNDLE_CONTENTS_COMPONENT, CustomBundleContentsComponent.DEFAULT);
		return customBundleContentsComponent.getOccupancy().floatValue();
	}

	@Override
	public MapCodec<CustomBundleFullnessProperty> type() {
		return CODEC;
	}
}
