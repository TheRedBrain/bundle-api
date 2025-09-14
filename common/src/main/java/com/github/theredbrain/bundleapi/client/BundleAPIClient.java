package com.github.theredbrain.bundleapi.client;

import com.github.theredbrain.bundleapi.item.CustomBundleItem;
import com.github.theredbrain.bundleapi.mixin.client.ModelPredicateProviderRegistryInvoker;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;

public class BundleAPIClient {
	public static void init() {
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.
	}

	public static void registerModelPredicateProviders(Item item) {
		ModelPredicateProviderRegistryInvoker.invokeRegister(item, Identifier.of("filled"), (stack, world, entity, seed) -> CustomBundleItem.getAmountFilled(stack));
	}
}