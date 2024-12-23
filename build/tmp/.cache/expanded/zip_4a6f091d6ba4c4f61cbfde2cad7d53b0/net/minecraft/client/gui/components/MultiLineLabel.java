package net.minecraft.client.gui.components;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface MultiLineLabel {
    MultiLineLabel EMPTY = new MultiLineLabel() {
        @Override
        public void renderCentered(GuiGraphics p_283384_, int p_94395_, int p_94396_) {
        }

        @Override
        public void renderCentered(GuiGraphics p_283208_, int p_210825_, int p_210826_, int p_210827_, int p_210828_) {
        }

        @Override
        public void renderLeftAligned(GuiGraphics p_283077_, int p_94379_, int p_94380_, int p_282157_, int p_282742_) {
        }

        @Override
        public int renderLeftAlignedNoShadow(GuiGraphics p_283645_, int p_94389_, int p_94390_, int p_94391_, int p_94392_) {
            return p_94390_;
        }

        @Override
        public int getLineCount() {
            return 0;
        }

        @Override
        public int getWidth() {
            return 0;
        }
    };

    static MultiLineLabel create(Font pFont, Component... pComponents) {
        return create(pFont, Integer.MAX_VALUE, Integer.MAX_VALUE, pComponents);
    }

    static MultiLineLabel create(Font pFont, int pMaxWidth, Component... pComponents) {
        return create(pFont, pMaxWidth, Integer.MAX_VALUE, pComponents);
    }

    static MultiLineLabel create(Font pFont, Component pComponent, int pMaxWidth) {
        return create(pFont, pMaxWidth, Integer.MAX_VALUE, pComponent);
    }

    static MultiLineLabel create(final Font pFont, final int pMaxWidth, final int pMaxRows, final Component... pComponents) {
        return pComponents.length == 0 ? EMPTY : new MultiLineLabel() {
            @Nullable
            private List<MultiLineLabel.TextAndWidth> cachedTextAndWidth;
            @Nullable
            private Language splitWithLanguage;

            @Override
            public void renderCentered(GuiGraphics p_283492_, int p_283184_, int p_282078_) {
                this.renderCentered(p_283492_, p_283184_, p_282078_, 9, -1);
            }

            @Override
            public void renderCentered(GuiGraphics p_281603_, int p_281267_, int p_281819_, int p_281545_, int p_282780_) {
                int i = p_281819_;

                for (MultiLineLabel.TextAndWidth multilinelabel$textandwidth : this.getSplitMessage()) {
                    p_281603_.drawCenteredString(pFont, multilinelabel$textandwidth.text, p_281267_, i, p_282780_);
                    i += p_281545_;
                }
            }

            @Override
            public void renderLeftAligned(GuiGraphics p_282318_, int p_283665_, int p_283416_, int p_281919_, int p_281686_) {
                int i = p_283416_;

                for (MultiLineLabel.TextAndWidth multilinelabel$textandwidth : this.getSplitMessage()) {
                    p_282318_.drawString(pFont, multilinelabel$textandwidth.text, p_283665_, i, p_281686_);
                    i += p_281919_;
                }
            }

            @Override
            public int renderLeftAlignedNoShadow(GuiGraphics p_281782_, int p_282841_, int p_283554_, int p_282768_, int p_283499_) {
                int i = p_283554_;

                for (MultiLineLabel.TextAndWidth multilinelabel$textandwidth : this.getSplitMessage()) {
                    p_281782_.drawString(pFont, multilinelabel$textandwidth.text, p_282841_, i, p_283499_, false);
                    i += p_282768_;
                }

                return i;
            }

            private List<MultiLineLabel.TextAndWidth> getSplitMessage() {
                Language language = Language.getInstance();
                if (this.cachedTextAndWidth != null && language == this.splitWithLanguage) {
                    return this.cachedTextAndWidth;
                } else {
                    this.splitWithLanguage = language;
                    List<FormattedCharSequence> list = new ArrayList<>();

                    for (Component component : pComponents) {
                        list.addAll(pFont.split(component, pMaxWidth));
                    }

                    this.cachedTextAndWidth = new ArrayList<>();

                    for (FormattedCharSequence formattedcharsequence : list.subList(0, Math.min(list.size(), pMaxRows))) {
                        this.cachedTextAndWidth.add(new MultiLineLabel.TextAndWidth(formattedcharsequence, pFont.width(formattedcharsequence)));
                    }

                    return this.cachedTextAndWidth;
                }
            }

            @Override
            public int getLineCount() {
                return this.getSplitMessage().size();
            }

            @Override
            public int getWidth() {
                return Math.min(pMaxWidth, this.getSplitMessage().stream().mapToInt(MultiLineLabel.TextAndWidth::width).max().orElse(0));
            }
        };
    }

    void renderCentered(GuiGraphics pGuiGraphics, int pX, int pY);

    void renderCentered(GuiGraphics pGuiGraphics, int pX, int pY, int pLineHeight, int pColor);

    void renderLeftAligned(GuiGraphics pGuiGraphics, int pX, int pY, int pLineHeight, int pColor);

    int renderLeftAlignedNoShadow(GuiGraphics pGuiGraphics, int pX, int pY, int pLineHeight, int pColor);

    int getLineCount();

    int getWidth();

    @OnlyIn(Dist.CLIENT)
    public static record TextAndWidth(FormattedCharSequence text, int width) {
    }
}