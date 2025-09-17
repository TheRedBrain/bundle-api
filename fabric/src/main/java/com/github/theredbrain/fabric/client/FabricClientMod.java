package com.github.theredbrain.fabric.client;

import com.github.theredbrain.bundleapi.client.BundleAPIClient;
import net.fabricmc.api.ClientModInitializer;

public final class FabricClientMod implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BundleAPIClient.init();
    }
}
