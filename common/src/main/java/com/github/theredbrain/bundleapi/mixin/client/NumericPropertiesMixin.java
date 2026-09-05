package com.github.theredbrain.bundleapi.mixin.client;

import com.github.theredbrain.bundleapi.BundleAPI;
import com.github.theredbrain.bundleapi.client.render.item.property.numeric.CustomBundleFullnessProperty;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperties;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperty;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Registers BundleAPI's item model numeric properties. {@code NumericProperties} has no public
 * registration hook, so the id mapper is shadowed and appended to during vanilla's bootstrap.
 */
@Mixin(RangeSelectItemModelProperties.class)
public class NumericPropertiesMixin {
    @Shadow
    @Final
    private static ExtraCodecs.LateBoundIdMapper<Identifier, MapCodec<? extends RangeSelectItemModelProperty>> ID_MAPPER;

    @Inject(method = "bootstrap", at = @At("TAIL"))
    private static void bundleapi$bootstrap(CallbackInfo ci) {
        ID_MAPPER.put(BundleAPI.identifier("custom_bundle/fullness"), CustomBundleFullnessProperty.CODEC);
    }
}
