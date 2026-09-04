package com.github.theredbrain.bundleapi.registry;

import com.github.theredbrain.bundleapi.component.type.CustomBundleContentsComponent;
import java.util.stream.Stream;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.ContainerComponentManipulator;
import net.minecraft.world.level.storage.loot.ContainerComponentManipulators;

public class BundleAPIContainerComponentModifiers {
	public static ContainerComponentManipulator<CustomBundleContentsComponent> CUSTOM_BUNDLE_CONTENTS_CONTAINER_COMPONENT_MODIFIER;

	public static void bootstrap() {
	}

	static {
		CUSTOM_BUNDLE_CONTENTS_CONTAINER_COMPONENT_MODIFIER = new ContainerComponentManipulator<CustomBundleContentsComponent>() {
			@Override
			public DataComponentType<CustomBundleContentsComponent> type() {
				return BundleAPIDataComponentTypes.CUSTOM_BUNDLE_CONTENTS_COMPONENT;
			}

			public CustomBundleContentsComponent empty() {
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
		ContainerComponentManipulators.ALL_MANIPULATORS.put(BundleAPIDataComponentTypes.CUSTOM_BUNDLE_CONTENTS_COMPONENT, CUSTOM_BUNDLE_CONTENTS_CONTAINER_COMPONENT_MODIFIER);
	}
}
