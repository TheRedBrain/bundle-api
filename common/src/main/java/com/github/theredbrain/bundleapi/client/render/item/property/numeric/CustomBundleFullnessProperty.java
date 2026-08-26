package com.github.theredbrain.bundleapi.client.render.item.property.numeric;

import com.github.theredbrain.bundleapi.item.CustomBundleItem;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperty;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * Item model numeric property {@code bundleapi:custom_bundle/fullness}.
 * <p>
 * Replaces the pre-1.21.4 {@code ModelPredicateProviderRegistry} "filled" predicate. Consumers author
 * an item model definition under {@code assets/<namespace>/items/<item>.json} using
 * {@code {"type":"minecraft:range_dispatch","property":"bundleapi:custom_bundle/fullness", ...}}.
 */
public record CustomBundleFullnessProperty() implements RangeSelectItemModelProperty {
	public static final MapCodec<CustomBundleFullnessProperty> CODEC = MapCodec.unit(new CustomBundleFullnessProperty());

	@Override
	public float get(ItemStack stack, @Nullable ClientLevel world, @Nullable ItemOwner context, int seed) {
		return CustomBundleItem.getAmountFilled(stack);
	}

	@Override
	public MapCodec<CustomBundleFullnessProperty> type() {
		return CODEC;
	}
}
