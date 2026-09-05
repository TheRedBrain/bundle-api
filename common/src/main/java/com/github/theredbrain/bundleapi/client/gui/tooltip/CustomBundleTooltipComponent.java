package com.github.theredbrain.bundleapi.client.gui.tooltip;

import com.github.theredbrain.bundleapi.component.type.CustomBundleContentsComponent;
import com.github.theredbrain.bundleapi.item.tooltip.CustomBundleTooltipData;
import org.apache.commons.lang3.math.Fraction;
import org.jspecify.annotations.Nullable;

import java.util.List;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

/**
 * Mirrors vanilla {@code ClientBundleTooltip} (1.21.4+ bundle tooltip layout: a grid of
 * 24x24 slots plus an occupancy progress bar, extracted through {@link GuiGraphicsExtractor} since 26.1),
 * but reads a {@link CustomBundleContentsComponent}.
 * Unlike vanilla bundles, custom bundles have no "selected stack", so no selection highlight is drawn.
 */
public class CustomBundleTooltipComponent implements ClientTooltipComponent {
	private static final Identifier BUNDLE_PROGRESS_BAR_BORDER_TEXTURE = Identifier.withDefaultNamespace("container/bundle/bundle_progressbar_border");
	private static final Identifier BUNDLE_PROGRESS_BAR_FILL_TEXTURE = Identifier.withDefaultNamespace("container/bundle/bundle_progressbar_fill");
	private static final Identifier BUNDLE_PROGRESS_BAR_FULL_TEXTURE = Identifier.withDefaultNamespace("container/bundle/bundle_progressbar_full");
	private static final Identifier BUNDLE_SLOT_BACKGROUND_TEXTURE = Identifier.withDefaultNamespace("container/bundle/slot_background");
	private static final int SLOTS_PER_ROW = 4;
	private static final int SLOT_DIMENSION = 24;
	private static final int ROW_WIDTH = 96;
	private static final int PROGRESS_BAR_HEIGHT = 13;
	private static final int PROGRESS_BAR_WIDTH = 94;
	private static final int MAX_SLOTS_SHOWN = 12;
	private static final int MAX_SLOTS_SHOWN_WHEN_TOO_MANY_TYPES = 11;
	private static final Component BUNDLE_FULL = Component.translatable("item.minecraft.bundle.full");
	private static final Component BUNDLE_EMPTY = Component.translatable("item.minecraft.bundle.empty");
	private final CustomBundleContentsComponent customBundleContents;
	private final Component emptyDescription;

	public CustomBundleTooltipComponent(CustomBundleContentsComponent customBundleContents, Component emptyDescription) {
		this.customBundleContents = customBundleContents;
		this.emptyDescription = emptyDescription;
	}

	/**
	 * @deprecated Kept for binary compatibility, uses {@link CustomBundleTooltipData#DEFAULT_EMPTY_DESCRIPTION}.
	 */
	@Deprecated
	public CustomBundleTooltipComponent(CustomBundleContentsComponent customBundleContents) {
		this(customBundleContents, CustomBundleTooltipData.DEFAULT_EMPTY_DESCRIPTION);
	}

	@Override
	public int getHeight(Font textRenderer) {
		return this.customBundleContents.isEmpty() ? this.getHeightOfEmpty(textRenderer) : this.getHeightOfNonEmpty();
	}

	@Override
	public int getWidth(Font textRenderer) {
		return ROW_WIDTH;
	}

	@Override
	public boolean showTooltipWithItemInHand() {
		return true;
	}

	private int getHeightOfEmpty(Font textRenderer) {
		return this.getDescriptionHeight(textRenderer) + PROGRESS_BAR_HEIGHT + 8;
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
		return Mth.positiveCeilDiv(this.getNumVisibleSlots(), SLOTS_PER_ROW);
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
	public void extractImage(Font textRenderer, int x, int y, int width, int height, GuiGraphicsExtractor context) {
		if (this.customBundleContents.isEmpty()) {
			this.drawEmptyTooltip(textRenderer, x, y, width, context);
		} else {
			this.drawNonEmptyTooltip(textRenderer, x, y, width, context);
		}
	}

	private void drawEmptyTooltip(Font textRenderer, int x, int y, int width, GuiGraphicsExtractor context) {
		this.drawEmptyDescription(x + this.getXMargin(width), y, textRenderer, context);
		this.drawProgressBar(x + this.getXMargin(width), y + this.getDescriptionHeight(textRenderer) + 4, textRenderer, context);
	}

	private void drawNonEmptyTooltip(Font textRenderer, int x, int y, int width, GuiGraphicsExtractor context) {
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

	private static void drawItem(int index, int x, int y, List<ItemStack> stacks, int seed, Font textRenderer, GuiGraphicsExtractor drawContext) {
		int i = stacks.size() - index;
		ItemStack itemStack = stacks.get(i);
		drawContext.blitSprite(RenderPipelines.GUI_TEXTURED, BUNDLE_SLOT_BACKGROUND_TEXTURE, x, y, SLOT_DIMENSION, SLOT_DIMENSION);
		drawContext.item(itemStack, x + 4, y + 4, seed);
		drawContext.itemDecorations(textRenderer, itemStack, x + 4, y + 4);
	}

	private static void drawExtraItemsCount(int x, int y, int numExtra, Font textRenderer, GuiGraphicsExtractor drawContext) {
		drawContext.centeredText(textRenderer, "+" + numExtra, x + 12, y + 10, -1);
	}

	private void drawProgressBar(int x, int y, Font textRenderer, GuiGraphicsExtractor drawContext) {
		drawContext.blitSprite(RenderPipelines.GUI_TEXTURED, this.getProgressBarFillTexture(), x + 1, y, this.getProgressBarFill(), PROGRESS_BAR_HEIGHT);
		drawContext.blitSprite(RenderPipelines.GUI_TEXTURED, BUNDLE_PROGRESS_BAR_BORDER_TEXTURE, x, y, ROW_WIDTH, PROGRESS_BAR_HEIGHT);
		Component text = this.getProgressBarLabel();
		if (text != null) {
			drawContext.centeredText(textRenderer, text, x + 48, y + 3, -1);
		}
	}

	private void drawEmptyDescription(int x, int y, Font textRenderer, GuiGraphicsExtractor drawContext) {
		drawContext.textWithWordWrap(textRenderer, this.emptyDescription, x, y, ROW_WIDTH, -5592406);
	}

	private int getDescriptionHeight(Font textRenderer) {
		return textRenderer.split(this.emptyDescription, ROW_WIDTH).size() * 9;
	}

	private int getProgressBarFill() {
		return Mth.clamp(Mth.mulAndTruncate(this.customBundleContents.getOccupancy(), PROGRESS_BAR_WIDTH), 0, PROGRESS_BAR_WIDTH);
	}

	private Identifier getProgressBarFillTexture() {
		return this.customBundleContents.getOccupancy().compareTo(Fraction.ONE) >= 0 ? BUNDLE_PROGRESS_BAR_FULL_TEXTURE : BUNDLE_PROGRESS_BAR_FILL_TEXTURE;
	}

	private @Nullable Component getProgressBarLabel() {
		if (this.customBundleContents.isEmpty()) {
			return BUNDLE_EMPTY;
		} else {
			return this.customBundleContents.getOccupancy().compareTo(Fraction.ONE) >= 0 ? BUNDLE_FULL : null;
		}
	}
}
