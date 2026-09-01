package com.github.theredbrain.bundleapi;

import com.github.theredbrain.bundleapi.registry.BundleAPIComponentPredicateTypes;
import com.github.theredbrain.bundleapi.registry.BundleAPIContainerComponentModifiers;
import com.github.theredbrain.bundleapi.registry.BundleAPIDataComponentTypes;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BundleAPI {
	public static final String MOD_ID = "bundleapi";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static void init() {
		LOGGER.info("Customized Bundles!");
		BundleAPIComponentPredicateTypes.bootstrap();
		BundleAPIContainerComponentModifiers.bootstrap();
		BundleAPIDataComponentTypes.bootstrap();
	}

	public static Identifier identifier(String path) {
		return Identifier.of(MOD_ID, path);
	}
}