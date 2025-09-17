package com.github.theredbrain.bundleapi.mixin.registry;

import com.github.theredbrain.bundleapi.BundleAPI;
import net.minecraft.predicate.item.ItemSubPredicateTypes;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemSubPredicateTypes.class)
public class ItemSubPredicateTypesMixin {
    // Static class init tail inject
    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void static_init_TAIL_BundleAPI(CallbackInfo ci) {
        Registry.register(
                Registries.ITEM_SUB_PREDICATE_TYPE,
                "custom_bundle_contents",
                BundleAPI.CUSTOM_BUNDLE_CONTENTS_ITEM_SUB_PREDICATE
        );
    }
}
