package com.github.theredbrain.bundleapi.registry;

import com.github.theredbrain.bundleapi.predicate.item.CustomBundleContentsPredicate;
import net.minecraft.core.Registry;
import net.minecraft.core.component.predicates.DataComponentPredicate;
import net.minecraft.core.registries.BuiltInRegistries;

public class BundleAPIComponentPredicateTypes {
	public static DataComponentPredicate.Type<CustomBundleContentsPredicate> CUSTOM_BUNDLE_CONTENTS_ITEM_SUB_PREDICATE = Registry.register(
			BuiltInRegistries.DATA_COMPONENT_PREDICATE_TYPE,
			"custom_bundle_contents",
			new DataComponentPredicate.ConcreteType<>(CustomBundleContentsPredicate.CODEC)
	);

	public static void bootstrap() {
	}
}
