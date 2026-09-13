package com.github.theredbrain.bundleapi;

import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BundleAPI {
	public static final String MOD_ID = "bundleapi";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static void init() {
		LOGGER.info("Customized Bundles!");
	}

	public static Identifier identifier(String path) {
		return new Identifier(MOD_ID, path);
	}
}
