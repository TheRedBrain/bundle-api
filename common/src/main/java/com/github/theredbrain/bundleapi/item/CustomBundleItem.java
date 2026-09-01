package com.github.theredbrain.bundleapi.item;

import com.github.theredbrain.bundleapi.component.type.CustomBundleContentsComponent;
import com.github.theredbrain.bundleapi.item.tooltip.CustomBundleTooltipData;
import com.github.theredbrain.bundleapi.registry.BundleAPIDataComponentTypes;
import org.apache.commons.lang3.math.Fraction;

import java.util.Optional;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

public class CustomBundleItem extends Item {
	private static final int FULL_ITEM_BAR_COLOR = ARGB.colorFromFloat(1.0F, 1.0F, 0.33F, 0.33F);
	private static final int ITEM_BAR_COLOR = ARGB.colorFromFloat(1.0F, 0.44F, 0.53F, 1.0F);

	public CustomBundleItem(Properties settings) {
		super(settings);
	}

	public static float getFullnessDisplay(ItemStack stack) {
		CustomBundleContentsComponent customBundleContentsComponent = stack.getOrDefault(BundleAPIDataComponentTypes.CUSTOM_BUNDLE_CONTENTS_COMPONENT, CustomBundleContentsComponent.DEFAULT);
		return customBundleContentsComponent.weight().floatValue();
	}

