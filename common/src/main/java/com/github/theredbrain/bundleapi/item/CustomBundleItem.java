package com.github.theredbrain.bundleapi.item;

import com.github.theredbrain.bundleapi.BundleAPI;
import com.github.theredbrain.bundleapi.component.type.CustomBundleContentsComponent;
import com.github.theredbrain.bundleapi.item.tooltip.CustomBundleTooltipData;
import org.apache.commons.lang3.math.Fraction;
import org.jspecify.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

public class CustomBundleItem extends Item {
	private static final int ITEM_BAR_COLOR = ARGB.colorFromFloat(1.0F, 0.4F, 0.4F, 1.0F);
	/**
	 * @deprecated Kept for binary compatibility. Since 1.21.4 item model properties are data-driven
	 * (see {@code bundleapi:custom_bundle/fullness}), so BundleAPI no longer iterates the instances
	 * to register model predicate providers.
	 */
	@Deprecated
	public final static HashSet<CustomBundleItem> instances = new HashSet<>();
	private final TagKey<Item> tag;
	private final Component emptyDescription;

	/**
	 * @param tag              items allowed inside, {@code null} to allow anything nestable
	 * @param emptyDescription hint shown inside the tooltip while the bundle is empty,
	 *                         {@code null} for vanilla's "Can hold a mixed stack of items"
	 */
	public CustomBundleItem(@Nullable TagKey<Item> tag, @Nullable Component emptyDescription, Properties settings) {
		super(settings);
		this.tag = tag;
		this.emptyDescription = emptyDescription != null ? emptyDescription : CustomBundleTooltipData.DEFAULT_EMPTY_DESCRIPTION;
		instances.add(this);
	}

	public CustomBundleItem(@Nullable TagKey<Item> tag, Properties settings) {
		this(tag, null, settings);
	}

	public CustomBundleItem(Properties settings) {
		this(null, null, settings);
	}

	/**
	 * Hint drawn inside the tooltip while this bundle is empty. Never {@code null}.
	 */
	public Component getEmptyDescription() {
		return this.emptyDescription;
	}

	public static float getAmountFilled(ItemStack stack) {
		CustomBundleContentsComponent customBundleContentsComponent = stack.getOrDefault(BundleAPI.CUSTOM_BUNDLE_CONTENTS_COMPONENT, CustomBundleContentsComponent.DEFAULT);
		return customBundleContentsComponent.getOccupancy().floatValue();
	}

	@Override
	public boolean overrideStackedOnOther(ItemStack stack, Slot slot, ClickAction clickType, Player player) {
		if (clickType != ClickAction.SECONDARY) {
			return false;
		} else {
			CustomBundleContentsComponent customBundleContentsComponent = stack.get(BundleAPI.CUSTOM_BUNDLE_CONTENTS_COMPONENT);
			if (customBundleContentsComponent == null) {
				return false;
			} else {
				ItemStack itemStack = slot.getItem();
				CustomBundleContentsComponent.Builder builder = new CustomBundleContentsComponent.Builder(customBundleContentsComponent);
				if (itemStack.isEmpty()) {
					this.playRemoveOneSound(player);
					ItemStack itemStack2 = builder.removeFirst();
					if (itemStack2 != null) {
						ItemStack itemStack3 = slot.safeInsert(itemStack2);
						builder.add(itemStack3);
					}
				} else if (itemStack.getItem().canFitInsideContainerItems() && (this.tag == null || itemStack.is(this.tag))) {
					int i = builder.add(slot, player);
					if (i > 0) {
						this.playInsertSound(player);
					}
				}

				stack.set(BundleAPI.CUSTOM_BUNDLE_CONTENTS_COMPONENT, builder.build());
				return true;
			}
		}
	}

	@Override
	public boolean overrideOtherStackedOnMe(ItemStack stack, ItemStack otherStack, Slot slot, ClickAction clickType, Player player, SlotAccess cursorStackReference) {
		if (clickType == ClickAction.SECONDARY && slot.allowModification(player)) {
			CustomBundleContentsComponent customBundleContentsComponent = stack.get(BundleAPI.CUSTOM_BUNDLE_CONTENTS_COMPONENT);
			if (customBundleContentsComponent == null) {
				return false;
			} else {
				CustomBundleContentsComponent.Builder builder = new CustomBundleContentsComponent.Builder(customBundleContentsComponent);
				if (otherStack.isEmpty()) {
					ItemStack itemStack = builder.removeFirst();
					if (itemStack != null) {
						this.playRemoveOneSound(player);
						cursorStackReference.set(itemStack);
					}
				} else if (this.tag == null || otherStack.is(this.tag)) {
					int i = builder.add(otherStack);
					if (i > 0) {
						this.playInsertSound(player);
					}
				}

				stack.set(BundleAPI.CUSTOM_BUNDLE_CONTENTS_COMPONENT, builder.build());
				return true;
			}
		} else {
			return false;
		}
	}

