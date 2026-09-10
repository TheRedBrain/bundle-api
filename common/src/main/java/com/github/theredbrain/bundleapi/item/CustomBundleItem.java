package com.github.theredbrain.bundleapi.item;

import net.minecraft.client.item.BundleTooltipData;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.item.TooltipData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.ClickType;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

/**
 * A bundle with a configurable capacity ({@code 64 * sizeMultiplier} occupancy units, i.e.
 * {@code sizeMultiplier} full stacks of one item) and an optional content whitelist tag.
 *
 * <p>Contents are stored in the stack NBT under {@code Items}, see {@link CustomBundleContents}.
 * The capacity and the tag are properties of the item, not of the stack.
 */
public class CustomBundleItem extends Item {
	private static final int ITEM_BAR_COLOR = MathHelper.packRgb(0.4F, 0.4F, 1.0F);
	/** Every constructed instance; the client entrypoints iterate this to register the {@code filled} model predicate. */
	public final static HashSet<CustomBundleItem> instances = new HashSet<>();
	@Nullable
	private final TagKey<Item> tag;
	private final int sizeMultiplier;

	public CustomBundleItem(@Nullable TagKey<Item> tag, int sizeMultiplier, Settings settings) {
		super(settings);
		this.tag = tag;
		this.sizeMultiplier = Math.max(1, sizeMultiplier);
		instances.add(this);
	}

	public CustomBundleItem(@Nullable TagKey<Item> tag, Settings settings) {
		this(tag, 1, settings);
	}

	public CustomBundleItem(Settings settings) {
		this(null, 1, settings);
	}

	/** Whitelist tag for the contents, or {@code null} when any nestable item is accepted. */
	@Nullable
	public TagKey<Item> getContentTag() {
		return this.tag;
	}

	public int getSizeMultiplier() {
		return this.sizeMultiplier;
	}

	/** Capacity in occupancy units ({@code 64 * sizeMultiplier}). */
	public int getCapacity() {
		return CustomBundleContents.capacity(this.sizeMultiplier);
	}

	/** {@code 0..1}: used by the {@code filled} model predicate. */
	public static float getAmountFilled(ItemStack stack) {
		return CustomBundleContents.getFillFraction(stack);
	}

	@Override
	public boolean onStackClicked(ItemStack stack, Slot slot, ClickType clickType, PlayerEntity player) {
		if (clickType != ClickType.RIGHT) {
			return false;
		}

		ItemStack slotStack = slot.getStack();
		if (slotStack.isEmpty()) {
			this.playRemoveOneSound(player);
			CustomBundleContents.removeFirst(stack).ifPresent(removed -> CustomBundleContents.add(stack, slot.insertStack(removed)));
		} else if (CustomBundleContents.accepts(stack, slotStack)) {
			int max = CustomBundleContents.getMaxAllowed(stack, slotStack);
			int added = CustomBundleContents.add(stack, slot.takeStackRange(slotStack.getCount(), max, player));
			if (added > 0) {
				this.playInsertSound(player);
			}
		}

		return true;
	}

	@Override
	public boolean onClicked(ItemStack stack, ItemStack otherStack, Slot slot, ClickType clickType, PlayerEntity player, StackReference cursorStackReference) {
		if (clickType == ClickType.RIGHT && slot.canTakePartial(player)) {
			if (otherStack.isEmpty()) {
				CustomBundleContents.removeFirst(stack).ifPresent(removed -> {
					this.playRemoveOneSound(player);
					cursorStackReference.set(removed);
				});
			} else if (CustomBundleContents.accepts(stack, otherStack)) {
				int added = CustomBundleContents.add(stack, otherStack);
				if (added > 0) {
					this.playInsertSound(player);
					otherStack.decrement(added);
				}
			}

			return true;
		} else {
			return false;
		}
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		ItemStack itemStack = user.getStackInHand(hand);
		if (dropAllBundledItems(itemStack, user)) {
			this.playDropContentsSound(user);
			user.incrementStat(Stats.USED.getOrCreateStat(this));
			return TypedActionResult.success(itemStack, world.isClient());
		} else {
			return TypedActionResult.fail(itemStack);
		}
	}

	@Override
	public boolean isItemBarVisible(ItemStack stack) {
		return CustomBundleContents.getOccupancy(stack) > 0;
	}

	@Override
	public int getItemBarStep(ItemStack stack) {
		return Math.min(1 + 12 * CustomBundleContents.getOccupancy(stack) / this.getCapacity(), 13);
	}

	@Override
	public int getItemBarColor(ItemStack stack) {
		return ITEM_BAR_COLOR;
	}

	private static boolean dropAllBundledItems(ItemStack stack, PlayerEntity player) {
		if (CustomBundleContents.isEmpty(stack)) {
			return false;
		}
		List<ItemStack> contents = CustomBundleContents.clear(stack);
		if (player instanceof ServerPlayerEntity) {
			for (ItemStack content : contents) {
				player.dropItem(content, true);
			}
		}
		return true;
	}

	/**
	 * Vanilla's {@link BundleTooltipData} so vanilla's {@code BundleTooltipComponent} renders the grid.
	 * That component treats {@code occupancy >= 64} as "full", so the occupancy is scaled from this
	 * bundle's capacity down to the vanilla 0..64 range.
	 */
	@Override
	public Optional<TooltipData> getTooltipData(ItemStack stack) {
		int scaledOccupancy = CustomBundleContents.getOccupancy(stack) * CustomBundleContents.BASE_CAPACITY / this.getCapacity();
		return Optional.of(new BundleTooltipData(CustomBundleContents.list(stack), scaledOccupancy));
	}

	@Override
	public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
		tooltip.add(Text.translatable("item.minecraft.bundle.fullness", CustomBundleContents.getOccupancy(stack), this.getCapacity()).formatted(Formatting.GRAY));
	}

	@Override
	public void onItemEntityDestroyed(ItemEntity entity) {
		ItemUsage.spawnItemContents(entity, CustomBundleContents.clear(entity.getStack()).stream());
	}

	private void playRemoveOneSound(Entity entity) {
		entity.playSound(SoundEvents.ITEM_BUNDLE_REMOVE_ONE, 0.8F, 0.8F + entity.getWorld().getRandom().nextFloat() * 0.4F);
	}

	private void playInsertSound(Entity entity) {
		entity.playSound(SoundEvents.ITEM_BUNDLE_INSERT, 0.8F, 0.8F + entity.getWorld().getRandom().nextFloat() * 0.4F);
	}

	private void playDropContentsSound(Entity entity) {
		entity.playSound(SoundEvents.ITEM_BUNDLE_DROP_CONTENTS, 0.8F, 0.8F + entity.getWorld().getRandom().nextFloat() * 0.4F);
	}
}
