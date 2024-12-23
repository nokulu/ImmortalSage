package net.minecraft.client.gui.components;

import com.mojang.blaze3d.systems.RenderSystem;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class Checkbox extends AbstractButton {
    private static final ResourceLocation CHECKBOX_SELECTED_HIGHLIGHTED_SPRITE = ResourceLocation.withDefaultNamespace("widget/checkbox_selected_highlighted");
    private static final ResourceLocation CHECKBOX_SELECTED_SPRITE = ResourceLocation.withDefaultNamespace("widget/checkbox_selected");
    private static final ResourceLocation CHECKBOX_HIGHLIGHTED_SPRITE = ResourceLocation.withDefaultNamespace("widget/checkbox_highlighted");
    private static final ResourceLocation CHECKBOX_SPRITE = ResourceLocation.withDefaultNamespace("widget/checkbox");
    private static final int TEXT_COLOR = 14737632;
    private static final int SPACING = 4;
    private static final int BOX_PADDING = 8;
    private boolean selected;
    private final Checkbox.OnValueChange onValueChange;
    private final MultiLineTextWidget textWidget;

    Checkbox(int pX, int pY, int pMaxWidth, Component pMessage, Font pFont, boolean pSelected, Checkbox.OnValueChange pOnValueChange) {
        super(pX, pY, 0, 0, pMessage);
        this.width = this.getAdjustedWidth(pMaxWidth, pMessage, pFont);
        this.textWidget = new MultiLineTextWidget(pMessage, pFont).setMaxWidth(this.width).setColor(14737632);
        this.height = this.getAdjustedHeight(pFont);
        this.selected = pSelected;
        this.onValueChange = pOnValueChange;
    }

    private int getAdjustedWidth(int pMaxWidth, Component pMessage, Font pFont) {
        return Math.min(getDefaultWidth(pMessage, pFont), pMaxWidth);
    }

    private int getAdjustedHeight(Font pFont) {
        return Math.max(getBoxSize(pFont), this.textWidget.getHeight());
    }

    static int getDefaultWidth(Component pMessage, Font pFont) {
        return getBoxSize(pFont) + 4 + pFont.width(pMessage);
    }

    public static Checkbox.Builder builder(Component pMessage, Font pFont) {
        return new Checkbox.Builder(pMessage, pFont);
    }

    public static int getBoxSize(Font pFont) {
        return 9 + 8;
    }

    @Override
    public void onPress() {
        this.selected = !this.selected;
        this.onValueChange.onValueChange(this, this.selected);
    }

    public boolean selected() {
        return this.selected;
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput pNarrationElementOutput) {
        pNarrationElementOutput.add(NarratedElementType.TITLE, this.createNarrationMessage());
        if (this.active) {
            if (this.isFocused()) {
                pNarrationElementOutput.add(NarratedElementType.USAGE, Component.translatable("narration.checkbox.usage.focused"));
            } else {
                pNarrationElementOutput.add(NarratedElementType.USAGE, Component.translatable("narration.checkbox.usage.hovered"));
            }
        }
    }

    @Override
    public void renderWidget(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        Minecraft minecraft = Minecraft.getInstance();
        RenderSystem.enableDepthTest();
        Font font = minecraft.font;
        pGuiGraphics.setColor(1.0F, 1.0F, 1.0F, this.alpha);
        RenderSystem.enableBlend();
        ResourceLocation resourcelocation;
        if (this.selected) {
            resourcelocation = this.isFocused() ? CHECKBOX_SELECTED_HIGHLIGHTED_SPRITE : CHECKBOX_SELECTED_SPRITE;
        } else {
            resourcelocation = this.isFocused() ? CHECKBOX_HIGHLIGHTED_SPRITE : CHECKBOX_SPRITE;
        }

        int i = getBoxSize(font);
        pGuiGraphics.blitSprite(resourcelocation, this.getX(), this.getY(), i, i);
        int j = this.getX() + i + 4;
        int k = this.getY() + i / 2 - this.textWidget.getHeight() / 2;
        this.textWidget.setPosition(j, k);
        this.textWidget.renderWidget(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
    }

    @OnlyIn(Dist.CLIENT)
    public static class Builder {
        private final Component message;
        private final Font font;
        private int maxWidth;
        private int x = 0;
        private int y = 0;
        private Checkbox.OnValueChange onValueChange = Checkbox.OnValueChange.NOP;
        private boolean selected = false;
        @Nullable
        private OptionInstance<Boolean> option = null;
        @Nullable
        private Tooltip tooltip = null;

        Builder(Component pMessage, Font pFont) {
            this.message = pMessage;
            this.font = pFont;
            this.maxWidth = Checkbox.getDefaultWidth(pMessage, pFont);
        }

        public Checkbox.Builder pos(int pX, int pY) {
            this.x = pX;
            this.y = pY;
            return this;
        }

        public Checkbox.Builder onValueChange(Checkbox.OnValueChange pOnValueChange) {
            this.onValueChange = pOnValueChange;
            return this;
        }

        public Checkbox.Builder selected(boolean pSelected) {
            this.selected = pSelected;
            this.option = null;
            return this;
        }

        public Checkbox.Builder selected(OptionInstance<Boolean> pOption) {
            this.option = pOption;
            this.selected = pOption.get();
            return this;
        }

        public Checkbox.Builder tooltip(Tooltip pTooltip) {
            this.tooltip = pTooltip;
            return this;
        }

        public Checkbox.Builder maxWidth(int pMaxWidth) {
            this.maxWidth = pMaxWidth;
            return this;
        }

        public Checkbox build() {
            Checkbox.OnValueChange checkbox$onvaluechange = this.option == null ? this.onValueChange : (p_311135_, p_313032_) -> {
                this.option.set(p_313032_);
                this.onValueChange.onValueChange(p_311135_, p_313032_);
            };
            Checkbox checkbox = new Checkbox(
                this.x, this.y, this.maxWidth, this.message, this.font, this.selected, checkbox$onvaluechange
            );
            checkbox.setTooltip(this.tooltip);
            return checkbox;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public interface OnValueChange {
        Checkbox.OnValueChange NOP = (p_310417_, p_311975_) -> {
        };

        void onValueChange(Checkbox pCheckbox, boolean pValue);
    }
}