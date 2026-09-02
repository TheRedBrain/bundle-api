package com.github.theredbrain.bundleapi.client.gui.tooltip;

import com.github.theredbrain.bundleapi.BundleAPI;
import com.github.theredbrain.bundleapi.component.type.CustomBundleContentsComponent;
import com.github.theredbrain.bundleapi.registry.BundleAPIDataComponentTypes;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ScrollWheelHandler;
import net.minecraft.client.gui.ItemSlotMouseAction;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ServerboundSelectBundleItemPacket;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector2i;

@Environment(EnvType.CLIENT)
public class CustomBundleMouseActions implements ItemSlotMouseAction {
	private final Minecraft client;
	private final ScrollWheelHandler scroller;

	public CustomBundleMouseActions(Minecraft client) {
		this.client = client;
		this.scroller = new ScrollWheelHandler();
	}

	@Override
	public boolean matches(Slot slot) {
		return slot.getItem().has(BundleAPIDataComponentTypes.CUSTOM_BUNDLE_CONTENTS_COMPONENT);
	}

	@Override
	public boolean onMouseScrolled(double horizontal, double vertical, int slotId, ItemStack item) {
		CustomBundleContentsComponent customBundleContentsComponent = item.getOrDefault(BundleAPIDataComponentTypes.CUSTOM_BUNDLE_CONTENTS_COMPONENT, CustomBundleContentsComponent.DEFAULT);
		// TODO disable scrolling
		int i = customBundleContentsComponent.getNumberOfItemsToShow();
		if (i == 0) {
			BundleAPI.LOGGER.info("CustomBundleMouseActions onMouseScrolled i == 0");
			return false;
		}

		Vector2i vector2i = this.scroller.onMouseScroll(horizontal, vertical);
		int j = vector2i.y == 0 ? -vector2i.x : vector2i.y;
		if (j != 0) {
			int k = customBundleContentsComponent.getSelectedItem();
			int l = ScrollWheelHandler.getNextScrollWheelSelection(j, k, i);
			if (k != l) {
				BundleAPI.LOGGER.info("CustomBundleMouseActions onMouseScrolled k != l");
				this.toggleSelectedBundleItem(item, slotId, l);
			}
		}

		return true;
	}

	@Override
	public void onStopHovering(Slot slot) {
		this.unselectedBundleItem(slot.getItem(), slot.index);
	}

	@Override
	public void onSlotClicked(Slot slot, ClickType actionType) {
		if (actionType == ClickType.QUICK_MOVE || actionType == ClickType.SWAP) {
			this.unselectedBundleItem(slot.getItem(), slot.index);
		}
	}

	private void toggleSelectedBundleItem(ItemStack item, int slotId, int selectedItemIndex) {
		CustomBundleContentsComponent customBundleContentsComponent = item.get(BundleAPIDataComponentTypes.CUSTOM_BUNDLE_CONTENTS_COMPONENT);

		if (this.client.getConnection() != null && customBundleContentsComponent != null && selectedItemIndex < customBundleContentsComponent.getNumberOfItemsToShow()) {
			ClientPacketListener clientPlayNetworkHandler = this.client.getConnection();
			CustomBundleContentsComponent.Mutable mutable = new CustomBundleContentsComponent.Mutable(customBundleContentsComponent);
			mutable.toggleSelectedItem(selectedItemIndex);
			item.set(BundleAPIDataComponentTypes.CUSTOM_BUNDLE_CONTENTS_COMPONENT, mutable.toImmutable());

			BundleAPI.LOGGER.info("CustomBundleMouseActions toggleSelectedBundleItem");
			clientPlayNetworkHandler.send(new ServerboundSelectBundleItemPacket(slotId, selectedItemIndex));
		}
	}

	public void unselectedBundleItem(ItemStack item, int slotId) {
		this.toggleSelectedBundleItem(item, slotId, -1);
	}
}
