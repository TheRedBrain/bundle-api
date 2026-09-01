package com.github.theredbrain.bundleapi.mixin.client.gui.screen.ingame;

import com.github.theredbrain.bundleapi.client.gui.tooltip.CustomBundleTooltipSubmenuHandler;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.tooltip.TooltipSubmenuHandler;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin extends Screen {

	@Shadow
	protected abstract void addTooltipSubmenuHandler(TooltipSubmenuHandler handler);

	protected HandledScreenMixin(Text title) {
		super(title);
	}

	@Inject(method = "init()V", at = @At("TAIL"))
	protected void bundleapi$init(CallbackInfo ci) {
		this.addTooltipSubmenuHandler(new CustomBundleTooltipSubmenuHandler(this.client));
	}
}
