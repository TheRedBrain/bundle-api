package com.github.theredbrain.bundleapi_test;

import com.github.theredbrain.bundleapi.component.type.CustomBundleContentsComponent;
import com.github.theredbrain.bundleapi.item.CustomBundleItem;
import com.github.theredbrain.bundleapi.registry.BundleAPIDataComponentTypes;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class BundleAPITest implements ModInitializer {
	public static final String MOD_ID = "bundleapi_test";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Testing Bundle API!");
	}

	private static Item registerItem(ResourceKey<Item> key, Item item, List<ResourceKey<CreativeModeTab>> itemGroupList) {

		for (ResourceKey<CreativeModeTab> itemGroup : itemGroupList) {
			ItemGroupEvents.modifyEntriesEvent(itemGroup).register(content -> {
				content.accept(item);
			});
		}
		return Registry.register(BuiltInRegistries.ITEM, key, item);
	}

	public static final TagKey<Item> TEST_BUNDLE_TAG = TagKey.create(Registries.ITEM, identifier("test_bundle_tag"));

	public static ResourceKey<Item> TEST_BUNDLE_KEY = ResourceKey.create(Registries.ITEM, identifier("test_bundle"));
	public static Item TEST_BUNDLE = registerItem(TEST_BUNDLE_KEY, new CustomBundleItem(new Item.Properties()
					.setId(TEST_BUNDLE_KEY)
					.stacksTo(1)
					.component(BundleAPIDataComponentTypes.CUSTOM_BUNDLE_CONTENTS_COMPONENT, CustomBundleContentsComponent.builder().size_multiplier(2).toImmutable())
			),
			List.of(CreativeModeTabs.OP_BLOCKS)
	);

	public static ResourceKey<Item> TEST_QUIVER_KEY = ResourceKey.create(Registries.ITEM, identifier("test_quiver"));
	public static Item TEST_QUIVER = registerItem(TEST_QUIVER_KEY, new CustomBundleItem(new Item.Properties()
					.setId(TEST_QUIVER_KEY)
					.stacksTo(1)
					.component(BundleAPIDataComponentTypes.CUSTOM_BUNDLE_CONTENTS_COMPONENT, CustomBundleContentsComponent.builder().size_multiplier(3).tag(Optional.ofNullable(TEST_BUNDLE_TAG)).toImmutable())
			),
			List.of(CreativeModeTabs.OP_BLOCKS)
	);

	public static Identifier identifier(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}