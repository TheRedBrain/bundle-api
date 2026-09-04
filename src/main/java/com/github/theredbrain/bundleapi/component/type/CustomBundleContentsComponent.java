package com.github.theredbrain.bundleapi.component.type;

import com.github.theredbrain.bundleapi.registry.BundleAPIDataComponentTypes;
import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.BeesComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipData;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.screen.slot.Slot;
import org.apache.commons.lang3.math.Fraction;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public final class CustomBundleContentsComponent implements TooltipData {
	public static final CustomBundleContentsComponent DEFAULT = new CustomBundleContentsComponent(List.of(), Optional.empty(), 1, true);
	public static final Codec<CustomBundleContentsComponent> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
							Content.CODEC.fieldOf("content").forGetter(component -> component.content),
							TagKey.codec(RegistryKeys.ITEM).optionalFieldOf("tag").forGetter(component -> component.tag),
							Codec.INT.optionalFieldOf("size_multiplier", 1).forGetter(component -> component.size_multiplier),
							Codec.BOOL.optionalFieldOf("enable_slot_selection", true).forGetter(component -> component.enable_slot_selection)/*,
							SoundEvent.ENTRY_CODEC.optionalFieldOf("drop_contents_sound").forGetter(component -> component.drop_contents_sound),
							SoundEvent.ENTRY_CODEC.optionalFieldOf("insert_sound").forGetter(component -> component.insert_sound),
							SoundEvent.ENTRY_CODEC.optionalFieldOf("insert_fail_sound").forGetter(component -> component.insert_fail_sound),
							SoundEvent.ENTRY_CODEC.optionalFieldOf("remove_one_sound").forGetter(component -> component.remove_one_sound)*/
					)
					.apply(instance, CustomBundleContentsComponent::new)
	);
	public static final PacketCodec<RegistryByteBuf, CustomBundleContentsComponent> PACKET_CODEC = PacketCodec.tuple(
			Content.PACKET_CODEC,
			component -> component.content,
			TagKey.packetCodec(RegistryKeys.ITEM).collect(PacketCodecs::optional),
			component -> component.tag,
			PacketCodecs.VAR_INT,
			component -> component.size_multiplier,
			PacketCodecs.BOOLEAN,
			component -> component.enable_slot_selection,
			/*SoundEvent.ENTRY_PACKET_CODEC.collect(PacketCodecs::optional),
			component -> component.drop_contents_sound,
			SoundEvent.ENTRY_PACKET_CODEC.collect(PacketCodecs::optional),
			component -> component.insert_sound,
			SoundEvent.ENTRY_PACKET_CODEC.collect(PacketCodecs::optional),
			component -> component.insert_fail_sound,
			SoundEvent.ENTRY_PACKET_CODEC.collect(PacketCodecs::optional),
			component -> component.remove_one_sound,*/
			CustomBundleContentsComponent::new
	);
	private static final Fraction NESTED_BUNDLE_OCCUPANCY = Fraction.getFraction(1, 16);

	final Content content;
	final Fraction occupancy;
	final Optional<TagKey<Item>> tag;
	final int size_multiplier;
	final boolean enable_slot_selection;
	final int selectedStackIndex;
		/*final Optional<RegistryEntry<SoundEvent>> drop_contents_sound;
		final Optional<RegistryEntry<SoundEvent>> insert_sound;
		final Optional<RegistryEntry<SoundEvent>> insert_fail_sound;
		final Optional<RegistryEntry<SoundEvent>> remove_one_sound;*/

	public CustomBundleContentsComponent(
			Content content,
			Fraction occupancy,
			int selectedStackIndex,
			Optional<TagKey<Item>> tag,
			int size_multiplier,
			boolean enable_slot_selection/*,
			Optional<RegistryEntry<SoundEvent>> drop_contents_sound,
			Optional<RegistryEntry<SoundEvent>> insert_sound,
			Optional<RegistryEntry<SoundEvent>> insert_fail_sound,
			Optional<RegistryEntry<SoundEvent>> remove_one_sound*/
	) {
		this.content = content;
		this.occupancy = occupancy;
		this.selectedStackIndex = selectedStackIndex;
		this.tag = tag;
		this.size_multiplier = size_multiplier;
		this.enable_slot_selection = enable_slot_selection;
		/*this.drop_contents_sound = drop_contents_sound;
		this.insert_sound = insert_sound;
		this.insert_fail_sound = insert_fail_sound;
		this.remove_one_sound = remove_one_sound;*/
	}

	CustomBundleContentsComponent(
			List<ItemStack> stacks,
			Fraction occupancy,
			Optional<TagKey<Item>> tag,
			int size_multiplier,
			boolean enable_slot_selection/*,
			Optional<RegistryEntry<SoundEvent>> drop_contents_sound,
			Optional<RegistryEntry<SoundEvent>> insert_sound,
			Optional<RegistryEntry<SoundEvent>> insert_fail_sound,
			Optional<RegistryEntry<SoundEvent>> remove_one_sound*/
	) {
		this(
				new Content(stacks),
				occupancy,
				-1,
				tag,
				size_multiplier,
				enable_slot_selection/*,
				drop_contents_sound,
				insert_sound,
				insert_fail_sound,
				remove_one_sound*/
		);
	}

	CustomBundleContentsComponent(
			List<ItemStack> stacks,
			Optional<TagKey<Item>> tag,
			int size_multiplier,
			boolean enable_slot_selection/*,
			Optional<RegistryEntry<SoundEvent>> drop_contents_sound,
			Optional<RegistryEntry<SoundEvent>> insert_sound,
			Optional<RegistryEntry<SoundEvent>> insert_fail_sound,
			Optional<RegistryEntry<SoundEvent>> remove_one_sound*/
	) {
		this(
				new Content(stacks),
				calculateOccupancy(stacks, size_multiplier),
				-1,
				tag,
				size_multiplier,
				enable_slot_selection/*,
				drop_contents_sound,
				insert_sound,
				insert_fail_sound,
				remove_one_sound*/
		);
	}

	public CustomBundleContentsComponent(
			Content content,
			Optional<TagKey<Item>> tag,
			int size_multiplier,
			boolean enable_slot_selection/*,
			Optional<RegistryEntry<SoundEvent>> drop_contents_sound,
			Optional<RegistryEntry<SoundEvent>> insert_sound,
			Optional<RegistryEntry<SoundEvent>> insert_fail_sound,
			Optional<RegistryEntry<SoundEvent>> remove_one_sound*/
	) {
		this(
				content,
				calculateOccupancy(content.stacks, size_multiplier),
				-1,
				tag,
				size_multiplier,
				enable_slot_selection/*,
				drop_contents_sound,
				insert_sound,
				insert_fail_sound,
				remove_one_sound*/
		);
	}

	public CustomBundleContentsComponent(Optional<TagKey<Item>> tag, int size_multiplier, boolean enable_slot_selection) {
		this(
				Content.DEFAULT,
				tag,
				size_multiplier,
				enable_slot_selection/*,
				Optional.empty(),
				Optional.empty(),
				Optional.empty(),
				Optional.empty()*/
		);
	}

	public CustomBundleContentsComponent(Optional<TagKey<Item>> tag, int size_multiplier) {
		this(
				tag,
				size_multiplier,
				true
		);
	}

	public static Builder builder() {
		return new Builder(DEFAULT);
	}

	private static Fraction calculateOccupancy(List<ItemStack> stacks, int size_multiplier) {
		Fraction fraction = Fraction.ZERO;

		for (ItemStack itemStack : stacks) {
			fraction = fraction.add(getOccupancy(itemStack, size_multiplier).multiplyBy(Fraction.getFraction(itemStack.getCount(), 1)));
		}

		return fraction;
	}

	static Fraction getOccupancy(ItemStack stack, int size_multiplier) {
		CustomBundleContentsComponent customBundleContentsComponent = stack.get(BundleAPIDataComponentTypes.CUSTOM_BUNDLE_CONTENTS_COMPONENT);
		if (customBundleContentsComponent != null) {
			return NESTED_BUNDLE_OCCUPANCY.add(customBundleContentsComponent.getOccupancy());
		} else {
			List<BeehiveBlockEntity.BeeData> list = stack.getOrDefault(DataComponentTypes.BEES, BeesComponent.DEFAULT).bees();
			return !list.isEmpty() ? Fraction.ONE : Fraction.getFraction(1, stack.getMaxCount() * size_multiplier);
		}
	}


	public static boolean canBeBundled(Optional<TagKey<Item>> tag, ItemStack stack) {
		return !stack.isEmpty() && stack.getItem().canBeNested() && (tag.isEmpty() || stack.isIn(tag.get()));
	}

	public int getNumberOfStacksShown() {
		int i = this.size();
		int j = i > 12 ? 11 : 12;
		int k = i % 4;
		int l = k == 0 ? 0 : 4 - k;
		return Math.min(i, j - l);
	}

	public ItemStack get(int index) {
		return (ItemStack) this.content.stacks.get(index);
	}

	public Stream<ItemStack> stream() {
		return this.content.stacks.stream().map(ItemStack::copy);
	}

	public Iterable<ItemStack> iterate() {
		return this.content.stacks;
	}

	public Iterable<ItemStack> iterateCopy() {
		return Lists.<ItemStack, ItemStack>transform(this.content.stacks, ItemStack::copy);
	}

	public int size() {
		return this.content.stacks.size();
	}

	public int sizeMultiplier() {
		return this.size_multiplier;
	}

	public Fraction getOccupancy() {
		return this.occupancy;
	}

	public boolean isEmpty() {
		return this.content.stacks.isEmpty();
	}

	public int getSelectedStackIndex() {
		return this.selectedStackIndex;
	}

	public boolean hasSelectedStack() {
		return this.selectedStackIndex != -1;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof CustomBundleContentsComponent customBundleContentsComponent)) {
			return false;
		}
		return this.occupancy.equals(customBundleContentsComponent.occupancy) && ItemStack.stacksEqual(this.content.stacks, customBundleContentsComponent.content.stacks);
	}

	public static class Builder {
		private Content content;
		private Fraction occupancy;
		private int selectedStackIndex;
		private Optional<TagKey<Item>> tag;
		private int size_multiplier;
		private boolean enable_slot_selection;

		public Builder(CustomBundleContentsComponent base) {
			this.content = new Content(base.content.stacks);
			this.occupancy = base.occupancy;
			this.selectedStackIndex = base.selectedStackIndex;
			this.tag = base.tag;
			this.size_multiplier = base.size_multiplier;
			this.enable_slot_selection = base.enable_slot_selection;
		}

		public Builder clear() {
			this.content.stacks.clear();
			this.occupancy = Fraction.ZERO;
			this.selectedStackIndex = -1;
			return this;
		}

		private int getInsertionIndex(ItemStack stack) {
			if (!stack.isStackable()) {
				return -1;
			}

			for (int i = 0; i < this.content.stacks.size(); i++) {
				if (ItemStack.areItemsAndComponentsEqual(this.content.stacks.get(i), stack)) {
					return i;
				}
			}

			return -1;
		}

//		private int addInternal(ItemStack stack) {
//			if (!stack.isStackable()) {
//				return -1;
//			} else {
//				for (int i = 0; i < this.content.stacks.size(); i++) {
//					if (ItemStack.areItemsAndComponentsEqual((ItemStack) this.content.stacks.get(i), stack) && this.content.stacks.get(i).getCount() < this.content.stacks.get(i).getMaxCount()) {
//						return i;
//					}
//				}
//
//				return -1;
//			}
//		}

		private int getMaxAllowed(ItemStack stack) {
			Fraction fraction = Fraction.ONE.subtract(this.occupancy);
			return Math.max(fraction.divideBy(CustomBundleContentsComponent.getOccupancy(stack, this.size_multiplier)).intValue(), 0);
		}

		public int add(ItemStack stack) {
			if (!CustomBundleContentsComponent.canBeBundled(this.tag, stack)) {
				return 0;
			}
			int i = Math.min(stack.getCount(), this.getMaxAllowed(stack));
			if (i == 0) {
				return 0;
			}
			this.occupancy = this.occupancy.add(CustomBundleContentsComponent.getOccupancy(stack, this.size_multiplier).multiplyBy(Fraction.getFraction(i, 1)));
			int j = this.getInsertionIndex(stack);
			if (j != -1) {
				ItemStack itemStack = (ItemStack) this.content.stacks.remove(j);
				ItemStack itemStack2 = itemStack.copyWithCount(itemStack.getCount() + i);
				stack.decrement(i);
				this.content.stacks.add(0, itemStack2);
			} else {
				this.content.stacks.add(0, stack.split(i));
			}

			return i;
		}

		public int add(Slot slot, PlayerEntity player) {
			ItemStack itemStack = slot.getStack();
			int i = this.getMaxAllowed(itemStack);
			return this.add(slot.takeStackRange(itemStack.getCount(), i, player));
		}

		public void setSelectedStackIndex(int selectedStackIndex) {
			this.selectedStackIndex = this.selectedStackIndex != selectedStackIndex && !this.isOutOfBounds(selectedStackIndex) ? selectedStackIndex : -1;
		}

		private boolean isOutOfBounds(int index) {
			return !this.enable_slot_selection || index < 0 || index >= this.content.stacks.size();
		}

		public @Nullable ItemStack removeSelected() {
			if (this.content.stacks.isEmpty()) {
				return null;
			}

			int i = this.isOutOfBounds(this.selectedStackIndex) ? 0 : this.selectedStackIndex;
			ItemStack itemStack = this.content.stacks.remove(i).copy();
			this.occupancy = this.occupancy.subtract(CustomBundleContentsComponent.getOccupancy(itemStack, this.size_multiplier).multiplyBy(Fraction.getFraction(itemStack.getCount(), 1)));
			this.setSelectedStackIndex(-1);
			return itemStack;
		}

		public Fraction getOccupancy() {
			return this.occupancy;
		}

		public Builder size_multiplier(int size_multiplier) {
			this.size_multiplier = size_multiplier;
			return this;
		}

		public Builder tag(Optional<TagKey<Item>> tag) {
			this.tag = tag;
			return this;
		}

		public Builder enable_slot_selection(boolean enable_slot_selection) {
			this.enable_slot_selection = enable_slot_selection;
			return this;
		}

		public CustomBundleContentsComponent build() {
			return new CustomBundleContentsComponent(List.copyOf(this.content.stacks), this.occupancy, this.tag, this.size_multiplier, this.enable_slot_selection);
		}
	}

	public record Content(List<ItemStack> stacks) {
		public static final Content DEFAULT = new Content(List.of());
		public static final Codec<Content> CODEC = ItemStack.CODEC.listOf().xmap(Content::new, component -> component.stacks);
		public static final PacketCodec<RegistryByteBuf, Content> PACKET_CODEC = ItemStack.PACKET_CODEC
				.collect(PacketCodecs.toList())
				.xmap(Content::new, content -> content.stacks);

		public Content(List<ItemStack> stacks) {
			this.stacks = new ArrayList<ItemStack>(stacks);
		}
	}
}
