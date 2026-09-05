package com.github.theredbrain.bundleapi.mixin.registry;

import com.github.theredbrain.bundleapi.BundleAPI;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DataComponents.class)
public class DataComponentTypesMixin {
    // Static class init tail inject
    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void static_init_TAIL_BundleAPI(CallbackInfo ci) {
        Registry.register(
                BuiltInRegistries.DATA_COMPONENT_TYPE,
                BundleAPI.identifier("custom_bundle_contents"),
                BundleAPI.CUSTOM_BUNDLE_CONTENTS_COMPONENT
        );
    }
}
