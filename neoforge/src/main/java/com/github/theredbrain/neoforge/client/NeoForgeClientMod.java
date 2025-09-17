package com.github.theredbrain.neoforge.client;

import com.github.theredbrain.bundleapi.BundleAPI;
import com.github.theredbrain.bundleapi.client.BundleAPIClient;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(modid = BundleAPI.MOD_ID, value = Dist.CLIENT)
public class NeoForgeClientMod {
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        BundleAPIClient.init();
    }
}