	@Override
	public boolean overrideStackedOnOther(ItemStack stack, Slot slot, ClickAction clickType, Player player) {
		CustomBundleContentsComponent customBundleContentsComponent = stack.get(BundleAPIDataComponentTypes.CUSTOM_BUNDLE_CONTENTS_COMPONENT);
		if (customBundleContentsComponent == null) {
			return false;
		}

		ItemStack itemStack = slot.getItem();
		CustomBundleContentsComponent.Mutable mutable = new CustomBundleContentsComponent.Mutable(customBundleContentsComponent);
		if (clickType == ClickAction.PRIMARY && !itemStack.isEmpty()) {
			if (mutable.tryTransfer(slot, player) > 0) {
				playInsertSound(player);
			} else {
				playInsertFailSound(player);
			}

			stack.set(BundleAPIDataComponentTypes.CUSTOM_BUNDLE_CONTENTS_COMPONENT, mutable.toImmutable());
			this.onContentChanged(player);
			return true;
		} else if (clickType == ClickAction.SECONDARY && itemStack.isEmpty()) {
			ItemStack itemStack2 = mutable.removeSelected();
			if (itemStack2 != null) {
				ItemStack itemStack3 = slot.safeInsert(itemStack2);
				if (itemStack3.getCount() > 0) {
					mutable.tryInsert(itemStack3);
				} else {
					playRemoveOneSound(player);
				}
			}

			stack.set(BundleAPIDataComponentTypes.CUSTOM_BUNDLE_CONTENTS_COMPONENT, mutable.toImmutable());
			this.onContentChanged(player);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean overrideOtherStackedOnMe(ItemStack stack, ItemStack otherStack, Slot slot, ClickAction clickType, Player player, SlotAccess cursorStackReference) {
		if (clickType == ClickAction.PRIMARY && otherStack.isEmpty()) {
			setSelectedStackIndex(stack, -1);
			return false;
		}

		CustomBundleContentsComponent customBundleContentsComponent = stack.get(BundleAPIDataComponentTypes.CUSTOM_BUNDLE_CONTENTS_COMPONENT);
		if (customBundleContentsComponent == null) {
			return false;
		}

		CustomBundleContentsComponent.Mutable mutable = new CustomBundleContentsComponent.Mutable(customBundleContentsComponent);
		if (clickType == ClickAction.PRIMARY && !otherStack.isEmpty()) {
			if (slot.allowModification(player) && mutable.tryInsert(otherStack) > 0) {
				playInsertSound(player);
			} else {
				playInsertFailSound(player);
			}

			stack.set(BundleAPIDataComponentTypes.CUSTOM_BUNDLE_CONTENTS_COMPONENT, mutable.toImmutable());
			this.onContentChanged(player);
			return true;
		} else if (clickType == ClickAction.SECONDARY && otherStack.isEmpty()) {
			if (slot.allowModification(player)) {
				ItemStack itemStack = mutable.removeSelected();
				if (itemStack != null) {
					playRemoveOneSound(player);
					cursorStackReference.set(itemStack);
				}
			}

			stack.set(BundleAPIDataComponentTypes.CUSTOM_BUNDLE_CONTENTS_COMPONENT, mutable.toImmutable());
			this.onContentChanged(player);
			return true;
		} else {
			setSelectedStackIndex(stack, -1);
			return false;
		}
	}

	@Override
	public InteractionResult use(Level world, Player user, InteractionHand hand) {
		user.startUsingItem(hand);
		return InteractionResult.SUCCESS;
	}

	private void dropContentsOnUse(Level world, Player player, ItemStack stack) {
		if (this.dropFirstBundledStack(stack, player)) {
			playDropContentsSound(world, player);
			player.awardStat(Stats.ITEM_USED.get(this));
		}
	}

	@Override
	public boolean isBarVisible(ItemStack stack) {
		CustomBundleContentsComponent customBundleContentsComponent = stack.getOrDefault(BundleAPIDataComponentTypes.CUSTOM_BUNDLE_CONTENTS_COMPONENT, CustomBundleContentsComponent.DEFAULT);
		return customBundleContentsComponent.weight().compareTo(Fraction.ZERO) > 0;
	}

	@Override
	public int getBarWidth(ItemStack stack) {
		CustomBundleContentsComponent customBundleContentsComponent = stack.getOrDefault(BundleAPIDataComponentTypes.CUSTOM_BUNDLE_CONTENTS_COMPONENT, CustomBundleContentsComponent.DEFAULT);
		return Math.min(1 + Mth.mulAndTruncate(customBundleContentsComponent.weight(), 12), 13);
	}

	@Override
	public int getBarColor(ItemStack stack) {
		CustomBundleContentsComponent customBundleContentsComponent = stack.getOrDefault(BundleAPIDataComponentTypes.CUSTOM_BUNDLE_CONTENTS_COMPONENT, CustomBundleContentsComponent.DEFAULT);
		return customBundleContentsComponent.weight().compareTo(Fraction.ONE) >= 0 ? FULL_ITEM_BAR_COLOR : ITEM_BAR_COLOR;
	}

	public static void setSelectedStackIndex(ItemStack itemStack, int selectedStackIndex) {
		CustomBundleContentsComponent customBundleContentsComponent = itemStack.get(BundleAPIDataComponentTypes.CUSTOM_BUNDLE_CONTENTS_COMPONENT);
		if (customBundleContentsComponent != null) {
			CustomBundleContentsComponent.Mutable mutable = new CustomBundleContentsComponent.Mutable(customBundleContentsComponent);
			mutable.toggleSelectedItem(selectedStackIndex);
			itemStack.set(BundleAPIDataComponentTypes.CUSTOM_BUNDLE_CONTENTS_COMPONENT, mutable.toImmutable());
		}
	}

	public static boolean hasSelectedStack(ItemStack stack) {
		CustomBundleContentsComponent customBundleContentsComponent = stack.get(BundleAPIDataComponentTypes.CUSTOM_BUNDLE_CONTENTS_COMPONENT);
		return customBundleContentsComponent != null && customBundleContentsComponent.getSelectedItem() != -1;
	}

	public static int getSelectedStackIndex(ItemStack stack) {
		CustomBundleContentsComponent customBundleContentsComponent = stack.getOrDefault(BundleAPIDataComponentTypes.CUSTOM_BUNDLE_CONTENTS_COMPONENT, CustomBundleContentsComponent.DEFAULT);
		return customBundleContentsComponent.getSelectedItem();
	}

//	public static ItemStack getSelectedStack(ItemStack stack) {
//		CustomBundleContentsComponent customBundleContentsComponent = stack.get(BundleAPIDataComponentTypes.CUSTOM_BUNDLE_CONTENTS_COMPONENT);
//		return customBundleContentsComponent != null && customBundleContentsComponent.getSelectedStackIndex() != -1
//				? customBundleContentsComponent.get(customBundleContentsComponent.getSelectedStackIndex())
//				: ItemStack.EMPTY;
//	}

//	public static int getNumberOfStacksShown(ItemStack stack) {
//		CustomBundleContentsComponent customBundleContentsComponent = stack.getOrDefault(BundleAPIDataComponentTypes.CUSTOM_BUNDLE_CONTENTS_COMPONENT, CustomBundleContentsComponent.DEFAULT);
//		return customBundleContentsComponent.getNumberOfStacksShown();
//	}

	private boolean dropFirstBundledStack(ItemStack stack, Player player) {
		CustomBundleContentsComponent customBundleContentsComponent = stack.get(BundleAPIDataComponentTypes.CUSTOM_BUNDLE_CONTENTS_COMPONENT);
		if (customBundleContentsComponent != null && !customBundleContentsComponent.isEmpty()) {
			Optional<ItemStack> optional = popFirstBundledStack(stack, player, customBundleContentsComponent);
			if (optional.isPresent()) {
				player.drop(optional.get(), true);
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	private static Optional<ItemStack> popFirstBundledStack(ItemStack stack, Player player, CustomBundleContentsComponent contents) {
		CustomBundleContentsComponent.Mutable mutable = new CustomBundleContentsComponent.Mutable(contents);
		ItemStack itemStack = mutable.removeSelected();
		if (itemStack != null) {
			playRemoveOneSound(player);
			stack.set(BundleAPIDataComponentTypes.CUSTOM_BUNDLE_CONTENTS_COMPONENT, mutable.toImmutable());
			return Optional.of(itemStack);
		} else {
			return Optional.empty();
		}
	}

	@Override
	public void onUseTick(Level world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
		if (user instanceof Player playerEntity) {
			int i = this.getUseDuration(stack, user);
			boolean bl = remainingUseTicks == i;
			if (bl || remainingUseTicks < i - 10 && remainingUseTicks % 2 == 0) {
				this.dropContentsOnUse(world, playerEntity, stack);
			}
		}
	}

	@Override
	public int getUseDuration(ItemStack stack, LivingEntity user) {
		return 200;
	}

	@Override
	public ItemUseAnimation getUseAnimation(ItemStack stack) {
		return ItemUseAnimation.BUNDLE;
	}

	@Override
	public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
		TooltipDisplay tooltipDisplayComponent = stack.getOrDefault(DataComponents.TOOLTIP_DISPLAY, TooltipDisplay.DEFAULT);
		return !tooltipDisplayComponent.shows(BundleAPIDataComponentTypes.CUSTOM_BUNDLE_CONTENTS_COMPONENT)
				? Optional.empty()
				: Optional.ofNullable(stack.get(BundleAPIDataComponentTypes.CUSTOM_BUNDLE_CONTENTS_COMPONENT)).map(CustomBundleTooltipData::new);
	}

	@Override
	public void onDestroyed(ItemEntity entity) {
		CustomBundleContentsComponent customBundleContentsComponent = entity.getItem().get(BundleAPIDataComponentTypes.CUSTOM_BUNDLE_CONTENTS_COMPONENT);
		if (customBundleContentsComponent != null) {
			entity.getItem().set(BundleAPIDataComponentTypes.CUSTOM_BUNDLE_CONTENTS_COMPONENT, CustomBundleContentsComponent.DEFAULT);
			ItemUtils.onContainerDestroyed(entity, customBundleContentsComponent.itemsCopy());
		}
	}

	private static void playRemoveOneSound(Entity entity) {
		entity.playSound(SoundEvents.BUNDLE_REMOVE_ONE, 0.8F, 0.8F + entity.level().getRandom().nextFloat() * 0.4F);
	}

	private static void playInsertSound(Entity entity) {
		entity.playSound(SoundEvents.BUNDLE_INSERT, 0.8F, 0.8F + entity.level().getRandom().nextFloat() * 0.4F);
	}

	private static void playInsertFailSound(Entity entity) {
		entity.playSound(SoundEvents.BUNDLE_INSERT_FAIL, 1.0F, 1.0F);
	}

	private static void playDropContentsSound(Level world, Entity entity) {
		world.playSound(
				null,
				entity.blockPosition(),
				SoundEvents.BUNDLE_DROP_CONTENTS,
				SoundSource.PLAYERS,
				0.8F,
				0.8F + entity.level().getRandom().nextFloat() * 0.4F
		);
	}

	private void onContentChanged(Player user) {
		AbstractContainerMenu screenHandler = user.containerMenu;
		if (screenHandler != null) {
			screenHandler.slotsChanged(user.getInventory());
		}
	}
}
