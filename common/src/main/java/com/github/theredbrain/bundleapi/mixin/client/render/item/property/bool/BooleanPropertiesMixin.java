package com.github.theredbrain.bundleapi.mixin.client.render.item.property.bool;

import com.github.theredbrain.bundleapi.BundleAPI;
import com.github.theredbrain.bundleapi.client.render.item.property.bool.CustomBundleHasSelectedItemProperty;
import com.mojang.serialization.MapCodec;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.item.property.bool.BooleanProperties;
import net.minecraft.client.render.item.property.bool.BooleanProperty;
import net.minecraft.util.Identifier;
import net.minecraft.util.dynamic.Codecs;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(BooleanProperties.class)
public class BooleanPropertiesMixin {

	@Shadow
	@Final
	private static Codecs.IdMapper<Identifier, MapCodec<? extends BooleanProperty>> ID_MAPPER;

	@Inject(method = "bootstrap", at = @At("TAIL"))
	private static void bundleapi$bootstrap(CallbackInfo ci) {
		ID_MAPPER.put(BundleAPI.identifier("custom_bundle/has_selected_item"), CustomBundleHasSelectedItemProperty.CODEC);
	}
}
