package com.github.theredbrain.bundleapi.client.gui.tooltip;

import com.github.theredbrain.bundleapi.component.type.CustomBundleContentsComponent;
import com.github.theredbrain.bundleapi.registry.BundleAPIDataComponentTypes;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.tooltip.TooltipSubmenuHandler;
import net.minecraft.client.input.Scroller;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.BundleItemSelectedC2SPacket;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import org.joml.Vector2i;

@Environment(EnvType.CLIENT)
public class CustomBundleTooltipSubmenuHandler implements TooltipSubmenuHandler {
	private final MinecraftClient client;
	private final Scroller scroller;

	public CustomBundleTooltipSubmenuHandler(MinecraftClient client) {
		this.client = client;
		this.scroller = new Scroller();
	}

	@Override
	public boolean isApplicableTo(Slot slot) {
		return slot.getStack().contains(BundleAPIDataComponentTypes.CUSTOM_BUNDLE_CONTENTS_COMPONENT);
	}

	@Override
	public boolean onScroll(double horizontal, double vertical, int slotId, ItemStack item) {
		CustomBundleContentsComponent customBundleContentsComponent = item.getOrDefault(BundleAPIDataComponentTypes.CUSTOM_BUNDLE_CONTENTS_COMPONENT, CustomBundleContentsComponent.DEFAULT);
		// TODO disable scrolling
		int i = customBundleContentsComponent.getNumberOfStacksShown();
		if (i == 0) {
			return false;
		}

		Vector2i vector2i = this.scroller.update(horizontal, vertical);
		int j = vector2i.y == 0 ? -vector2i.x : vector2i.y;
		if (j != 0) {
			int k = customBundleContentsComponent.getSelectedStackIndex();
			int l = Scroller.scrollCycling(j, k, i);
			if (k != l) {
				this.sendPacket(item, slotId, l);
			}
		}

		return true;
	}

	@Override
	public void reset(Slot slot) {
		this.reset(slot.getStack(), slot.id);
	}

	@Override
	public void onMouseClick(Slot slot, SlotActionType actionType) {
		if (actionType == SlotActionType.QUICK_MOVE || actionType == SlotActionType.SWAP) {
			this.reset(slot.getStack(), slot.id);
		}
	}

	private void sendPacket(ItemStack item, int slotId, int selectedItemIndex) {
		CustomBundleContentsComponent customBundleContentsComponent = item.getOrDefault(BundleAPIDataComponentTypes.CUSTOM_BUNDLE_CONTENTS_COMPONENT, CustomBundleContentsComponent.DEFAULT);

		if (this.client.getNetworkHandler() != null && selectedItemIndex < customBundleContentsComponent.getNumberOfStacksShown()) {
			ClientPlayNetworkHandler clientPlayNetworkHandler = this.client.getNetworkHandler();
//			BundleItem.setSelectedStackIndex(item, selectedItemIndex);
			CustomBundleContentsComponent.Builder builder = new CustomBundleContentsComponent.Builder(customBundleContentsComponent);
			builder.setSelectedStackIndex(selectedItemIndex);
			item.set(BundleAPIDataComponentTypes.CUSTOM_BUNDLE_CONTENTS_COMPONENT, builder.build());

			// TODO send custom packet
			clientPlayNetworkHandler.sendPacket(new BundleItemSelectedC2SPacket(slotId, selectedItemIndex));
		}
	}

	public void reset(ItemStack item, int slotId) {
		this.sendPacket(item, slotId, -1);
	}
}
