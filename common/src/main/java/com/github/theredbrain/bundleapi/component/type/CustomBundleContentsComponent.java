package com.github.theredbrain.bundleapi.component.type;

import com.github.theredbrain.bundleapi.BundleAPI;
import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.BeesComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipData;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.screen.slot.Slot;
import org.apache.commons.lang3.math.Fraction;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public record CustomBundleContentsComponent(Content content, Fraction occupancy/*, Optional<RegistryEntryList<Item>> tag  TODO add tag key in 1.21.4*/, int size_multiplier) implements TooltipData {
	public static final CustomBundleContentsComponent DEFAULT = new CustomBundleContentsComponent(List.of()/*, Optional.empty()*/, 1);
	public static final Codec<CustomBundleContentsComponent> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
							CustomBundleContentsComponent.Content.CODEC.fieldOf("content").forGetter(component -> component.content),
//							RegistryCodecs.entryList(RegistryKeys.ITEM).optionalFieldOf("tag").forGetter(component -> component.tag),
							Codec.INT.optionalFieldOf("size_multiplier", 1).forGetter(component -> component.size_multiplier)
					)
					.apply(instance, CustomBundleContentsComponent::new)
	);
	public static final PacketCodec<RegistryByteBuf, CustomBundleContentsComponent> PACKET_CODEC = PacketCodec.tuple(
			CustomBundleContentsComponent.Content.PACKET_CODEC,
			component -> component.content,
//			PacketCodecs.optional(PacketCodecs.registryEntryList(RegistryKeys.ITEM)),
//			component -> component.tag,
			PacketCodecs.VAR_INT,
			component -> component.size_multiplier,
			CustomBundleContentsComponent::new
	);
	private static final Fraction NESTED_BUNDLE_OCCUPANCY = Fraction.getFraction(1, 16);

	public CustomBundleContentsComponent(Content content, Fraction occupancy/*, Optional<RegistryEntryList<Item>> tag*/, int size_multiplier) {
		this.content = content;
		this.occupancy = occupancy;
//		this.tag = tag;
		this.size_multiplier = size_multiplier;
	}

	CustomBundleContentsComponent(List<ItemStack> stacks, Fraction occupancy/*, Optional<RegistryEntryList<Item>> tag*/, int size_multiplier) {
		this(new Content(stacks), occupancy/*, tag*/, size_multiplier);
	}

	CustomBundleContentsComponent(List<ItemStack> stacks/*, Optional<RegistryEntryList<Item>> tag*/, int size_multiplier) {
		this(new Content(stacks), calculateOccupancy(stacks, size_multiplier)/*, tag*/, size_multiplier);
	}

	public CustomBundleContentsComponent(Content content/*, Optional<RegistryEntryList<Item>> tag*/, int size_multiplier) {
		this(content, calculateOccupancy(content.stacks, size_multiplier)/*, tag*/, size_multiplier);
	}

	public CustomBundleContentsComponent(/*Optional<RegistryEntryList<Item>> tag, */int size_multiplier) {
		this(Content.DEFAULT/*, calculateOccupancy(List.of(), size_multiplier)*//*, tag*/, size_multiplier);
	}

	public static CustomBundleContentsComponent.Builder builder() {
		return new CustomBundleContentsComponent.Builder(DEFAULT);
	}

	private static Fraction calculateOccupancy(List<ItemStack> stacks, int size_multiplier) {
		Fraction fraction = Fraction.ZERO;

		for (ItemStack itemStack : stacks) {
			fraction = fraction.add(getOccupancy(itemStack, size_multiplier).multiplyBy(Fraction.getFraction(itemStack.getCount(), 1)));
		}

		return fraction;
	}

	static Fraction getOccupancy(ItemStack stack, int size_multiplier) {
		CustomBundleContentsComponent customBundleContentsComponent = stack.get(BundleAPI.CUSTOM_BUNDLE_CONTENTS_COMPONENT);
		if (customBundleContentsComponent != null) {
			return NESTED_BUNDLE_OCCUPANCY.add(customBundleContentsComponent.getOccupancy());
		} else {
			List<BeehiveBlockEntity.BeeData> list = stack.getOrDefault(DataComponentTypes.BEES, BeesComponent.DEFAULT).bees();
			return !list.isEmpty() ? Fraction.ONE : Fraction.getFraction(1, stack.getMaxCount() * size_multiplier);
		}
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

	public static class Builder {
		private CustomBundleContentsComponent.Content content;
		private Fraction occupancy;
		//		private Optional<RegistryEntryList<Item>> tag;
		private int size_multiplier;

		public Builder(CustomBundleContentsComponent base) {
			this.content = new CustomBundleContentsComponent.Content(base.content.stacks);
			this.occupancy = base.occupancy;
//			this.tag = base.tag;
			this.size_multiplier = base.size_multiplier;
		}

		public CustomBundleContentsComponent.Builder clear() {
			this.content.stacks.clear();
			this.occupancy = Fraction.ZERO;
			return this;
		}

		private int addInternal(ItemStack stack) {
			if (!stack.isStackable()) {
				return -1;
			} else {
				for (int i = 0; i < this.content.stacks.size(); i++) {
					if (ItemStack.areItemsAndComponentsEqual((ItemStack) this.content.stacks.get(i), stack) && this.content.stacks.get(i).getCount() < this.content.stacks.get(i).getMaxCount()) {
						return i;
					}
				}

				return -1;
			}
		}

		private int getMaxAllowed(ItemStack stack) {
			Fraction fraction = Fraction.ONE.subtract(this.occupancy);
			return Math.max(fraction.divideBy(CustomBundleContentsComponent.getOccupancy(stack, this.size_multiplier)).intValue(), 0);
		}

		public int add(ItemStack stack) {
			if (!stack.isEmpty() && stack.getItem().canBeNested()) {
				int i = Math.min(stack.getCount(), this.getMaxAllowed(stack));
				if (i == 0) {
					return 0;
				} else {
					this.occupancy = this.occupancy.add(CustomBundleContentsComponent.getOccupancy(stack, this.size_multiplier).multiplyBy(Fraction.getFraction(i, 1)));
					int j = this.addInternal(stack);
					if (j != -1) {
						ItemStack itemStack = (ItemStack) this.content.stacks.remove(j);
						int maxCount = itemStack.getMaxCount();
						int count = itemStack.getCount();
						int countDiff = maxCount - count;
						if (i <= countDiff) {
							ItemStack itemStack2 = itemStack.copyWithCount(itemStack.getCount() + i);
							stack.decrement(i);
							this.content.stacks.add(0, itemStack2);
						} else {
							ItemStack itemStack2 = itemStack.copyWithCount(itemStack.getCount() + countDiff);
							this.content.stacks.add(0, itemStack2);

							ItemStack itemStack3 = stack.copyWithCount(i - countDiff);
							this.content.stacks.add(0, itemStack3);
							stack.decrement(i);
						}
					} else {
						this.content.stacks.add(0, stack.split(i));
					}

					return i;
				}
			} else {
				return 0;
			}
		}

		public int add(Slot slot, PlayerEntity player) {
			ItemStack itemStack = slot.getStack();
			int i = this.getMaxAllowed(itemStack);
			return this.add(slot.takeStackRange(itemStack.getCount(), i, player));
		}

		@Nullable
		public ItemStack removeFirst() {
			if (this.content.stacks.isEmpty()) {
				return null;
			} else {
				ItemStack itemStack = ((ItemStack) this.content.stacks.remove(0)).copy();
				this.occupancy = this.occupancy.subtract(CustomBundleContentsComponent.getOccupancy(itemStack, this.size_multiplier).multiplyBy(Fraction.getFraction(itemStack.getCount(), 1)));
				return itemStack;
			}
		}

		public Fraction getOccupancy() {
			return this.occupancy;
		}

		public CustomBundleContentsComponent.Builder size_multiplier(int size_multiplier) {
			this.size_multiplier = size_multiplier;
			return this;
		}

//		public Builder tag(Optional<RegistryEntryList<Item>> tag) {
//			tag.get().getTagKey()
//			this.tag = RegistryEntryList.of();
//			return this;
//		}

		public CustomBundleContentsComponent build() {
			return new CustomBundleContentsComponent(List.copyOf(this.content.stacks), this.occupancy/*, this.tag*/, this.size_multiplier);
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
