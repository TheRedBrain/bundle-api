package com.github.theredbrain.bundleapi.item;

import com.github.theredbrain.bundleapi.component.type.CustomBundleContentsComponent;
import com.github.theredbrain.bundleapi.item.tooltip.CustomBundleTooltipData;
import com.github.theredbrain.bundleapi.registry.BundleAPIDataComponentTypes;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.item.consume.UseAction;
import net.minecraft.item.tooltip.TooltipData;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ClickType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.apache.commons.lang3.math.Fraction;

import java.util.Optional;

public class CustomBundleItem extends Item {
	private static final int FULL_ITEM_BAR_COLOR = ColorHelper.fromFloats(1.0F, 1.0F, 0.33F, 0.33F);
	private static final int ITEM_BAR_COLOR = ColorHelper.fromFloats(1.0F, 0.44F, 0.53F, 1.0F);

	public CustomBundleItem(Settings settings) {
		super(settings);
	}

	public static float getAmountFilled(ItemStack stack) {
		CustomBundleContentsComponent customBundleContentsComponent = stack.getOrDefault(BundleAPIDataComponentTypes.CUSTOM_BUNDLE_CONTENTS_COMPONENT, CustomBundleContentsComponent.DEFAULT);
		return customBundleContentsComponent.getOccupancy().floatValue();
	}

