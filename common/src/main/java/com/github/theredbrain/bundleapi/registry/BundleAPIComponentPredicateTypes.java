package com.github.theredbrain.bundleapi.registry;

import com.github.theredbrain.bundleapi.predicate.item.CustomBundleContentsPredicate;
import net.minecraft.predicate.component.ComponentPredicate;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class BundleAPIComponentPredicateTypes {
	public static ComponentPredicate.Type<CustomBundleContentsPredicate> CUSTOM_BUNDLE_CONTENTS_ITEM_SUB_PREDICATE = Registry.register(
			Registries.DATA_COMPONENT_PREDICATE_TYPE,
			"custom_bundle_contents",
			new ComponentPredicate.OfValue<>(CustomBundleContentsPredicate.CODEC)
	);

	public static void bootstrap() {
	}
}
