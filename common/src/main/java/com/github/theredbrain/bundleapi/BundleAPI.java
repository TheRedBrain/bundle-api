package com.github.theredbrain.bundleapi;

import com.github.theredbrain.bundleapi.component.type.CustomBundleContentsComponent;
import com.github.theredbrain.bundleapi.predicate.item.CustomBundleContentsPredicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.Stream;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.predicates.DataComponentPredicate;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.ContainerComponentManipulator;
import net.minecraft.world.level.storage.loot.ContainerComponentManipulators;

public class BundleAPI {
	public static final String MOD_ID = "bundleapi";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static DataComponentType<CustomBundleContentsComponent> CUSTOM_BUNDLE_CONTENTS_COMPONENT = DataComponentType.<CustomBundleContentsComponent>builder().persistent(CustomBundleContentsComponent.CODEC).networkSynchronized(CustomBundleContentsComponent.PACKET_CODEC).cacheEncoding().build();
	public static DataComponentPredicate.Type<CustomBundleContentsPredicate> CUSTOM_BUNDLE_CONTENTS_ITEM_SUB_PREDICATE = new DataComponentPredicate.ConcreteType<>(CustomBundleContentsPredicate.CODEC);
	public static ContainerComponentManipulator<CustomBundleContentsComponent> CUSTOM_BUNDLE_CONTENTS_CONTAINER_COMPONENT_MODIFIER;

	public static void init() {
		LOGGER.info("Customized Bundles!");
	}

	static {
		CUSTOM_BUNDLE_CONTENTS_CONTAINER_COMPONENT_MODIFIER = new ContainerComponentManipulator<CustomBundleContentsComponent>() {
			@Override
			public DataComponentType<CustomBundleContentsComponent> type() {
				return CUSTOM_BUNDLE_CONTENTS_COMPONENT;
			}

			@Override
			public CustomBundleContentsComponent empty() {
				return CustomBundleContentsComponent.DEFAULT;
			}

			@Override
			public Stream<ItemStack> getContents(CustomBundleContentsComponent customBundleContentsComponent) {
				return customBundleContentsComponent.stream();
			}

			@Override
			public CustomBundleContentsComponent setContents(CustomBundleContentsComponent customBundleContentsComponent, Stream<ItemStack> stream) {
				CustomBundleContentsComponent.Builder builder = new CustomBundleContentsComponent.Builder(customBundleContentsComponent).clear();
				stream.forEach(builder::add);
				return builder.build();
			}
		};
		ContainerComponentManipulators.ALL_MANIPULATORS.put(CUSTOM_BUNDLE_CONTENTS_COMPONENT, CUSTOM_BUNDLE_CONTENTS_CONTAINER_COMPONENT_MODIFIER);
	}

	public static Identifier identifier(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}