package com.github.theredbrain.bundleapi.item;

import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BundleItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.collection.DefaultedList;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * NBT-backed storage for {@link CustomBundleItem} stacks (1.20.1 port of the 1.21.1
 * {@code CustomBundleContentsComponent}).
 *
 * <p>Contents live in the same {@code Items} list that vanilla {@link BundleItem} uses, so the
 * serialized form of a custom bundle is a superset of a vanilla bundle's and vanilla tooling
 * (tooltips, NBT commands, loot NBT functions) keeps working.
 *
 * <p>Occupancy is measured in vanilla's integer "units": an item takes {@code 64 / maxCount} units,
 * a bundle is full at {@code 64 * sizeMultiplier} units. This is numerically identical to the
 * 1.21.1 fraction model ({@code 1 / (maxCount * sizeMultiplier)} per item) - a bundle with
 * multiplier {@code n} holds {@code n} full stacks of any one item.
 */
public final class CustomBundleContents {
	public static final String ITEMS_KEY = "Items";
	/** Vanilla {@code BundleItem.MAX_STORAGE}: capacity of a multiplier-1 bundle. */
	public static final int BASE_CAPACITY = 64;
	/** Vanilla {@code BundleItem.BUNDLE_ITEM_OCCUPANCY}: flat cost of nesting a bundle inside another. */
	public static final int NESTED_BUNDLE_OCCUPANCY = 4;

	private CustomBundleContents() {
	}

	// --- capacity -------------------------------------------------------------------------------

	public static int capacity(int sizeMultiplier) {
		return BASE_CAPACITY * Math.max(1, sizeMultiplier);
	}

	/** Capacity of the given bundle stack in occupancy units; vanilla bundles report 64. */
	public static int capacity(ItemStack bundle) {
		return bundle.getItem() instanceof CustomBundleItem custom ? custom.getCapacity() : BASE_CAPACITY;
	}

	// --- reading --------------------------------------------------------------------------------

	@Nullable
	private static NbtList getItemList(ItemStack bundle) {
		NbtCompound nbt = bundle.getNbt();
		if (nbt == null || !nbt.contains(ITEMS_KEY, NbtElement.LIST_TYPE)) {
			return null;
		}
		return nbt.getList(ITEMS_KEY, NbtElement.COMPOUND_TYPE);
	}

	public static Stream<ItemStack> stream(ItemStack bundle) {
		NbtList list = getItemList(bundle);
		if (list == null) {
			return Stream.empty();
		}
		return list.stream().map(NbtCompound.class::cast).map(ItemStack::fromNbt);
	}

	/** Snapshot of the contents (copies); first element is the most recently inserted stack. */
	public static DefaultedList<ItemStack> list(ItemStack bundle) {
		DefaultedList<ItemStack> stacks = DefaultedList.of();
		stream(bundle).forEach(stacks::add);
		return stacks;
	}

	public static boolean isEmpty(ItemStack bundle) {
		NbtList list = getItemList(bundle);
		return list == null || list.isEmpty();
	}

	/** Occupancy units used by one unit of the given stack when stored in a bundle. */
	public static int getItemOccupancy(ItemStack stack) {
		Item item = stack.getItem();
		if (item instanceof BundleItem || item instanceof CustomBundleItem) {
			return NESTED_BUNDLE_OCCUPANCY + getOccupancy(stack);
		}
		if ((stack.isOf(Items.BEEHIVE) || stack.isOf(Items.BEE_NEST)) && stack.hasNbt()) {
			NbtCompound blockEntityNbt = BlockItem.getBlockEntityNbt(stack);
			if (blockEntityNbt != null && !blockEntityNbt.getList(BeehiveBlockEntity.BEES_KEY, NbtElement.COMPOUND_TYPE).isEmpty()) {
				return BASE_CAPACITY;
			}
		}
		return BASE_CAPACITY / stack.getMaxCount();
	}

	/** Total occupancy units currently used by the bundle's contents. */
	public static int getOccupancy(ItemStack bundle) {
		return stream(bundle).mapToInt(stack -> getItemOccupancy(stack) * stack.getCount()).sum();
	}

	/** {@code 0..1} fill ratio of the bundle relative to its own capacity. */
	public static float getFillFraction(ItemStack bundle) {
		return Math.min(1.0F, getOccupancy(bundle) / (float) capacity(bundle));
	}

