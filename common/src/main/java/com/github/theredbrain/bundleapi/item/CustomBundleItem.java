package com.github.theredbrain.bundleapi.item;

import com.github.theredbrain.bundleapi.BundleAPI;
import com.github.theredbrain.bundleapi.component.type.CustomBundleContentsComponent;
import com.github.theredbrain.bundleapi.item.tooltip.CustomBundleTooltipData;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.item.tooltip.TooltipData;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.ClickType;
import net.minecraft.util.Formatting;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.apache.commons.lang3.math.Fraction;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class CustomBundleItem extends Item {
	private static final int ITEM_BAR_COLOR = ColorHelper.fromFloats(1.0F, 0.4F, 0.4F, 1.0F);
	/**
	 * @deprecated Kept for binary compatibility. Since 1.21.4 item model properties are data-driven
	 * (see {@code bundleapi:custom_bundle/fullness}), so BundleAPI no longer iterates the instances
	 * to register model predicate providers.
	 */
	@Deprecated
	public final static HashSet<CustomBundleItem> instances = new HashSet<>();
	private final TagKey<Item> tag;

	public CustomBundleItem(@Nullable TagKey<Item> tag, Settings settings) {
		super(settings);
		this.tag = tag;
		instances.add(this);
	}

	public CustomBundleItem(Settings settings) {
		this(null, settings);
	}

	public static float getAmountFilled(ItemStack stack) {
		CustomBundleContentsComponent customBundleContentsComponent = stack.getOrDefault(BundleAPI.CUSTOM_BUNDLE_CONTENTS_COMPONENT, CustomBundleContentsComponent.DEFAULT);
		return customBundleContentsComponent.getOccupancy().floatValue();
	}

	@Override
	public boolean onStackClicked(ItemStack stack, Slot slot, ClickType clickType, PlayerEntity player) {
		if (clickType != ClickType.RIGHT) {
			return false;
		} else {
			CustomBundleContentsComponent customBundleContentsComponent = stack.get(BundleAPI.CUSTOM_BUNDLE_CONTENTS_COMPONENT);
			if (customBundleContentsComponent == null) {
				return false;
			} else {
				ItemStack itemStack = slot.getStack();
				CustomBundleContentsComponent.Builder builder = new CustomBundleContentsComponent.Builder(customBundleContentsComponent);
				if (itemStack.isEmpty()) {
					this.playRemoveOneSound(player);
					ItemStack itemStack2 = builder.removeFirst();
					if (itemStack2 != null) {
						ItemStack itemStack3 = slot.insertStack(itemStack2);
						builder.add(itemStack3);
					}
				} else if (itemStack.getItem().canBeNested() && (this.tag == null || itemStack.isIn(this.tag))) {
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
	public boolean onClicked(ItemStack stack, ItemStack otherStack, Slot slot, ClickType clickType, PlayerEntity player, StackReference cursorStackReference) {
		if (clickType == ClickType.RIGHT && slot.canTakePartial(player)) {
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
				} else if (this.tag == null || otherStack.isIn(this.tag)) {
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
	public ActionResult use(World world, PlayerEntity user, Hand hand) {
		ItemStack itemStack = user.getStackInHand(hand);
		if (dropAllBundledItems(itemStack, user)) {
			this.playDropContentsSound(user);
			user.incrementStat(Stats.USED.getOrCreateStat(this));
			return world.isClient() ? ActionResult.SUCCESS : ActionResult.SUCCESS_SERVER;
		} else {
			return ActionResult.FAIL;
		}
	}

	@Override
	public boolean isItemBarVisible(ItemStack stack) {
		CustomBundleContentsComponent customBundleContentsComponent = stack.getOrDefault(BundleAPI.CUSTOM_BUNDLE_CONTENTS_COMPONENT, CustomBundleContentsComponent.DEFAULT);
		return customBundleContentsComponent.getOccupancy().compareTo(Fraction.ZERO) > 0;
	}

	@Override
	public int getItemBarStep(ItemStack stack) {
		CustomBundleContentsComponent customBundleContentsComponent = stack.getOrDefault(BundleAPI.CUSTOM_BUNDLE_CONTENTS_COMPONENT, CustomBundleContentsComponent.DEFAULT);
		return Math.min(1 + MathHelper.multiplyFraction(customBundleContentsComponent.getOccupancy(), 12), 13);
	}

	@Override
	public int getItemBarColor(ItemStack stack) {
		return ITEM_BAR_COLOR;
	}

	private static boolean dropAllBundledItems(ItemStack stack, PlayerEntity player) {
		CustomBundleContentsComponent customBundleContentsComponent = stack.get(BundleAPI.CUSTOM_BUNDLE_CONTENTS_COMPONENT);
		if (customBundleContentsComponent != null && !customBundleContentsComponent.isEmpty()) {
			stack.set(BundleAPI.CUSTOM_BUNDLE_CONTENTS_COMPONENT, new CustomBundleContentsComponent.Builder(customBundleContentsComponent).clear().build());
			if (player instanceof ServerPlayerEntity) {
				customBundleContentsComponent.iterateCopy().forEach(stackx -> player.dropItem(stackx, true));
			}

			return true;
		} else {
			return false;
		}
	}

	@Override
	public Optional<TooltipData> getTooltipData(ItemStack stack) {
		TooltipDisplayComponent tooltipDisplayComponent = stack.getOrDefault(DataComponentTypes.TOOLTIP_DISPLAY, TooltipDisplayComponent.DEFAULT);
		return !tooltipDisplayComponent.shouldDisplay(BundleAPI.CUSTOM_BUNDLE_CONTENTS_COMPONENT)
			? Optional.empty()
			: Optional.ofNullable(stack.get(BundleAPI.CUSTOM_BUNDLE_CONTENTS_COMPONENT)).map(CustomBundleTooltipData::new);
	}

	@Override
	public void appendTooltip(ItemStack stack, TooltipContext context, TooltipDisplayComponent displayComponent, Consumer<Text> textConsumer, TooltipType type) {
		CustomBundleContentsComponent customBundleContentsComponent = stack.get(BundleAPI.CUSTOM_BUNDLE_CONTENTS_COMPONENT);
		if (customBundleContentsComponent != null) {
			int bundleMaxSize = customBundleContentsComponent.sizeMultiplier() * 64;
			int i = MathHelper.multiplyFraction(customBundleContentsComponent.getOccupancy(), bundleMaxSize);
			textConsumer.accept(Text.translatable("item.minecraft.bundle.fullness", i, bundleMaxSize).formatted(Formatting.GRAY));
		}
	}

	@Override
	public void onItemEntityDestroyed(ItemEntity entity) {
		CustomBundleContentsComponent customBundleContentsComponent = entity.getStack().get(BundleAPI.CUSTOM_BUNDLE_CONTENTS_COMPONENT);
		if (customBundleContentsComponent != null) {
			entity.getStack().set(BundleAPI.CUSTOM_BUNDLE_CONTENTS_COMPONENT, new CustomBundleContentsComponent.Builder(customBundleContentsComponent).clear().build());
			ItemUsage.spawnItemContents(entity, customBundleContentsComponent.iterateCopy());
		}
	}

	private void playRemoveOneSound(Entity entity) {
		entity.playSound(SoundEvents.ITEM_BUNDLE_REMOVE_ONE, 0.8F, 0.8F + entity.getEntityWorld().getRandom().nextFloat() * 0.4F);
	}

	private void playInsertSound(Entity entity) {
		entity.playSound(SoundEvents.ITEM_BUNDLE_INSERT, 0.8F, 0.8F + entity.getEntityWorld().getRandom().nextFloat() * 0.4F);
	}

	private void playDropContentsSound(Entity entity) {
		entity.playSound(SoundEvents.ITEM_BUNDLE_DROP_CONTENTS, 0.8F, 0.8F + entity.getEntityWorld().getRandom().nextFloat() * 0.4F);
	}
}
