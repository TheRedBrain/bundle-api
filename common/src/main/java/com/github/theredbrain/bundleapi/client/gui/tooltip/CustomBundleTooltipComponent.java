package com.github.theredbrain.bundleapi.client.gui.tooltip;

import com.github.theredbrain.bundleapi.component.type.CustomBundleContentsComponent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.HoveredTooltipPositioner;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.apache.commons.lang3.math.Fraction;
import org.jspecify.annotations.Nullable;

import java.util.List;

@Environment(EnvType.CLIENT)
public class CustomBundleTooltipComponent implements TooltipComponent {
	private static final Identifier BUNDLE_PROGRESS_BAR_BORDER_TEXTURE = Identifier.ofVanilla("container/bundle/bundle_progressbar_border");
	private static final Identifier BUNDLE_PROGRESS_BAR_FILL_TEXTURE = Identifier.ofVanilla("container/bundle/bundle_progressbar_fill");
	private static final Identifier BUNDLE_PROGRESS_BAR_FULL_TEXTURE = Identifier.ofVanilla("container/bundle/bundle_progressbar_full");
	private static final Identifier BUNDLE_SLOT_HIGHLIGHT_BACK_TEXTURE = Identifier.ofVanilla("container/bundle/slot_highlight_back");
	private static final Identifier BUNDLE_SLOT_HIGHLIGHT_FRONT_TEXTURE = Identifier.ofVanilla("container/bundle/slot_highlight_front");
	private static final Identifier BUNDLE_SLOT_BACKGROUND_TEXTURE = Identifier.ofVanilla("container/bundle/slot_background");
	private static final Text BUNDLE_FULL = Text.translatable("item.minecraft.bundle.full");
	private static final Text BUNDLE_EMPTY = Text.translatable("item.minecraft.bundle.empty");
	private static final Text BUNDLE_EMPTY_DESCRIPTION = Text.translatable("item.minecraft.bundle.empty.description");
	private final CustomBundleContentsComponent customBundleContents;

	public CustomBundleTooltipComponent(CustomBundleContentsComponent customBundleContents) {
		this.customBundleContents = customBundleContents;
	}

	@Override
	public int getHeight(TextRenderer textRenderer) {
		return this.customBundleContents.isEmpty() ? getHeightOfEmpty(textRenderer) : this.getHeightOfNonEmpty();
	}

	@Override
	public int getWidth(TextRenderer textRenderer) {
		return 96;
	}

	@Override
	public boolean isSticky() {
		return true;
	}

	private static int getHeightOfEmpty(TextRenderer textRenderer) {
		return getDescriptionHeight(textRenderer) + 13 + 8;
	}

	private int getHeightOfNonEmpty() {
		return this.getRowsHeight() + 13 + 8;
	}

	private int getRowsHeight() {
		return this.getRows() * 24;
	}

	private int getXMargin(int width) {
		return (width - 96) / 2;
	}

	private int getRows() {
		return MathHelper.ceilDiv(this.getNumVisibleSlots(), 4);
	}

	private int getNumVisibleSlots() {
		return Math.min(12, this.customBundleContents.size());
	}

	@Override
	public void drawItems(TextRenderer textRenderer, int x, int y, int width, int height, DrawContext context) {
		if (this.customBundleContents.isEmpty()) {
			this.drawEmptyTooltip(textRenderer, x, y, width, height, context);
		} else {
			this.drawNonEmptyTooltip(textRenderer, x, y, width, height, context);
		}
	}

	private void drawEmptyTooltip(TextRenderer textRenderer, int x, int y, int width, int height, DrawContext context) {
		drawEmptyDescription(x + this.getXMargin(width), y, textRenderer, context);
		this.drawProgressBar(x + this.getXMargin(width), y + getDescriptionHeight(textRenderer) + 4, textRenderer, context);
	}

	private void drawNonEmptyTooltip(TextRenderer textRenderer, int x, int y, int width, int height, DrawContext context) {
		boolean bl = this.customBundleContents.size() > 12;
		List<ItemStack> list = this.firstStacksInContents(this.customBundleContents.getNumberOfStacksShown());
		int i = x + this.getXMargin(width) + 96;
		int j = y + this.getRows() * 24;
		int k = 1;

		for (int l = 1; l <= this.getRows(); l++) {
			for (int m = 1; m <= 4; m++) {
				int n = i - m * 24;
				int o = j - l * 24;
				if (shouldDrawExtraItemsCount(bl, m, l)) {
					drawExtraItemsCount(n, o, this.numContentItemsAfter(list), textRenderer, context);
				} else if (shouldDrawItem(list, k)) {
					this.drawItem(k, n, o, list, k, textRenderer, context);
					k++;
				}
			}
		}

		this.drawSelectedItemTooltip(textRenderer, context, x, y, width);
		this.drawProgressBar(x + this.getXMargin(width), y + this.getRowsHeight() + 4, textRenderer, context);
	}

