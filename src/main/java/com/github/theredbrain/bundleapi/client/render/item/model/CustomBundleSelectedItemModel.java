package com.github.theredbrain.bundleapi.client.render.item.model;

import com.mojang.serialization.MapCodec;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BundleContents;
import org.jspecify.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class CustomBundleSelectedItemModel implements ItemModel {
	static final ItemModel INSTANCE = new CustomBundleSelectedItemModel();

	@Override
	public void update(
			ItemStackRenderState state,
			ItemStack stack,
			ItemModelResolver resolver,
			ItemDisplayContext displayContext,
			@Nullable ClientLevel world,
			@Nullable ItemOwner heldItemContext,
			int seed
	) {
		state.appendModelIdentityElement(this);
		BundleContents bundleContentsComponent = stack.get(DataComponents.BUNDLE_CONTENTS);
		if (bundleContentsComponent != null && bundleContentsComponent.getSelectedItem() != -1) {
			resolver.appendItemLayers(state, bundleContentsComponent.getItemUnsafe(bundleContentsComponent.getSelectedItem()), displayContext, world, heldItemContext, seed);
		}
	}

	@Environment(EnvType.CLIENT)
	public record Unbaked() implements ItemModel.Unbaked {
		public static final MapCodec<Unbaked> CODEC = MapCodec.unit(new Unbaked());

		@Override
		public MapCodec<Unbaked> type() {
			return CODEC;
		}

		@Override
		public ItemModel bake(ItemModel.BakingContext context) {
			return CustomBundleSelectedItemModel.INSTANCE;
		}

		@Override
		public void resolveDependencies(ResolvableModel.Resolver resolver) {
		}
	}
}
