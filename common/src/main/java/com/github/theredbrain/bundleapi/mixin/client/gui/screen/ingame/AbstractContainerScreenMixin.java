package com.github.theredbrain.bundleapi.mixin.client.gui.screen.ingame;

import com.github.theredbrain.bundleapi.client.gui.tooltip.CustomBundleMouseActions;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.ItemSlotMouseAction;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin extends Screen {

	@Shadow
	protected abstract void addItemSlotMouseAction(ItemSlotMouseAction itemSlotMouseAction);

	protected AbstractContainerScreenMixin(Component title) {
		super(title);
	}

	@Inject(method = "init()V", at = @At("TAIL"))
	protected void bundleapi$init(CallbackInfo ci) {
		this.addItemSlotMouseAction(new CustomBundleMouseActions(this.minecraft));
	}
}