	@Override
	public boolean onStackClicked(ItemStack stack, Slot slot, ClickType clickType, PlayerEntity player) {
		CustomBundleContentsComponent customBundleContentsComponent = stack.get(BundleAPIDataComponentTypes.CUSTOM_BUNDLE_CONTENTS_COMPONENT);
		if (customBundleContentsComponent == null) {
			return false;
		}

		ItemStack itemStack = slot.getStack();
		CustomBundleContentsComponent.Builder builder = new CustomBundleContentsComponent.Builder(customBundleContentsComponent);
		if (clickType == ClickType.LEFT && !itemStack.isEmpty()) {
			if (builder.add(slot, player) > 0) {
				playInsertSound(player);
			} else {
				playInsertFailSound(player);
			}

			stack.set(BundleAPIDataComponentTypes.CUSTOM_BUNDLE_CONTENTS_COMPONENT, builder.build());
			this.onContentChanged(player);
			return true;
		} else if (clickType == ClickType.RIGHT && itemStack.isEmpty()) {
			ItemStack itemStack2 = builder.removeSelected();
			if (itemStack2 != null) {
				ItemStack itemStack3 = slot.insertStack(itemStack2);
				if (itemStack3.getCount() > 0) {
					builder.add(itemStack3);
				} else {
					playRemoveOneSound(player);
				}
			}

			stack.set(BundleAPIDataComponentTypes.CUSTOM_BUNDLE_CONTENTS_COMPONENT, builder.build());
			this.onContentChanged(player);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean onClicked(ItemStack stack, ItemStack otherStack, Slot slot, ClickType clickType, PlayerEntity player, StackReference cursorStackReference) {
		if (clickType == ClickType.LEFT && otherStack.isEmpty()) {
			setSelectedStackIndex(stack, -1);
			return false;
		}

		CustomBundleContentsComponent customBundleContentsComponent = stack.get(BundleAPIDataComponentTypes.CUSTOM_BUNDLE_CONTENTS_COMPONENT);
		if (customBundleContentsComponent == null) {
			return false;
		}

		CustomBundleContentsComponent.Builder builder = new CustomBundleContentsComponent.Builder(customBundleContentsComponent);
		if (clickType == ClickType.LEFT && !otherStack.isEmpty()) {
			if (slot.canTakePartial(player) && builder.add(otherStack) > 0) {
				playInsertSound(player);
			} else {
				playInsertFailSound(player);
			}

			stack.set(BundleAPIDataComponentTypes.CUSTOM_BUNDLE_CONTENTS_COMPONENT, builder.build());
			this.onContentChanged(player);
			return true;
		} else if (clickType == ClickType.RIGHT && otherStack.isEmpty()) {
			if (slot.canTakePartial(player)) {
				ItemStack itemStack = builder.removeSelected();
				if (itemStack != null) {
					playRemoveOneSound(player);
					cursorStackReference.set(itemStack);
				}
			}

			stack.set(BundleAPIDataComponentTypes.CUSTOM_BUNDLE_CONTENTS_COMPONENT, builder.build());
			this.onContentChanged(player);
			return true;
		} else {
			setSelectedStackIndex(stack, -1);
			return false;
		}
	}

	@Override
	public ActionResult use(World world, PlayerEntity user, Hand hand) {
		user.setCurrentHand(hand);
		return ActionResult.SUCCESS;
	}

	private void dropContentsOnUse(World world, PlayerEntity player, ItemStack stack) {
		if (this.dropFirstBundledStack(stack, player)) {
			playDropContentsSound(world, player);
			player.incrementStat(Stats.USED.getOrCreateStat(this));
		}
	}

	@Override
	public boolean isItemBarVisible(ItemStack stack) {
		CustomBundleContentsComponent customBundleContentsComponent = stack.getOrDefault(BundleAPIDataComponentTypes.CUSTOM_BUNDLE_CONTENTS_COMPONENT, CustomBundleContentsComponent.DEFAULT);
		return customBundleContentsComponent.getOccupancy().compareTo(Fraction.ZERO) > 0;
	}

	@Override
	public int getItemBarStep(ItemStack stack) {
		CustomBundleContentsComponent customBundleContentsComponent = stack.getOrDefault(BundleAPIDataComponentTypes.CUSTOM_BUNDLE_CONTENTS_COMPONENT, CustomBundleContentsComponent.DEFAULT);
		return Math.min(1 + MathHelper.multiplyFraction(customBundleContentsComponent.getOccupancy(), 12), 13);
	}

	@Override
	public int getItemBarColor(ItemStack stack) {
		CustomBundleContentsComponent customBundleContentsComponent = stack.getOrDefault(BundleAPIDataComponentTypes.CUSTOM_BUNDLE_CONTENTS_COMPONENT, CustomBundleContentsComponent.DEFAULT);
		return customBundleContentsComponent.getOccupancy().compareTo(Fraction.ONE) >= 0 ? FULL_ITEM_BAR_COLOR : ITEM_BAR_COLOR;
	}

	public static void setSelectedStackIndex(ItemStack stack, int selectedStackIndex) {
		CustomBundleContentsComponent customBundleContentsComponent = stack.get(BundleAPIDataComponentTypes.CUSTOM_BUNDLE_CONTENTS_COMPONENT);
		if (customBundleContentsComponent != null) {
			CustomBundleContentsComponent.Builder builder = new CustomBundleContentsComponent.Builder(customBundleContentsComponent);
			builder.setSelectedStackIndex(selectedStackIndex);
			stack.set(BundleAPIDataComponentTypes.CUSTOM_BUNDLE_CONTENTS_COMPONENT, builder.build());
		}
	}

	public static boolean hasSelectedStack(ItemStack stack) {
		CustomBundleContentsComponent customBundleContentsComponent = stack.get(BundleAPIDataComponentTypes.CUSTOM_BUNDLE_CONTENTS_COMPONENT);
		return customBundleContentsComponent != null && customBundleContentsComponent.getSelectedStackIndex() != -1;
	}

	public static int getSelectedStackIndex(ItemStack stack) {
		CustomBundleContentsComponent customBundleContentsComponent = stack.getOrDefault(BundleAPIDataComponentTypes.CUSTOM_BUNDLE_CONTENTS_COMPONENT, CustomBundleContentsComponent.DEFAULT);
		return customBundleContentsComponent.getSelectedStackIndex();
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

	private boolean dropFirstBundledStack(ItemStack stack, PlayerEntity player) {
		CustomBundleContentsComponent customBundleContentsComponent = stack.get(BundleAPIDataComponentTypes.CUSTOM_BUNDLE_CONTENTS_COMPONENT);
		if (customBundleContentsComponent != null && !customBundleContentsComponent.isEmpty()) {
			Optional<ItemStack> optional = popFirstBundledStack(stack, player, customBundleContentsComponent);
			if (optional.isPresent()) {
				player.dropItem(optional.get(), true);
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	private static Optional<ItemStack> popFirstBundledStack(ItemStack stack, PlayerEntity player, CustomBundleContentsComponent contents) {
		CustomBundleContentsComponent.Builder builder = new CustomBundleContentsComponent.Builder(contents);
		ItemStack itemStack = builder.removeSelected();
		if (itemStack != null) {
			playRemoveOneSound(player);
			stack.set(BundleAPIDataComponentTypes.CUSTOM_BUNDLE_CONTENTS_COMPONENT, builder.build());
			return Optional.of(itemStack);
		} else {
			return Optional.empty();
		}
	}

	@Override
	public void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
		if (user instanceof PlayerEntity playerEntity) {
			int i = this.getMaxUseTime(stack, user);
			boolean bl = remainingUseTicks == i;
			if (bl || remainingUseTicks < i - 10 && remainingUseTicks % 2 == 0) {
				this.dropContentsOnUse(world, playerEntity, stack);
			}
		}
	}

	@Override
	public int getMaxUseTime(ItemStack stack, LivingEntity user) {
		return 200;
	}

	@Override
	public UseAction getUseAction(ItemStack stack) {
		return UseAction.BUNDLE;
	}

	@Override
	public Optional<TooltipData> getTooltipData(ItemStack stack) {
		TooltipDisplayComponent tooltipDisplayComponent = stack.getOrDefault(DataComponentTypes.TOOLTIP_DISPLAY, TooltipDisplayComponent.DEFAULT);
		return !tooltipDisplayComponent.shouldDisplay(BundleAPIDataComponentTypes.CUSTOM_BUNDLE_CONTENTS_COMPONENT)
				? Optional.empty()
				: Optional.ofNullable(stack.get(BundleAPIDataComponentTypes.CUSTOM_BUNDLE_CONTENTS_COMPONENT)).map(CustomBundleTooltipData::new);
	}

	@Override
	public void onItemEntityDestroyed(ItemEntity entity) {
		CustomBundleContentsComponent customBundleContentsComponent = entity.getStack().get(BundleAPIDataComponentTypes.CUSTOM_BUNDLE_CONTENTS_COMPONENT);
		if (customBundleContentsComponent != null) {
			entity.getStack().set(BundleAPIDataComponentTypes.CUSTOM_BUNDLE_CONTENTS_COMPONENT, CustomBundleContentsComponent.DEFAULT);
			ItemUsage.spawnItemContents(entity, customBundleContentsComponent.iterateCopy());
		}
	}

	private static void playRemoveOneSound(Entity entity) {
		entity.playSound(SoundEvents.ITEM_BUNDLE_REMOVE_ONE, 0.8F, 0.8F + entity.getEntityWorld().getRandom().nextFloat() * 0.4F);
	}

	private static void playInsertSound(Entity entity) {
		entity.playSound(SoundEvents.ITEM_BUNDLE_INSERT, 0.8F, 0.8F + entity.getEntityWorld().getRandom().nextFloat() * 0.4F);
	}

	private static void playInsertFailSound(Entity entity) {
		entity.playSound(SoundEvents.ITEM_BUNDLE_INSERT_FAIL, 1.0F, 1.0F);
	}

	private static void playDropContentsSound(World world, Entity entity) {
		world.playSound(
				null,
				entity.getBlockPos(),
				SoundEvents.ITEM_BUNDLE_DROP_CONTENTS,
				SoundCategory.PLAYERS,
				0.8F,
				0.8F + entity.getEntityWorld().getRandom().nextFloat() * 0.4F
		);
	}

	private void onContentChanged(PlayerEntity user) {
		ScreenHandler screenHandler = user.currentScreenHandler;
		if (screenHandler != null) {
			screenHandler.onContentChanged(user.getInventory());
		}
	}
}
