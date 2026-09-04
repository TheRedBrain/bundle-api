package com.github.theredbrain.bundleapi.client.gui.tooltip;

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
public class CustomBundleTooltipSubmenuHandler implements ItemSlotMouseAction {
	private final Minecraft client;
	private final ScrollWheelHandler scroller;

	public CustomBundleTooltipSubmenuHandler(Minecraft client) {
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
		int i = customBundleContentsComponent.getNumberOfStacksShown();
		if (i == 0) {
			return false;
		}

		Vector2i vector2i = this.scroller.onMouseScroll(horizontal, vertical);
		int j = vector2i.y == 0 ? -vector2i.x : vector2i.y;
		if (j != 0) {
			int k = customBundleContentsComponent.getSelectedStackIndex();
			int l = ScrollWheelHandler.getNextScrollWheelSelection(j, k, i);
			if (k != l) {
				this.sendPacket(item, slotId, l);
			}
		}

		return true;
	}

	@Override
	public void onStopHovering(Slot slot) {
		this.reset(slot.getItem(), slot.index);
	}

	@Override
	public void onSlotClicked(Slot slot, ClickType actionType) {
		if (actionType == ClickType.QUICK_MOVE || actionType == ClickType.SWAP) {
			this.reset(slot.getItem(), slot.index);
		}
	}

	private void sendPacket(ItemStack item, int slotId, int selectedItemIndex) {
		CustomBundleContentsComponent customBundleContentsComponent = item.getOrDefault(BundleAPIDataComponentTypes.CUSTOM_BUNDLE_CONTENTS_COMPONENT, CustomBundleContentsComponent.DEFAULT);

		if (this.client.getConnection() != null && selectedItemIndex < customBundleContentsComponent.getNumberOfStacksShown()) {
			ClientPacketListener clientPlayNetworkHandler = this.client.getConnection();
//			BundleItem.setSelectedStackIndex(item, selectedItemIndex);
			CustomBundleContentsComponent.Builder builder = new CustomBundleContentsComponent.Builder(customBundleContentsComponent);
			builder.setSelectedStackIndex(selectedItemIndex);
			item.set(BundleAPIDataComponentTypes.CUSTOM_BUNDLE_CONTENTS_COMPONENT, builder.build());

			// TODO send custom packet
			clientPlayNetworkHandler.send(new ServerboundSelectBundleItemPacket(slotId, selectedItemIndex));
		}
	}

	public void reset(ItemStack item, int slotId) {
		this.sendPacket(item, slotId, -1);
	}
}
