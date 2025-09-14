package com.github.theredbrain.fabric;

import com.github.theredbrain.bundleapi.BundleAPI;
import net.fabricmc.api.ModInitializer;

public final class FabricMod implements ModInitializer {
    @Override
    public void onInitialize() {
        // Run our common setup.
        BundleAPI.init();
    }
}
