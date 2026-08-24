package com.github.theredbrain.bundleapi_test;

import com.github.theredbrain.bundleapi.BundleAPI;
import com.github.theredbrain.bundleapi.component.type.CustomBundleContentsComponent;
import com.github.theredbrain.bundleapi.item.CustomBundleItem;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
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

	private static Item registerItem(String name, Function<Item.Settings, Item> factory, @Nullable RegistryKey<ItemGroup> itemGroup) {
		RegistryKey<Item> registryKey = RegistryKey.of(RegistryKeys.ITEM, identifier(name));
		// Since 1.21.2 every Item.Settings must carry its registry key or item construction crashes.
		Item item = factory.apply(new Item.Settings().registryKey(registryKey));

		if (itemGroup != null) {
			ItemGroupEvents.modifyEntriesEvent(itemGroup).register(content -> {
				content.add(item);
			});
		}
		return Registry.register(Registries.ITEM, registryKey, item);
	}

	public static final TagKey<Item> TEST_BUNDLE_TAG = TagKey.of(RegistryKeys.ITEM, identifier("test_bundle_tag"));

	public static Item TEST_BUNDLE = registerItem(
			"test_bundle",
			settings -> new CustomBundleItem(settings
					.maxCount(1)
					.component(BundleAPI.CUSTOM_BUNDLE_CONTENTS_COMPONENT, CustomBundleContentsComponent.builder().size_multiplier(2).build())
			),
			ItemGroups.OPERATOR
	);

	public static Item TEST_QUIVER = registerItem(
			"test_quiver",
			settings -> new CustomBundleItem(TEST_BUNDLE_TAG, settings
					.maxCount(1)
					.component(BundleAPI.CUSTOM_BUNDLE_CONTENTS_COMPONENT, CustomBundleContentsComponent.builder().size_multiplier(3).build())
			),
			ItemGroups.OPERATOR
	);

	public static Identifier identifier(String path) {
		return Identifier.of(MOD_ID, path);
	}
}
