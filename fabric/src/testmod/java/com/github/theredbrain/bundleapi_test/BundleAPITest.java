package com.github.theredbrain.bundleapi_test;

import com.github.theredbrain.bundleapi.component.type.CustomBundleContentsComponent;
import com.github.theredbrain.bundleapi.item.CustomBundleItem;
import com.github.theredbrain.bundleapi.registry.BundleAPIDataComponentTypes;
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

	private static Item registerItem(RegistryKey<Item> key, Item item, List<RegistryKey<ItemGroup>> itemGroupList) {

		for (RegistryKey<ItemGroup> itemGroup : itemGroupList) {
			ItemGroupEvents.modifyEntriesEvent(itemGroup).register(content -> {
				content.add(item);
			});
		}
		return Registry.register(Registries.ITEM, key, item);
	}

	public static final TagKey<Item> TEST_BUNDLE_TAG = TagKey.of(RegistryKeys.ITEM, identifier("test_bundle_tag"));

	public static RegistryKey<Item> TEST_BUNDLE_KEY = RegistryKey.of(RegistryKeys.ITEM, identifier("test_bundle"));
	public static Item TEST_BUNDLE = registerItem(TEST_BUNDLE_KEY, new CustomBundleItem(new Item.Settings()
					.registryKey(TEST_BUNDLE_KEY)
					.maxCount(1)
					.component(BundleAPIDataComponentTypes.CUSTOM_BUNDLE_CONTENTS_COMPONENT, CustomBundleContentsComponent.builder().size_multiplier(2).build())
			),
			List.of(ItemGroups.OPERATOR)
	);

	public static RegistryKey<Item> TEST_QUIVER_KEY = RegistryKey.of(RegistryKeys.ITEM, identifier("test_quiver"));
	public static Item TEST_QUIVER = registerItem(TEST_QUIVER_KEY, new CustomBundleItem(new Item.Settings()
					.registryKey(TEST_QUIVER_KEY)
					.maxCount(1)
					.component(BundleAPIDataComponentTypes.CUSTOM_BUNDLE_CONTENTS_COMPONENT, CustomBundleContentsComponent.builder().size_multiplier(3).tag(Optional.ofNullable(TEST_BUNDLE_TAG)).build())
			),
			List.of(ItemGroups.OPERATOR)
	);

	public static Identifier identifier(String path) {
		return Identifier.of(MOD_ID, path);
	}
}