	private List<ItemStack> firstStacksInContents(int numberOfStacksShown) {
		int i = Math.min(this.customBundleContents.size(), numberOfStacksShown);
		return this.customBundleContents.stream().toList().subList(0, i);
	}

	private static boolean shouldDrawExtraItemsCount(boolean hasMoreItems, int column, int row) {
		return hasMoreItems && column * row == 1;
	}

	private static boolean shouldDrawItem(List<ItemStack> items, int itemIndex) {
		return items.size() >= itemIndex;
	}

	private int numContentItemsAfter(List<ItemStack> items) {
		return this.customBundleContents.stream().skip(items.size()).mapToInt(ItemStack::getCount).sum();
	}

	private void drawItem(int index, int x, int y, List<ItemStack> stacks, int seed, TextRenderer textRenderer, DrawContext drawContext) {
		int i = stacks.size() - index;
		boolean bl = i == this.customBundleContents.getSelectedStackIndex();
		ItemStack itemStack = stacks.get(i);
		if (bl) {
			drawContext.drawGuiTexture(RenderPipelines.GUI_TEXTURED, BUNDLE_SLOT_HIGHLIGHT_BACK_TEXTURE, x, y, 24, 24);
		} else {
			drawContext.drawGuiTexture(RenderPipelines.GUI_TEXTURED, BUNDLE_SLOT_BACKGROUND_TEXTURE, x, y, 24, 24);
		}

		drawContext.drawItem(itemStack, x + 4, y + 4, seed);
		drawContext.drawStackOverlay(textRenderer, itemStack, x + 4, y + 4);
		if (bl) {
			drawContext.drawGuiTexture(RenderPipelines.GUI_TEXTURED, BUNDLE_SLOT_HIGHLIGHT_FRONT_TEXTURE, x, y, 24, 24);
		}
	}

	private static void drawExtraItemsCount(int x, int y, int numExtra, TextRenderer textRenderer, DrawContext drawContext) {
		drawContext.drawCenteredTextWithShadow(textRenderer, "+" + numExtra, x + 12, y + 10, -1);
	}

	private void drawSelectedItemTooltip(TextRenderer textRenderer, DrawContext drawContext, int x, int y, int width) {
		if (this.customBundleContents.hasSelectedStack()) {
			ItemStack itemStack = this.customBundleContents.get(this.customBundleContents.getSelectedStackIndex());
			Text text = itemStack.getFormattedName();
			int i = textRenderer.getWidth(text.asOrderedText());
			int j = x + width / 2 - 12;
			TooltipComponent tooltipComponent = TooltipComponent.of(text.asOrderedText());
			drawContext.drawTooltipImmediately(
					textRenderer, List.of(tooltipComponent), j - i / 2, y - 15, HoveredTooltipPositioner.INSTANCE, itemStack.get(DataComponentTypes.TOOLTIP_STYLE)
			);
		}
	}

	private void drawProgressBar(int x, int y, TextRenderer textRenderer, DrawContext drawContext) {
		drawContext.drawGuiTexture(RenderPipelines.GUI_TEXTURED, this.getProgressBarFillTexture(), x + 1, y, this.getProgressBarFill(), 13);
		drawContext.drawGuiTexture(RenderPipelines.GUI_TEXTURED, BUNDLE_PROGRESS_BAR_BORDER_TEXTURE, x, y, 96, 13);
		Text text = this.getProgressBarLabel();
		if (text != null) {
			drawContext.drawCenteredTextWithShadow(textRenderer, text, x + 48, y + 3, -1);
		}
	}

	private static void drawEmptyDescription(int x, int y, TextRenderer textRenderer, DrawContext drawContext) {
		drawContext.drawWrappedTextWithShadow(textRenderer, BUNDLE_EMPTY_DESCRIPTION, x, y, 96, -5592406);
	}

	private static int getDescriptionHeight(TextRenderer textRenderer) {
		return textRenderer.wrapLines(BUNDLE_EMPTY_DESCRIPTION, 96).size() * 9;
	}

	private int getProgressBarFill() {
		return MathHelper.clamp(MathHelper.multiplyFraction(this.customBundleContents.getOccupancy(), 94), 0, 94);
	}

	private Identifier getProgressBarFillTexture() {
		return this.customBundleContents.getOccupancy().compareTo(Fraction.ONE) >= 0 ? BUNDLE_PROGRESS_BAR_FULL_TEXTURE : BUNDLE_PROGRESS_BAR_FILL_TEXTURE;
	}

	private @Nullable Text getProgressBarLabel() {
		if (this.customBundleContents.isEmpty()) {
			return BUNDLE_EMPTY;
		} else {
			return this.customBundleContents.getOccupancy().compareTo(Fraction.ONE) >= 0 ? BUNDLE_FULL : null;
		}
	}
}
