package com.github.theredbrain.bundleapi;

import com.github.theredbrain.bundleapi.component.type.CustomBundleContentsComponent;
import com.github.theredbrain.bundleapi.predicate.item.CustomBundleContentsPredicate;
import net.fabricmc.api.ModInitializer;
import net.minecraft.component.ComponentType;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.ContainerComponentModifier;
import net.minecraft.loot.ContainerComponentModifiers;
import net.minecraft.predicate.item.ItemSubPredicate;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.Stream;

public class BundleAPI implements ModInitializer {
	public static final String MOD_ID = "bundleapi";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static ComponentType<CustomBundleContentsComponent> CUSTOM_BUNDLE_CONTENTS_COMPONENT;
	public static ItemSubPredicate.Type<CustomBundleContentsPredicate> CUSTOM_BUNDLE_CONTENTS_ITEM_SUB_PREDICATE;
	public static ContainerComponentModifier<CustomBundleContentsComponent> CUSTOM_BUNDLE_CONTENTS_CONTAINER_COMPONENT_MODIFIER;

	@Override
	public void onInitialize() {
		LOGGER.info("Customized Bundles!");
	}

	static {
		CUSTOM_BUNDLE_CONTENTS_COMPONENT = Registry.register(
				Registries.DATA_COMPONENT_TYPE,
				identifier("custom_bundle_contents"),
				ComponentType.<CustomBundleContentsComponent>builder().codec(CustomBundleContentsComponent.CODEC).packetCodec(CustomBundleContentsComponent.PACKET_CODEC).cache().build()
		);
		CUSTOM_BUNDLE_CONTENTS_ITEM_SUB_PREDICATE = Registry.register(
				Registries.ITEM_SUB_PREDICATE_TYPE,
				"custom_bundle_contents",
				new ItemSubPredicate.Type<>(CustomBundleContentsPredicate.CODEC)
		);

		CUSTOM_BUNDLE_CONTENTS_CONTAINER_COMPONENT_MODIFIER = new ContainerComponentModifier<CustomBundleContentsComponent>() {
			@Override
			public ComponentType<CustomBundleContentsComponent> getComponentType() {
				return CUSTOM_BUNDLE_CONTENTS_COMPONENT;
			}

			public CustomBundleContentsComponent getDefault() {
				return CustomBundleContentsComponent.DEFAULT;
			}

			public Stream<ItemStack> stream(CustomBundleContentsComponent customBundleContentsComponent) {
				return customBundleContentsComponent.stream();
			}

			public CustomBundleContentsComponent create(CustomBundleContentsComponent customBundleContentsComponent, Stream<ItemStack> stream) {
				CustomBundleContentsComponent.Builder builder = new CustomBundleContentsComponent.Builder(customBundleContentsComponent).clear();
				stream.forEach(builder::add);
				return builder.build();
			}
		};
		ContainerComponentModifiers.TYPE_TO_MODIFIER.put(CUSTOM_BUNDLE_CONTENTS_COMPONENT, CUSTOM_BUNDLE_CONTENTS_CONTAINER_COMPONENT_MODIFIER);
	}

	public static Identifier identifier(String path) {
		return Identifier.of(MOD_ID, path);
	}
}