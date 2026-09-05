package com.github.theredbrain.bundleapi_test;

import com.github.theredbrain.bundleapi.BundleAPI;
import com.github.theredbrain.bundleapi.component.type.CustomBundleContentsComponent;
import com.github.theredbrain.bundleapi.item.CustomBundleItem;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Function;

public class BundleAPITest implements ModInitializer {
	public static final String MOD_ID = "bundleapi_test";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Testing Bundle API!");
	}

	private static Item registerItem(String name, Function<Item.Properties, Item> factory, @Nullable ResourceKey<CreativeModeTab> itemGroup) {
		ResourceKey<Item> registryKey = ResourceKey.create(Registries.ITEM, identifier(name));
		// Since 1.21.2 every Item.Properties must carry its registry key or item construction crashes.
		Item item = factory.apply(new Item.Properties().setId(registryKey));

		if (itemGroup != null) {
			CreativeModeTabEvents.modifyOutputEvent(itemGroup).register(content -> {
				content.accept(item);
			});
		}
		return Registry.register(BuiltInRegistries.ITEM, registryKey, item);
	}

	public static final TagKey<Item> TEST_BUNDLE_TAG = TagKey.create(Registries.ITEM, identifier("test_bundle_tag"));

	public static Item TEST_BUNDLE = registerItem(
			"test_bundle",
			settings -> new CustomBundleItem(settings
					.stacksTo(1)
					.component(BundleAPI.CUSTOM_BUNDLE_CONTENTS_COMPONENT, CustomBundleContentsComponent.builder().size_multiplier(2).build())
			),
			CreativeModeTabs.OP_BLOCKS
	);

	public static Item TEST_QUIVER = registerItem(
			"test_quiver",
			settings -> new CustomBundleItem(TEST_BUNDLE_TAG, settings
					.stacksTo(1)
					.component(BundleAPI.CUSTOM_BUNDLE_CONTENTS_COMPONENT, CustomBundleContentsComponent.builder().size_multiplier(3).build())
			),
			CreativeModeTabs.OP_BLOCKS
	);

	public static Identifier identifier(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
