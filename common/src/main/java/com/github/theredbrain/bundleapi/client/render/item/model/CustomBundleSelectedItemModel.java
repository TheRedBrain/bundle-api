package com.github.theredbrain.bundleapi.client.render.item.model;

import com.mojang.serialization.MapCodec;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.item.ItemModelManager;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.render.item.model.ItemModel;
import net.minecraft.client.render.model.ResolvableModel;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.BundleContentsComponent;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.HeldItemContext;
import org.jspecify.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class CustomBundleSelectedItemModel implements ItemModel {
	static final ItemModel INSTANCE = new CustomBundleSelectedItemModel();

	@Override
	public void update(
			ItemRenderState state,
			ItemStack stack,
			ItemModelManager resolver,
			ItemDisplayContext displayContext,
			@Nullable ClientWorld world,
			@Nullable HeldItemContext heldItemContext,
			int seed
	) {
		state.addModelKey(this);
		BundleContentsComponent bundleContentsComponent = stack.get(DataComponentTypes.BUNDLE_CONTENTS);
		if (bundleContentsComponent != null && bundleContentsComponent.getSelectedStackIndex() != -1) {
			resolver.update(state, bundleContentsComponent.get(bundleContentsComponent.getSelectedStackIndex()), displayContext, world, heldItemContext, seed);
		}
	}

	@Environment(EnvType.CLIENT)
	public record Unbaked() implements ItemModel.Unbaked {
		public static final MapCodec<CustomBundleSelectedItemModel.Unbaked> CODEC = MapCodec.unit(new CustomBundleSelectedItemModel.Unbaked());

		@Override
		public MapCodec<CustomBundleSelectedItemModel.Unbaked> getCodec() {
			return CODEC;
		}

		@Override
		public ItemModel bake(ItemModel.BakeContext context) {
			return CustomBundleSelectedItemModel.INSTANCE;
		}

		@Override
		public void resolve(ResolvableModel.Resolver resolver) {
		}
	}
}
