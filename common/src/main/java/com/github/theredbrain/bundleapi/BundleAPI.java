package com.github.theredbrain.bundleapi;

import com.github.theredbrain.bundleapi.component.type.CustomBundleContentsComponent;
import com.github.theredbrain.bundleapi.item.CustomBundleItem;
import com.github.theredbrain.bundleapi.registry.BundleAPIComponentPredicateTypes;
import com.github.theredbrain.bundleapi.registry.BundleAPIContainerComponentModifiers;
import com.github.theredbrain.bundleapi.registry.BundleAPIDataComponentTypes;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class BundleAPI {
	public static final String MOD_ID = "bundleapi";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static void init() {
		LOGGER.info("Customized Bundles!");
		BundleAPIComponentPredicateTypes.bootstrap();
		BundleAPIContainerComponentModifiers.bootstrap();
		BundleAPIDataComponentTypes.bootstrap();
	}

//	private static Item registerItem(ResourceKey<Item> key, Item item, List<ResourceKey<CreativeModeTab>> itemGroupList) {
//
//		return Registry.register(BuiltInRegistries.ITEM, key, item);
//	}
//
//	public static final TagKey<Item> TEST_BUNDLE_TAG = TagKey.create(Registries.ITEM, identifier("test_bundle_tag"));
//
//	public static ResourceKey<Item> TEST_BUNDLE_KEY = ResourceKey.create(Registries.ITEM, identifier("test_bundle"));
//	public static Item TEST_BUNDLE = registerItem(TEST_BUNDLE_KEY, new CustomBundleItem(new Item.Properties()
//					.setId(TEST_BUNDLE_KEY)
//					.stacksTo(1)
//					.component(BundleAPIDataComponentTypes.CUSTOM_BUNDLE_CONTENTS_COMPONENT, CustomBundleContentsComponent.builder().size_multiplier(2).toImmutable())
//			),
//			List.of()
//	);
//
//	public static ResourceKey<Item> TEST_QUIVER_KEY = ResourceKey.create(Registries.ITEM, identifier("test_quiver"));
//	public static Item TEST_QUIVER = registerItem(TEST_QUIVER_KEY, new CustomBundleItem(new Item.Properties()
//					.setId(TEST_QUIVER_KEY)
//					.stacksTo(1)
//					.component(BundleAPIDataComponentTypes.CUSTOM_BUNDLE_CONTENTS_COMPONENT, CustomBundleContentsComponent.builder().size_multiplier(3).tag(Optional.ofNullable(TEST_BUNDLE_TAG)).toImmutable())
//			),
//			List.of()
//	);

	public static Identifier identifier(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}