package com.github.theredbrain.bundleapi.predicate.item;

import com.github.theredbrain.bundleapi.BundleAPI;
import com.github.theredbrain.bundleapi.component.type.CustomBundleContentsComponent;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancements.criterion.CollectionPredicate;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.advancements.criterion.SingleComponentItemPredicate;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.ItemStack;

public record CustomBundleContentsPredicate(
		Optional<CollectionPredicate<ItemStack, ItemPredicate>> items) implements SingleComponentItemPredicate<CustomBundleContentsComponent> {
	public static final Codec<CustomBundleContentsPredicate> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(CollectionPredicate.<ItemStack, ItemPredicate>codec(ItemPredicate.CODEC).optionalFieldOf("items").forGetter(CustomBundleContentsPredicate::items))
					.apply(instance, CustomBundleContentsPredicate::new)
	);

	@Override
	public DataComponentType<CustomBundleContentsComponent> componentType() {
		return BundleAPI.CUSTOM_BUNDLE_CONTENTS_COMPONENT;
	}

	@Override
	public boolean matches(CustomBundleContentsComponent customBundleContentsComponent) {
		return !this.items.isPresent() || this.items.get().test(customBundleContentsComponent.iterate());
	}
}
