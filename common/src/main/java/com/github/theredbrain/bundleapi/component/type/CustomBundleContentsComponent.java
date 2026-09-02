package com.github.theredbrain.bundleapi.component.type;

import com.github.theredbrain.bundleapi.registry.BundleAPIDataComponentTypes;
import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.apache.commons.lang3.math.Fraction;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Bees;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;

public final class CustomBundleContentsComponent implements TooltipComponent {
	public static final CustomBundleContentsComponent DEFAULT = new CustomBundleContentsComponent(List.of(), Optional.empty(), 1, true);
	public static final Codec<CustomBundleContentsComponent> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
							Content.CODEC.fieldOf("content").forGetter(component -> component.content),
							TagKey.hashedCodec(Registries.ITEM).optionalFieldOf("tag").forGetter(component -> component.tag),
							Codec.INT.optionalFieldOf("size_multiplier", 1).forGetter(component -> component.size_multiplier),
							Codec.BOOL.optionalFieldOf("enable_slot_selection", true).forGetter(component -> component.enable_slot_selection)/*,
							SoundEvent.ENTRY_CODEC.optionalFieldOf("drop_contents_sound").forGetter(component -> component.drop_contents_sound),
							SoundEvent.ENTRY_CODEC.optionalFieldOf("insert_sound").forGetter(component -> component.insert_sound),
							SoundEvent.ENTRY_CODEC.optionalFieldOf("insert_fail_sound").forGetter(component -> component.insert_fail_sound),
							SoundEvent.ENTRY_CODEC.optionalFieldOf("remove_one_sound").forGetter(component -> component.remove_one_sound)*/
					)
					.apply(instance, CustomBundleContentsComponent::new)
	);
	public static final StreamCodec<RegistryFriendlyByteBuf, CustomBundleContentsComponent> PACKET_CODEC = StreamCodec.composite(
			Content.PACKET_CODEC,
			component -> component.content,
			TagKey.streamCodec(Registries.ITEM).apply(ByteBufCodecs::optional),
			component -> component.tag,
			ByteBufCodecs.VAR_INT,
			component -> component.size_multiplier,
			ByteBufCodecs.BOOL,
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
	final int selectedItem;
		/*final Optional<RegistryEntry<SoundEvent>> drop_contents_sound;
		final Optional<RegistryEntry<SoundEvent>> insert_sound;
		final Optional<RegistryEntry<SoundEvent>> insert_fail_sound;
		final Optional<RegistryEntry<SoundEvent>> remove_one_sound;*/

	public CustomBundleContentsComponent(
			Content content,
			Fraction occupancy,
			int selectedItem,
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
		this.selectedItem = selectedItem;
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
				computeContentWeight(stacks, size_multiplier),
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
				computeContentWeight(content.stacks, size_multiplier),
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

	public static Mutable builder() {
		return new Mutable(DEFAULT);
	}

	private static Fraction computeContentWeight(List<ItemStack> stacks, int size_multiplier) {
		Fraction fraction = Fraction.ZERO;

		for (ItemStack itemStack : stacks) {
			fraction = fraction.add(getWeight(itemStack, size_multiplier).multiplyBy(Fraction.getFraction(itemStack.getCount(), 1)));
		}

		return fraction;
	}

	static Fraction getWeight(ItemStack stack, int size_multiplier) {
		CustomBundleContentsComponent customBundleContentsComponent = stack.get(BundleAPIDataComponentTypes.CUSTOM_BUNDLE_CONTENTS_COMPONENT);
		if (customBundleContentsComponent != null) {
			return NESTED_BUNDLE_OCCUPANCY.add(customBundleContentsComponent.weight());
		} else {
			List<BeehiveBlockEntity.Occupant> list = stack.getOrDefault(DataComponents.BEES, Bees.EMPTY).bees();
			return !list.isEmpty() ? Fraction.ONE : Fraction.getFraction(1, stack.getMaxStackSize() * size_multiplier);
		}
	}


	public static boolean canItemBeInBundle(Optional<TagKey<Item>> tag, ItemStack stack) {
		return !stack.isEmpty() && stack.getItem().canFitInsideContainerItems() && (tag.isEmpty() || stack.is(tag.get()));
	}

	public int getNumberOfItemsToShow() {
		int i = this.size();
		int j = i > 12 ? 11 : 12;
		int k = i % 4;
		int l = k == 0 ? 0 : 4 - k;
		return Math.min(i, j - l);
	}

	public ItemStack getItemUnsafe(int index) {
		return (ItemStack) this.content.stacks.get(index);
	}

	public Stream<ItemStack> itemCopyStream() {
		return this.content.stacks.stream().map(ItemStack::copy);
	}

	public Iterable<ItemStack> items() {
		return this.content.stacks;
	}

	public Iterable<ItemStack> itemsCopy() {
		return Lists.<ItemStack, ItemStack>transform(this.content.stacks, ItemStack::copy);
	}

	public int size() {
		return this.content.stacks.size();
	}

	public int sizeMultiplier() {
		return this.size_multiplier;
	}

	public Fraction weight() {
		return this.occupancy;
	}

	public boolean isEmpty() {
		return this.content.stacks.isEmpty();
	}

	public int getSelectedItem() {
		return this.selectedItem;
	}

	public boolean hasSelectedItem() {
		return this.selectedItem != -1;
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}
		if (!(object instanceof CustomBundleContentsComponent customBundleContentsComponent)) {
			return false;
		}
		return this.occupancy.equals(customBundleContentsComponent.occupancy) && ItemStack.listMatches(this.content.stacks, customBundleContentsComponent.content.stacks);
	}

	@Override
	public int hashCode() {
		return ItemStack.hashStackList(this.content.stacks);
	}

	@Override
	public String toString() {
		return "CustomBundleContents" + String.valueOf(this.content.stacks);
	}

	public static class Mutable {
		private Content content;
		private Fraction weight;
		private int selectedStackIndex;
		private Optional<TagKey<Item>> tag;
		private int size_multiplier;
		private boolean enable_slot_selection;

		public Mutable(CustomBundleContentsComponent base) {
			this.content = new Content(base.content.stacks);
			this.weight = base.occupancy;
			this.selectedStackIndex = base.selectedItem;
			this.tag = base.tag;
			this.size_multiplier = base.size_multiplier;
			this.enable_slot_selection = base.enable_slot_selection;
		}

		public Mutable clearItems() {
			this.content.stacks.clear();
			this.weight = Fraction.ZERO;
			this.selectedStackIndex = -1;
			return this;
		}

		private int findStackIndex(ItemStack stack) {
			if (!stack.isStackable()) {
				return -1;
			}

			for (int i = 0; i < this.content.stacks.size(); i++) {
				if (ItemStack.isSameItemSameComponents(this.content.stacks.get(i), stack)) {
					return i;
				}
			}

			return -1;
		}

		private int getMaxAmountToAdd(ItemStack stack) {
			Fraction fraction = Fraction.ONE.subtract(this.weight);
			return Math.max(fraction.divideBy(CustomBundleContentsComponent.getWeight(stack, this.size_multiplier)).intValue(), 0);
		}

		public int tryInsert(ItemStack stack) {
			if (!CustomBundleContentsComponent.canItemBeInBundle(this.tag, stack)) {
				return 0;
			}
			int i = Math.min(stack.getCount(), this.getMaxAmountToAdd(stack));
			if (i == 0) {
				return 0;
			}
			this.weight = this.weight.add(CustomBundleContentsComponent.getWeight(stack, this.size_multiplier).multiplyBy(Fraction.getFraction(i, 1)));
			int j = this.findStackIndex(stack);
			if (j != -1) {
				ItemStack itemStack = this.content.stacks.remove(j);
				ItemStack itemStack2 = itemStack.copyWithCount(itemStack.getCount() + i);
				stack.shrink(i);
				this.content.stacks.add(0, itemStack2);
			} else {
				this.content.stacks.add(0, stack.split(i));
			}

			return i;
		}

		public int tryTransfer(Slot slot, Player player) {
			ItemStack itemStack = slot.getItem();
			int i = this.getMaxAmountToAdd(itemStack);
			return this.tryInsert(slot.safeTake(itemStack.getCount(), i, player));
		}

		public void toggleSelectedItem(int i) {
			this.selectedStackIndex = this.selectedStackIndex != i && !this.indexIsOutsideAllowedBounds(i) ? i : -1;
		}

		private boolean indexIsOutsideAllowedBounds(int index) {
			return !this.enable_slot_selection || index < 0 || index >= this.content.stacks.size();
		}

		public @Nullable ItemStack removeSelected() {
			if (this.content.stacks.isEmpty()) {
				return null;
			}

			int i = this.indexIsOutsideAllowedBounds(this.selectedStackIndex) ? 0 : this.selectedStackIndex;
			ItemStack itemStack = this.content.stacks.remove(i).copy();
			this.weight = this.weight.subtract(CustomBundleContentsComponent.getWeight(itemStack, this.size_multiplier).multiplyBy(Fraction.getFraction(itemStack.getCount(), 1)));
			this.toggleSelectedItem(-1);
			return itemStack;
		}

		public Fraction weight() {
			return this.weight;
		}

		public Mutable size_multiplier(int size_multiplier) {
			this.size_multiplier = size_multiplier;
			return this;
		}

		public Mutable tag(Optional<TagKey<Item>> tag) {
			this.tag = tag;
			return this;
		}

		public Mutable enable_slot_selection(boolean enable_slot_selection) {
			this.enable_slot_selection = enable_slot_selection;
			return this;
		}

		public CustomBundleContentsComponent toImmutable() {
			return new CustomBundleContentsComponent(List.copyOf(this.content.stacks), this.weight, this.tag, this.size_multiplier, this.enable_slot_selection);
		}
	}

	public record Content(List<ItemStack> stacks) {
		public static final Content DEFAULT = new Content(List.of());
		public static final Codec<Content> CODEC = ItemStack.CODEC.listOf().xmap(Content::new, component -> component.stacks);
		public static final StreamCodec<RegistryFriendlyByteBuf, Content> PACKET_CODEC = ItemStack.STREAM_CODEC
				.apply(ByteBufCodecs.list())
				.map(Content::new, content -> content.stacks);

		public Content(List<ItemStack> stacks) {
			this.stacks = new ArrayList<ItemStack>(stacks);
		}
	}
}
