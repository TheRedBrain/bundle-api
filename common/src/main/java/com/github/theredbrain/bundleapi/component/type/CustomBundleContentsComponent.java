package com.github.theredbrain.bundleapi.component.type;

import com.github.theredbrain.bundleapi.BundleAPI;
import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.apache.commons.lang3.math.Fraction;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.component.Bees;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;

/**
 * Mirrors vanilla {@link BundleContents} (26.1: stored as immutable {@link ItemStackTemplate}s, because no
 * {@link ItemStack} may exist before registries load), extended with a {@code size_multiplier} that scales the
 * capacity. The occupancy is computed from the templates when the component is created and is {@link Fraction#ONE}
 * ("full") when the weight cannot be computed (vanilla treats an errored weight the same way for display).
 *
 * @param content         the stored items, most recently inserted first
 * @param occupancy       the fraction of the bundle that is used, in {@code [0, 1]}
 * @param size_multiplier capacity multiplier relative to a vanilla bundle
 */
public record CustomBundleContentsComponent(Content content, Fraction occupancy/*, Optional<RegistryEntryList<Item>> tag  TODO add tag key in 1.21.4*/, int size_multiplier) implements TooltipComponent {
	public static final CustomBundleContentsComponent DEFAULT = new CustomBundleContentsComponent(Content.DEFAULT, Fraction.ZERO/*, Optional.empty()*/, 1);
	public static final Codec<CustomBundleContentsComponent> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
							CustomBundleContentsComponent.Content.CODEC.fieldOf("content").forGetter(component -> component.content),
//							RegistryCodecs.entryList(RegistryKeys.ITEM).optionalFieldOf("tag").forGetter(component -> component.tag),
							Codec.INT.optionalFieldOf("size_multiplier", 1).forGetter(component -> component.size_multiplier)
					)
					.apply(instance, CustomBundleContentsComponent::new)
	);
	public static final StreamCodec<RegistryFriendlyByteBuf, CustomBundleContentsComponent> PACKET_CODEC = StreamCodec.composite(
			CustomBundleContentsComponent.Content.PACKET_CODEC,
			component -> component.content,
//			PacketCodecs.optional(PacketCodecs.registryEntryList(RegistryKeys.ITEM)),
//			component -> component.tag,
			ByteBufCodecs.VAR_INT,
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

	public CustomBundleContentsComponent(Content content/*, Optional<RegistryEntryList<Item>> tag*/, int size_multiplier) {
		this(content, calculateOccupancySafe(content.items, size_multiplier)/*, tag*/, size_multiplier);
	}

	public CustomBundleContentsComponent(/*Optional<RegistryEntryList<Item>> tag, */int size_multiplier) {
		this(Content.DEFAULT, Fraction.ZERO/*, tag*/, size_multiplier);
	}

	public static CustomBundleContentsComponent.Builder builder() {
		return new CustomBundleContentsComponent.Builder(DEFAULT);
	}

	private static Fraction calculateOccupancySafe(List<? extends ItemInstance> items, int size_multiplier) {
		return calculateOccupancy(items, size_multiplier).result().orElse(Fraction.ONE);
	}

	/**
	 * Mirrors {@code BundleContents#computeContentWeight}.
	 */
	private static DataResult<Fraction> calculateOccupancy(List<? extends ItemInstance> items, int size_multiplier) {
		try {
			Fraction fraction = Fraction.ZERO;

			for (ItemInstance item : items) {
				DataResult<Fraction> itemOccupancy = getOccupancy(item, size_multiplier);
				if (itemOccupancy.isError()) {
					return itemOccupancy;
				}

				fraction = fraction.add(itemOccupancy.getOrThrow().multiplyBy(Fraction.getFraction(item.count(), 1)));
			}

			return DataResult.success(fraction);
		} catch (ArithmeticException exception) {
			return DataResult.error(() -> "Excessive total bundle weight");
		}
	}

	/**
	 * Mirrors {@code BundleContents#getWeight}: the occupancy of a single item of the given instance.
	 */
	static DataResult<Fraction> getOccupancy(ItemInstance item, int size_multiplier) {
		CustomBundleContentsComponent customBundleContentsComponent = item.get(BundleAPI.CUSTOM_BUNDLE_CONTENTS_COMPONENT);
		if (customBundleContentsComponent != null) {
			return DataResult.success(NESTED_BUNDLE_OCCUPANCY.add(customBundleContentsComponent.getOccupancy()));
		}

		BundleContents bundleContents = item.get(DataComponents.BUNDLE_CONTENTS);
		if (bundleContents != null) {
			return bundleContents.weight().map(nested -> nested.add(NESTED_BUNDLE_OCCUPANCY));
		}

		List<BeehiveBlockEntity.Occupant> list = item.getOrDefault(DataComponents.BEES, Bees.EMPTY).bees();
		return !list.isEmpty() ? BundleContents.BEEHIVE_WEIGHT : DataResult.success(Fraction.getFraction(1, item.getMaxStackSize() * size_multiplier));
	}

	/**
	 * @return a fresh {@link ItemStack} created from the template at {@code index}
	 */
	public ItemStack get(int index) {
		return this.content.items.get(index).create();
	}

	/**
	 * The stored item templates, most recently inserted first. Mirrors {@link BundleContents#items()}.
	 */
	public List<ItemStackTemplate> items() {
		return this.content.items;
	}

	/**
	 * Fresh stacks created from the templates. Mirrors {@link BundleContents#itemCopyStream()}.
	 */
	public Stream<ItemStack> stream() {
		return this.content.items.stream().map(ItemStackTemplate::create);
	}

	/**
	 * @deprecated the contents are immutable templates since 26.1; use {@link #items()} for the templates or
	 * {@link #stream()} / {@link #iterateCopy()} for stacks. Both iterate methods now create fresh stacks.
	 */
	@Deprecated
	public Iterable<ItemStack> iterate() {
		return this.iterateCopy();
	}

	public Iterable<ItemStack> iterateCopy() {
		return this.stream().toList();
	}

	public int size() {
		return this.content.items.size();
	}

	public int sizeMultiplier() {
		return this.size_multiplier;
	}

	public Fraction getOccupancy() {
		return this.occupancy;
	}

	public boolean isEmpty() {
		return this.content.items.isEmpty();
	}

	/**
	 * Mirrors {@link BundleContents.Mutable}: works on mutable {@link ItemStack}s and converts back to templates in
	 * {@link #build()}.
	 */
	public static class Builder {
		private final List<ItemStack> stacks;
		private Fraction occupancy;
		//		private Optional<RegistryEntryList<Item>> tag;
		private int size_multiplier;

		public Builder(CustomBundleContentsComponent base) {
			this.stacks = new ArrayList<>(base.content.items.size());
			for (ItemStackTemplate item : base.content.items) {
				this.stacks.add(item.create());
			}
			this.occupancy = base.occupancy;
//			this.tag = base.tag;
			this.size_multiplier = base.size_multiplier;
		}

		public CustomBundleContentsComponent.Builder clear() {
			this.stacks.clear();
			this.occupancy = Fraction.ZERO;
			return this;
		}

		private int addInternal(ItemStack stack) {
			if (!stack.isStackable()) {
				return -1;
			} else {
				for (int i = 0; i < this.stacks.size(); i++) {
					if (ItemStack.isSameItemSameComponents(this.stacks.get(i), stack) && this.stacks.get(i).getCount() < this.stacks.get(i).getMaxStackSize()) {
						return i;
					}
				}

				return -1;
			}
		}

		private int getMaxAllowed(Fraction itemOccupancy) {
			Fraction fraction = Fraction.ONE.subtract(this.occupancy);
			return Math.max(fraction.divideBy(itemOccupancy).intValue(), 0);
		}

		public int add(ItemStack stack) {
			if (!stack.isEmpty() && stack.getItem().canFitInsideContainerItems()) {
				DataResult<Fraction> maybeOccupancy = CustomBundleContentsComponent.getOccupancy(stack, this.size_multiplier);
				if (maybeOccupancy.isError()) {
					return 0;
				}
				Fraction itemOccupancy = maybeOccupancy.getOrThrow();
				int i = Math.min(stack.getCount(), this.getMaxAllowed(itemOccupancy));
				if (i == 0) {
					return 0;
				} else {
					this.occupancy = this.occupancy.add(itemOccupancy.multiplyBy(Fraction.getFraction(i, 1)));
					int j = this.addInternal(stack);
					if (j != -1) {
						// Unlike vanilla, stacks are never grown past their max stack size (templates cap the count at 99).
						ItemStack itemStack = this.stacks.remove(j);
						int maxCount = itemStack.getMaxStackSize();
						int count = itemStack.getCount();
						int countDiff = maxCount - count;
						if (i <= countDiff) {
							ItemStack itemStack2 = itemStack.copyWithCount(itemStack.getCount() + i);
							stack.shrink(i);
							this.stacks.add(0, itemStack2);
						} else {
							ItemStack itemStack2 = itemStack.copyWithCount(itemStack.getCount() + countDiff);
							this.stacks.add(0, itemStack2);

							ItemStack itemStack3 = stack.copyWithCount(i - countDiff);
							this.stacks.add(0, itemStack3);
							stack.shrink(i);
						}
					} else {
						this.stacks.add(0, stack.split(i));
					}

					return i;
				}
			} else {
				return 0;
			}
		}

		public int add(Slot slot, Player player) {
			ItemStack itemStack = slot.getItem();
			DataResult<Fraction> itemOccupancy = CustomBundleContentsComponent.getOccupancy(itemStack, this.size_multiplier);
			if (itemOccupancy.isError()) {
				return 0;
			}
			int i = this.getMaxAllowed(itemOccupancy.getOrThrow());
			return this.add(slot.safeTake(itemStack.getCount(), i, player));
		}

		public @Nullable ItemStack removeFirst() {
			if (this.stacks.isEmpty()) {
				return null;
			} else {
				ItemStack itemStack = this.stacks.remove(0).copy();
				this.occupancy = this.occupancy.subtract(CustomBundleContentsComponent.getOccupancy(itemStack, this.size_multiplier).getOrThrow().multiplyBy(Fraction.getFraction(itemStack.getCount(), 1)));
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
			ImmutableList.Builder<ItemStackTemplate> builder = ImmutableList.builder();
			for (ItemStack stack : this.stacks) {
				if (!stack.isEmpty()) {
					builder.add(ItemStackTemplate.fromNonEmptyStack(stack));
				}
			}
			return new CustomBundleContentsComponent(new Content(builder.build()), this.occupancy/*, this.tag*/, this.size_multiplier);
		}
	}

	/**
	 * @param items the stored item templates, most recently inserted first (immutable)
	 */
	public record Content(List<ItemStackTemplate> items) {
		public static final Content DEFAULT = new Content(List.of());
		public static final Codec<Content> CODEC = ItemStackTemplate.CODEC.listOf().xmap(Content::new, content -> content.items);
		public static final StreamCodec<RegistryFriendlyByteBuf, Content> PACKET_CODEC = ItemStackTemplate.STREAM_CODEC
				.apply(ByteBufCodecs.list())
				.map(Content::new, content -> content.items);

		public Content(List<ItemStackTemplate> items) {
			this.items = List.copyOf(items);
		}
	}
}
