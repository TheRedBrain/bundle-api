package com.github.theredbrain.bundleapi.registry;

import com.github.theredbrain.bundleapi.BundleAPI;
import com.github.theredbrain.bundleapi.component.type.CustomBundleContentsComponent;
import net.minecraft.component.ComponentType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class BundleAPIDataComponentTypes {
	public static ComponentType<CustomBundleContentsComponent> CUSTOM_BUNDLE_CONTENTS_COMPONENT = Registry.register(
			Registries.DATA_COMPONENT_TYPE,
			BundleAPI.identifier("custom_bundle_contents"),
			ComponentType.<CustomBundleContentsComponent>builder().codec(CustomBundleContentsComponent.CODEC).packetCodec(CustomBundleContentsComponent.PACKET_CODEC).cache().build()
	);

	public static void bootstrap() {
	}
}
