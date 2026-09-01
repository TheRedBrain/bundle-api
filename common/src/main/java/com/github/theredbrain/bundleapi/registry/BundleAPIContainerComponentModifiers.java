package com.github.theredbrain.bundleapi.registry;

import com.github.theredbrain.bundleapi.component.type.CustomBundleContentsComponent;

import java.util.Objects;
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

			@Override
			public CustomBundleContentsComponent empty() {
				return CustomBundleContentsComponent.DEFAULT;
			}

			@Override
			public Stream<ItemStack> getContents(CustomBundleContentsComponent customBundleContentsComponent) {
				return customBundleContentsComponent.itemCopyStream();
			}

			@Override
			public CustomBundleContentsComponent setContents(CustomBundleContentsComponent customBundleContentsComponent, Stream<ItemStack> stream) {
				CustomBundleContentsComponent.Mutable mutable = new CustomBundleContentsComponent.Mutable(customBundleContentsComponent).clearItems();
				Objects.requireNonNull(mutable);
				stream.forEach(mutable::tryInsert);
				return mutable.toImmutable();
			}
		};
		ContainerComponentManipulators.ALL_MANIPULATORS.put(BundleAPIDataComponentTypes.CUSTOM_BUNDLE_CONTENTS_COMPONENT, CUSTOM_BUNDLE_CONTENTS_CONTAINER_COMPONENT_MODIFIER);
	}
}
