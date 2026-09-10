package com.github.theredbrain.forge;

import com.github.theredbrain.bundleapi.BundleAPI;
import com.github.theredbrain.bundleapi.client.BundleAPIClient;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;

@Mod(BundleAPI.MOD_ID)
public final class ForgeMod {
    public ForgeMod() {
        // Run our common setup.
        BundleAPI.init();

        if (FMLEnvironment.dist == Dist.CLIENT) {
            // FMLClientSetupEvent fires after every mod's RegisterEvent, so all consumer bundle items exist by then.
            // Explicit event class: Forge 47's plain addListener(Consumer) infers the event type from the
            // lambda via TypeTools, which is fragile; the 4-arg overload takes it directly.
            FMLJavaModLoadingContext.get().getModEventBus().addListener(
                    EventPriority.NORMAL, false, FMLClientSetupEvent.class,
                    event -> event.enqueueWork(BundleAPIClient::init));
        }
    }
}