	/**
	 * Whether the given stack may be inserted at all: it must be nestable and, if the bundle has a
	 * content tag, be in that tag. Does not consider remaining space.
	 */
	public static boolean accepts(ItemStack bundle, ItemStack stack) {
		if (stack.isEmpty() || !stack.getItem().canBeNested()) {
			return false;
		}
		if (bundle.getItem() instanceof CustomBundleItem custom) {
			TagKey<Item> tag = custom.getContentTag();
			return tag == null || stack.isIn(tag);
		}
		return true;
	}

	/** How many items of the given stack would still fit (0 if the stack is not accepted). */
	public static int getMaxAllowed(ItemStack bundle, ItemStack stack) {
		if (!accepts(bundle, stack)) {
			return 0;
		}
		int free = capacity(bundle) - getOccupancy(bundle);
		return Math.max(0, free / getItemOccupancy(stack));
	}

	// --- mutation -------------------------------------------------------------------------------

	/**
	 * Inserts as much of {@code stack} as fits. Returns the number of items moved into the bundle.
	 * Like vanilla's {@code addToBundle}, this does NOT shrink {@code stack}; the caller decides
	 * what to do with the source.
	 *
	 * <p>Unlike vanilla (whose capacity never exceeds one stack), entries are capped at the item's
	 * max stack size so a multiplier bundle never produces an over-stacked entry when emptied.
	 */
	public static int add(ItemStack bundle, ItemStack stack) {
		int toAdd = Math.min(stack.getCount(), getMaxAllowed(bundle, stack));
		if (toAdd <= 0) {
			return 0;
		}

		NbtCompound nbt = bundle.getOrCreateNbt();
		if (!nbt.contains(ITEMS_KEY, NbtElement.LIST_TYPE)) {
			nbt.put(ITEMS_KEY, new NbtList());
		}
		NbtList items = nbt.getList(ITEMS_KEY, NbtElement.COMPOUND_TYPE);

		int remaining = toAdd;
		int mergeIndex = findMergeIndex(stack, items);
		if (mergeIndex >= 0) {
			NbtCompound entry = items.getCompound(mergeIndex);
			ItemStack existing = ItemStack.fromNbt(entry);
			int merged = Math.min(existing.getMaxCount() - existing.getCount(), remaining);
			existing.increment(merged);
			existing.writeNbt(entry);
			items.remove(mergeIndex);
			items.add(0, entry);
			remaining -= merged;
		}
		if (remaining > 0) {
			NbtCompound entry = new NbtCompound();
			stack.copyWithCount(remaining).writeNbt(entry);
			items.add(0, entry);
		}
		return toAdd;
	}

	private static int findMergeIndex(ItemStack stack, NbtList items) {
		Item item = stack.getItem();
		if (item instanceof BundleItem || item instanceof CustomBundleItem) {
			return -1;
		}
		for (int i = 0; i < items.size(); i++) {
			ItemStack existing = ItemStack.fromNbt(items.getCompound(i));
			if (existing.getCount() < existing.getMaxCount() && ItemStack.canCombine(existing, stack)) {
				return i;
			}
		}
		return -1;
	}

	/** Removes and returns the most recently inserted stack, if any. */
	public static Optional<ItemStack> removeFirst(ItemStack bundle) {
		NbtCompound nbt = bundle.getNbt();
		if (nbt == null || !nbt.contains(ITEMS_KEY, NbtElement.LIST_TYPE)) {
			return Optional.empty();
		}
		NbtList items = nbt.getList(ITEMS_KEY, NbtElement.COMPOUND_TYPE);
		if (items.isEmpty()) {
			bundle.removeSubNbt(ITEMS_KEY);
			return Optional.empty();
		}
		ItemStack removed = ItemStack.fromNbt(items.getCompound(0));
		items.remove(0);
		if (items.isEmpty()) {
			bundle.removeSubNbt(ITEMS_KEY);
		}
		return Optional.of(removed);
	}

	/** Removes everything from the bundle and returns what was inside (copies). */
	public static List<ItemStack> clear(ItemStack bundle) {
		List<ItemStack> removed = new ArrayList<>();
		stream(bundle).forEach(removed::add);
		if (bundle.getNbt() != null) {
			bundle.removeSubNbt(ITEMS_KEY);
		}
		return removed;
	}
}
