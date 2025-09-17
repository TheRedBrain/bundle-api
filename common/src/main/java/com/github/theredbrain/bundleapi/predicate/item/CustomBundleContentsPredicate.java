package com.github.theredbrain.bundleapi.predicate.item;

import com.github.theredbrain.bundleapi.BundleAPI;
import com.github.theredbrain.bundleapi.component.type.CustomBundleContentsComponent;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.component.ComponentType;
import net.minecraft.item.ItemStack;
import net.minecraft.predicate.collection.CollectionPredicate;
import net.minecraft.predicate.item.ComponentSubPredicate;
import net.minecraft.predicate.item.ItemPredicate;

import java.util.Optional;

public record CustomBundleContentsPredicate(
		Optional<CollectionPredicate<ItemStack, ItemPredicate>> items) implements ComponentSubPredicate<CustomBundleContentsComponent> {
	public static final Codec<CustomBundleContentsPredicate> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(CollectionPredicate.createCodec(ItemPredicate.CODEC).optionalFieldOf("items").forGetter(CustomBundleContentsPredicate::items))
					.apply(instance, CustomBundleContentsPredicate::new)
	);

	@Override
	public ComponentType<CustomBundleContentsComponent> getComponentType() {
		return BundleAPI.CUSTOM_BUNDLE_CONTENTS_COMPONENT;
	}

	public boolean test(ItemStack itemStack, CustomBundleContentsComponent customBundleContentsComponent) {
		return !this.items.isPresent() || ((CollectionPredicate) this.items.get()).test(customBundleContentsComponent.iterate());
	}
}
