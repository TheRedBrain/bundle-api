package com.github.theredbrain.neoforge;

import com.github.theredbrain.bundleapi.BundleAPI;
import net.neoforged.fml.common.Mod;

@Mod(BundleAPI.MOD_ID)
public final class NeoForgeMod {
    public NeoForgeMod() {
        // Run our common setup.
        BundleAPI.init();
    }
}
