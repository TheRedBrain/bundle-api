package com.github.theredbrain.bundleapi.registry;

import com.github.theredbrain.bundleapi.component.type.CustomBundleContentsComponent;
import net.minecraft.component.ComponentType;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.ContainerComponentModifier;
import net.minecraft.loot.ContainerComponentModifiers;

import java.util.stream.Stream;

public class BundleAPIContainerComponentModifiers {
	public static ContainerComponentModifier<CustomBundleContentsComponent> CUSTOM_BUNDLE_CONTENTS_CONTAINER_COMPONENT_MODIFIER;

	public static void bootstrap() {
	}

	static {
		CUSTOM_BUNDLE_CONTENTS_CONTAINER_COMPONENT_MODIFIER = new ContainerComponentModifier<CustomBundleContentsComponent>() {
			@Override
			public ComponentType<CustomBundleContentsComponent> getComponentType() {
				return BundleAPIDataComponentTypes.CUSTOM_BUNDLE_CONTENTS_COMPONENT;
			}

			public CustomBundleContentsComponent getDefault() {
				return CustomBundleContentsComponent.DEFAULT;
			}

			public Stream<ItemStack> stream(CustomBundleContentsComponent customBundleContentsComponent) {
				return customBundleContentsComponent.stream();
			}

			public CustomBundleContentsComponent apply(CustomBundleContentsComponent customBundleContentsComponent, Stream<ItemStack> stream) {
				CustomBundleContentsComponent.Builder builder = new CustomBundleContentsComponent.Builder(customBundleContentsComponent).clear();
				stream.forEach(builder::add);
				return builder.build();
			}
		};
		ContainerComponentModifiers.TYPE_TO_MODIFIER.put(BundleAPIDataComponentTypes.CUSTOM_BUNDLE_CONTENTS_COMPONENT, CUSTOM_BUNDLE_CONTENTS_CONTAINER_COMPONENT_MODIFIER);
	}
}
