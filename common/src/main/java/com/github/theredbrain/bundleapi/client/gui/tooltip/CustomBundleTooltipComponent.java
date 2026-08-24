package com.github.theredbrain.bundleapi.client.gui.tooltip;

import com.github.theredbrain.bundleapi.component.type.CustomBundleContentsComponent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.apache.commons.lang3.math.Fraction;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Mirrors vanilla {@code BundleTooltipComponent} (1.21.4+ bundle tooltip layout: a grid of
 * 24x24 slots plus an occupancy progress bar), but reads a {@link CustomBundleContentsComponent}.
 * Unlike vanilla bundles, custom bundles have no "selected stack", so no selection highlight is drawn.
 */
@Environment(EnvType.CLIENT)
public class CustomBundleTooltipComponent implements TooltipComponent {
	private static final Identifier BUNDLE_PROGRESS_BAR_BORDER_TEXTURE = Identifier.ofVanilla("container/bundle/bundle_progressbar_border");
	private static final Identifier BUNDLE_PROGRESS_BAR_FILL_TEXTURE = Identifier.ofVanilla("container/bundle/bundle_progressbar_fill");
	private static final Identifier BUNDLE_PROGRESS_BAR_FULL_TEXTURE = Identifier.ofVanilla("container/bundle/bundle_progressbar_full");
	private static final Identifier BUNDLE_SLOT_BACKGROUND_TEXTURE = Identifier.ofVanilla("container/bundle/slot_background");
	private static final int SLOTS_PER_ROW = 4;
	private static final int SLOT_DIMENSION = 24;
	private static final int ROW_WIDTH = 96;
	private static final int PROGRESS_BAR_HEIGHT = 13;
	private static final int PROGRESS_BAR_WIDTH = 94;
	private static final int MAX_SLOTS_SHOWN = 12;
	private static final int MAX_SLOTS_SHOWN_WHEN_TOO_MANY_TYPES = 11;
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
		return ROW_WIDTH;
	}

	@Override
	public boolean isSticky() {
		return true;
	}

	private static int getHeightOfEmpty(TextRenderer textRenderer) {
		return getDescriptionHeight(textRenderer) + PROGRESS_BAR_HEIGHT + 8;
	}

	private int getHeightOfNonEmpty() {
		return this.getRowsHeight() + PROGRESS_BAR_HEIGHT + 8;
	}

	private int getRowsHeight() {
		return this.getRows() * SLOT_DIMENSION;
	}

	private int getXMargin(int width) {
		return (width - ROW_WIDTH) / 2;
	}

	private int getRows() {
		return MathHelper.ceilDiv(this.getNumVisibleSlots(), SLOTS_PER_ROW);
	}

	private int getNumVisibleSlots() {
		return Math.min(MAX_SLOTS_SHOWN, this.customBundleContents.size());
	}

	/**
	 * Mirrors {@code BundleContentsComponent#getNumberOfStacksShown}.
	 */
	private int getNumberOfStacksShown() {
		int i = this.customBundleContents.size();
		int j = i > MAX_SLOTS_SHOWN ? MAX_SLOTS_SHOWN_WHEN_TOO_MANY_TYPES : MAX_SLOTS_SHOWN;
		int k = i % SLOTS_PER_ROW;
		int l = k == 0 ? 0 : SLOTS_PER_ROW - k;
		return Math.min(i, j - l);
	}

	@Override
	public void drawItems(TextRenderer textRenderer, int x, int y, int width, int height, DrawContext context) {
		if (this.customBundleContents.isEmpty()) {
			this.drawEmptyTooltip(textRenderer, x, y, width, context);
		} else {
			this.drawNonEmptyTooltip(textRenderer, x, y, width, context);
		}
	}

	private void drawEmptyTooltip(TextRenderer textRenderer, int x, int y, int width, DrawContext context) {
		drawEmptyDescription(x + this.getXMargin(width), y, textRenderer, context);
		this.drawProgressBar(x + this.getXMargin(width), y + getDescriptionHeight(textRenderer) + 4, textRenderer, context);
	}

	private void drawNonEmptyTooltip(TextRenderer textRenderer, int x, int y, int width, DrawContext context) {
		boolean bl = this.customBundleContents.size() > MAX_SLOTS_SHOWN;
		List<ItemStack> list = this.firstStacksInContents(this.getNumberOfStacksShown());
		int i = x + this.getXMargin(width) + ROW_WIDTH;
		int j = y + this.getRows() * SLOT_DIMENSION;
		int k = 1;

		for (int l = 1; l <= this.getRows(); l++) {
			for (int m = 1; m <= SLOTS_PER_ROW; m++) {
				int n = i - m * SLOT_DIMENSION;
				int o = j - l * SLOT_DIMENSION;
				if (shouldDrawExtraItemsCount(bl, m, l)) {
					drawExtraItemsCount(n, o, this.numContentItemsAfter(list), textRenderer, context);
				} else if (shouldDrawItem(list, k)) {
					drawItem(k, n, o, list, k, textRenderer, context);
					k++;
				}
			}
		}

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

	private static void drawItem(int index, int x, int y, List<ItemStack> stacks, int seed, TextRenderer textRenderer, DrawContext drawContext) {
		int i = stacks.size() - index;
		ItemStack itemStack = stacks.get(i);
		drawContext.drawGuiTexture(RenderPipelines.GUI_TEXTURED, BUNDLE_SLOT_BACKGROUND_TEXTURE, x, y, SLOT_DIMENSION, SLOT_DIMENSION);
		drawContext.drawItem(itemStack, x + 4, y + 4, seed);
		drawContext.drawStackOverlay(textRenderer, itemStack, x + 4, y + 4);
	}

	private static void drawExtraItemsCount(int x, int y, int numExtra, TextRenderer textRenderer, DrawContext drawContext) {
		drawContext.drawCenteredTextWithShadow(textRenderer, "+" + numExtra, x + 12, y + 10, -1);
	}

	private void drawProgressBar(int x, int y, TextRenderer textRenderer, DrawContext drawContext) {
		drawContext.drawGuiTexture(RenderPipelines.GUI_TEXTURED, this.getProgressBarFillTexture(), x + 1, y, this.getProgressBarFill(), PROGRESS_BAR_HEIGHT);
		drawContext.drawGuiTexture(RenderPipelines.GUI_TEXTURED, BUNDLE_PROGRESS_BAR_BORDER_TEXTURE, x, y, ROW_WIDTH, PROGRESS_BAR_HEIGHT);
		Text text = this.getProgressBarLabel();
		if (text != null) {
			drawContext.drawCenteredTextWithShadow(textRenderer, text, x + 48, y + 3, -1);
		}
	}

	private static void drawEmptyDescription(int x, int y, TextRenderer textRenderer, DrawContext drawContext) {
		drawContext.drawWrappedTextWithShadow(textRenderer, BUNDLE_EMPTY_DESCRIPTION, x, y, ROW_WIDTH, -5592406);
	}

	private static int getDescriptionHeight(TextRenderer textRenderer) {
		return textRenderer.wrapLines(BUNDLE_EMPTY_DESCRIPTION, ROW_WIDTH).size() * 9;
	}

	private int getProgressBarFill() {
		return MathHelper.clamp(MathHelper.multiplyFraction(this.customBundleContents.getOccupancy(), PROGRESS_BAR_WIDTH), 0, PROGRESS_BAR_WIDTH);
	}

	private Identifier getProgressBarFillTexture() {
		return this.customBundleContents.getOccupancy().compareTo(Fraction.ONE) >= 0 ? BUNDLE_PROGRESS_BAR_FULL_TEXTURE : BUNDLE_PROGRESS_BAR_FILL_TEXTURE;
	}

	@Nullable
	private Text getProgressBarLabel() {
		if (this.customBundleContents.isEmpty()) {
			return BUNDLE_EMPTY;
		} else {
			return this.customBundleContents.getOccupancy().compareTo(Fraction.ONE) >= 0 ? BUNDLE_FULL : null;
		}
	}
}
