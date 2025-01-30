package com.github.theredbrain.bundleapi;

import com.github.theredbrain.bundleapi.item.CustomBundleItem;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;

public class BundleAPIClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.
	}

	public static void registerModelPredicateProviders(Item item) {
		ModelPredicateProviderRegistry.register(item, Identifier.of("filled"), (stack, world, entity, seed) -> CustomBundleItem.getAmountFilled(stack));
	}
}