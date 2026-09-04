package com.github.theredbrain.bundleapi.mixin.client.render.item.model;

import com.github.theredbrain.bundleapi.BundleAPI;
import com.github.theredbrain.bundleapi.client.render.item.model.CustomBundleSelectedItemModel;
import com.mojang.serialization.MapCodec;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModels;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(ItemModels.class)
public class ItemModelTypesMixin {
	@Shadow
	@Final
	private static ExtraCodecs.LateBoundIdMapper<Identifier, MapCodec<? extends ItemModel.Unbaked>> ID_MAPPER;

	@Inject(method = "bootstrap", at = @At("TAIL"))
	private static void bundleapi$bootstrap(CallbackInfo ci) {
		ID_MAPPER.put(BundleAPI.identifier("custom_bundle/selected_item"), CustomBundleSelectedItemModel.Unbaked.CODEC);
	}
}
