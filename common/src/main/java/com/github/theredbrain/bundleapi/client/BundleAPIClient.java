package com.github.theredbrain.bundleapi.client;

import com.github.theredbrain.bundleapi.item.CustomBundleItem;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;

public class BundleAPIClient {
	public static void init() {
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.
	}

	public static void registerModelPredicateProviders(Item item) {
		ModelPredicateProviderRegistry.register(item, Identifier.of("filled"), (stack, world, entity, seed) -> CustomBundleItem.getAmountFilled(stack));
	}
}