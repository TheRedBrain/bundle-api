package com.github.theredbrain.bundleapi;

import com.github.theredbrain.bundleapi.registry.BundleAPIComponentPredicateTypes;
import com.github.theredbrain.bundleapi.registry.BundleAPIContainerComponentModifiers;
import com.github.theredbrain.bundleapi.registry.BundleAPIDataComponentTypes;
import dev.yumi.mc.core.api.ModContainer;
import dev.yumi.mc.core.api.entrypoint.ModInitializer;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BundleAPI implements ModInitializer {
	public static final String MOD_ID = "bundleapi";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static void init() {
	}

	public static Identifier identifier(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}

	@Override
	public void onInitialize(ModContainer modContainer) {
		LOGGER.info("Customized Bundles!");
		BundleAPIComponentPredicateTypes.bootstrap();
		BundleAPIContainerComponentModifiers.bootstrap();
		BundleAPIDataComponentTypes.bootstrap();
	}
}