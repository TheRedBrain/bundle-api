package com.github.theredbrain.bundleapi;

import com.github.theredbrain.bundleapi.component.type.CustomBundleContentsComponent;
import com.github.theredbrain.bundleapi.predicate.item.CustomBundleContentsPredicate;
import net.fabricmc.api.ModInitializer;
import net.minecraft.component.ComponentType;
import net.minecraft.predicate.item.ItemSubPredicate;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BundleAPI implements ModInitializer {
	public static final String MOD_ID = "bundleapi";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static ComponentType<CustomBundleContentsComponent> CUSTOM_BUNDLE_CONTENTS_COMPONENT;
	public static ItemSubPredicate.Type<CustomBundleContentsPredicate> CUSTOM_BUNDLE_CONTENTS_ITEM_SUB_PREDICATE;

	@Override
	public void onInitialize() {
		LOGGER.info("Customized Bundles!");
	}

	static {
		CUSTOM_BUNDLE_CONTENTS_COMPONENT = Registry.register(
				Registries.DATA_COMPONENT_TYPE,
				identifier("custom_bundle_contents"),
				ComponentType.<CustomBundleContentsComponent>builder().codec(CustomBundleContentsComponent.CODEC).packetCodec(CustomBundleContentsComponent.PACKET_CODEC).build()
		);
		CUSTOM_BUNDLE_CONTENTS_ITEM_SUB_PREDICATE = Registry.register(
				Registries.ITEM_SUB_PREDICATE_TYPE,
				"custom_bundle_contents",
				new ItemSubPredicate.Type<>(CustomBundleContentsPredicate.CODEC)
		);
	}

	public static Identifier identifier(String path) {
		return Identifier.of(MOD_ID, path);
	}
}