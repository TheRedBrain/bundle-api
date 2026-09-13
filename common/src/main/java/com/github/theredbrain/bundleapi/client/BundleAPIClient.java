package com.github.theredbrain.bundleapi.client;

import com.github.theredbrain.bundleapi.item.CustomBundleItem;
import com.github.theredbrain.bundleapi.mixin.client.ModelPredicateProviderRegistryInvoker;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;

public class BundleAPIClient {
	private static final Identifier FILLED = new Identifier("filled");

	/**
	 * Called from each platform's client setup, after all item registration has happened
	 * (Fabric: client entrypoints run after every main entrypoint; Forge: FMLClientSetupEvent).
	 * Registers the {@code filled} model predicate for every {@link CustomBundleItem} constructed so far.
	 * Items created later must call {@link #registerModelPredicateProviders(Item)} themselves.
	 */
	public static void init() {
		for (CustomBundleItem customBundleItem : CustomBundleItem.instances) {
			registerModelPredicateProviders(customBundleItem);
		}
	}

	public static void registerModelPredicateProviders(Item item) {
		ModelPredicateProviderRegistryInvoker.invokeRegister(item, FILLED, (stack, world, entity, seed) -> CustomBundleItem.getAmountFilled(stack));
	}
}
