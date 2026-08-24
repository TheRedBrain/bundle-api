package com.github.theredbrain.bundleapi.client.render.item.property.numeric;

import com.github.theredbrain.bundleapi.item.CustomBundleItem;
import com.mojang.serialization.MapCodec;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.item.property.numeric.NumericProperty;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.ItemStack;
import net.minecraft.util.HeldItemContext;
import org.jetbrains.annotations.Nullable;

/**
 * Item model numeric property {@code bundleapi:custom_bundle/fullness}.
 * <p>
 * Replaces the pre-1.21.4 {@code ModelPredicateProviderRegistry} "filled" predicate. Consumers author
 * an item model definition under {@code assets/<namespace>/items/<item>.json} using
 * {@code {"type":"minecraft:range_dispatch","property":"bundleapi:custom_bundle/fullness", ...}}.
 */
@Environment(EnvType.CLIENT)
public record CustomBundleFullnessProperty() implements NumericProperty {
	public static final MapCodec<CustomBundleFullnessProperty> CODEC = MapCodec.unit(new CustomBundleFullnessProperty());

	@Override
	public float getValue(ItemStack stack, @Nullable ClientWorld world, @Nullable HeldItemContext context, int seed) {
		return CustomBundleItem.getAmountFilled(stack);
	}

	@Override
	public MapCodec<CustomBundleFullnessProperty> getCodec() {
		return CODEC;
	}
}
