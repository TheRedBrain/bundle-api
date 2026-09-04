package com.github.theredbrain.bundleapi.client.render.item.property.bool;

import com.github.theredbrain.bundleapi.component.type.CustomBundleContentsComponent;
import com.github.theredbrain.bundleapi.registry.BundleAPIDataComponentTypes;
import com.mojang.serialization.MapCodec;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

@Environment(EnvType.CLIENT)
public record CustomBundleHasSelectedItemProperty() implements ConditionalItemModelProperty {
	public static final MapCodec<CustomBundleHasSelectedItemProperty> CODEC = MapCodec.unit(new CustomBundleHasSelectedItemProperty());

	@Override
	public boolean get(ItemStack stack, @Nullable ClientLevel world, @Nullable LivingEntity entity, int seed, ItemDisplayContext displayContext) {
		CustomBundleContentsComponent customBundleContentsComponent = stack.get(BundleAPIDataComponentTypes.CUSTOM_BUNDLE_CONTENTS_COMPONENT);
		return customBundleContentsComponent != null && customBundleContentsComponent.getSelectedStackIndex() != -1;
	}

	@Override
	public MapCodec<CustomBundleHasSelectedItemProperty> type() {
		return CODEC;
	}
}