	@Override
	public InteractionResult use(Level world, Player user, InteractionHand hand) {
		ItemStack itemStack = user.getItemInHand(hand);
		if (dropAllBundledItems(itemStack, user)) {
			this.playDropContentsSound(user);
			user.awardStat(Stats.ITEM_USED.get(this));
			return world.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER;
		} else {
			return InteractionResult.FAIL;
		}
	}

	@Override
	public boolean isBarVisible(ItemStack stack) {
		CustomBundleContentsComponent customBundleContentsComponent = stack.getOrDefault(BundleAPI.CUSTOM_BUNDLE_CONTENTS_COMPONENT, CustomBundleContentsComponent.DEFAULT);
		return customBundleContentsComponent.getOccupancy().compareTo(Fraction.ZERO) > 0;
	}

	@Override
	public int getBarWidth(ItemStack stack) {
		CustomBundleContentsComponent customBundleContentsComponent = stack.getOrDefault(BundleAPI.CUSTOM_BUNDLE_CONTENTS_COMPONENT, CustomBundleContentsComponent.DEFAULT);
		return Math.min(1 + Mth.mulAndTruncate(customBundleContentsComponent.getOccupancy(), 12), 13);
	}

	@Override
	public int getBarColor(ItemStack stack) {
		return ITEM_BAR_COLOR;
	}

	private static boolean dropAllBundledItems(ItemStack stack, Player player) {
		CustomBundleContentsComponent customBundleContentsComponent = stack.get(BundleAPI.CUSTOM_BUNDLE_CONTENTS_COMPONENT);
		if (customBundleContentsComponent != null && !customBundleContentsComponent.isEmpty()) {
			stack.set(BundleAPI.CUSTOM_BUNDLE_CONTENTS_COMPONENT, new CustomBundleContentsComponent.Builder(customBundleContentsComponent).clear().build());
			if (player instanceof ServerPlayer) {
				customBundleContentsComponent.stream().forEach(stackx -> player.drop(stackx, true));
			}

			return true;
		} else {
			return false;
		}
	}

	@Override
	public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
		TooltipDisplay tooltipDisplayComponent = stack.getOrDefault(DataComponents.TOOLTIP_DISPLAY, TooltipDisplay.DEFAULT);
		return !tooltipDisplayComponent.shows(BundleAPI.CUSTOM_BUNDLE_CONTENTS_COMPONENT)
			? Optional.empty()
			: Optional.ofNullable(stack.get(BundleAPI.CUSTOM_BUNDLE_CONTENTS_COMPONENT)).map(contents -> new CustomBundleTooltipData(contents, this.emptyDescription));
	}

	@Override
	public void onDestroyed(ItemEntity entity) {
		CustomBundleContentsComponent customBundleContentsComponent = entity.getItem().get(BundleAPI.CUSTOM_BUNDLE_CONTENTS_COMPONENT);
		if (customBundleContentsComponent != null) {
			entity.getItem().set(BundleAPI.CUSTOM_BUNDLE_CONTENTS_COMPONENT, new CustomBundleContentsComponent.Builder(customBundleContentsComponent).clear().build());
			ItemUtils.onContainerDestroyed(entity, customBundleContentsComponent.stream());
		}
	}

	private void playRemoveOneSound(Entity entity) {
		entity.playSound(SoundEvents.BUNDLE_REMOVE_ONE, 0.8F, 0.8F + entity.level().getRandom().nextFloat() * 0.4F);
	}

	private void playInsertSound(Entity entity) {
		entity.playSound(SoundEvents.BUNDLE_INSERT, 0.8F, 0.8F + entity.level().getRandom().nextFloat() * 0.4F);
	}

	private void playDropContentsSound(Entity entity) {
		entity.playSound(SoundEvents.BUNDLE_DROP_CONTENTS, 0.8F, 0.8F + entity.level().getRandom().nextFloat() * 0.4F);
	}
}
