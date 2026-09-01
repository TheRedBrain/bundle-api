package com.github.theredbrain.bundleapi.registry;

import com.github.theredbrain.bundleapi.BundleAPI;
import com.github.theredbrain.bundleapi.component.type.CustomBundleContentsComponent;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;

public class BundleAPIDataComponentTypes {
	public static DataComponentType<CustomBundleContentsComponent> CUSTOM_BUNDLE_CONTENTS_COMPONENT = Registry.register(
			BuiltInRegistries.DATA_COMPONENT_TYPE,
			BundleAPI.identifier("custom_bundle_contents"),
			DataComponentType.<CustomBundleContentsComponent>builder().persistent(CustomBundleContentsComponent.CODEC).networkSynchronized(CustomBundleContentsComponent.PACKET_CODEC).cacheEncoding().build()
	);

	public static void bootstrap() {
	}
}
