package net.minecraft.client.gui.components;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class OptionsList extends ContainerObjectSelectionList<OptionsList.Entry> {
    private static final int BIG_BUTTON_WIDTH = 310;
    private static final int DEFAULT_ITEM_HEIGHT = 25;
    private final OptionsSubScreen screen;

    public OptionsList(Minecraft pMinecraft, int pWidth, OptionsSubScreen pScreen) {
        super(pMinecraft, pWidth, pScreen.layout.getContentHeight(), pScreen.layout.getHeaderHeight(), 25);
        this.centerListVertically = false;
        this.screen = pScreen;
    }

    public void addBig(OptionInstance<?> pOption) {
        this.addEntry(OptionsList.OptionEntry.big(this.minecraft.options, pOption, this.screen));
    }

    public void addSmall(OptionInstance<?>... pOptions) {
        for (int i = 0; i < pOptions.length; i += 2) {
            OptionInstance<?> optioninstance = i < pOptions.length - 1 ? pOptions[i + 1] : null;
            this.addEntry(OptionsList.OptionEntry.small(this.minecraft.options, pOptions[i], optioninstance, this.screen));
        }
    }

    public void addSmall(List<AbstractWidget> pOptions) {
        for (int i = 0; i < pOptions.size(); i += 2) {
            this.addSmall(pOptions.get(i), i < pOptions.size() - 1 ? pOptions.get(i + 1) : null);
        }
    }

    public void addSmall(AbstractWidget pLeftOption, @Nullable AbstractWidget pRightOption) {
        this.addEntry(OptionsList.Entry.small(pLeftOption, pRightOption, this.screen));
    }

    @Override
    public int getRowWidth() {
        return 310;
    }

    @Nullable
    public AbstractWidget findOption(OptionInstance<?> pOption) {
        for (OptionsList.Entry optionslist$entry : this.children()) {
            if (optionslist$entry instanceof OptionsList.OptionEntry optionslist$optionentry) {
                AbstractWidget abstractwidget = optionslist$optionentry.options.get(pOption);
                if (abstractwidget != null) {
                    return abstractwidget;
                }
            }
        }

        return null;
    }

    public void applyUnsavedChanges() {
        for (OptionsList.Entry optionslist$entry : this.children()) {
            if (optionslist$entry instanceof OptionsList.OptionEntry) {
                OptionsList.OptionEntry optionslist$optionentry = (OptionsList.OptionEntry)optionslist$entry;

                for (AbstractWidget abstractwidget : optionslist$optionentry.options.values()) {
                    if (abstractwidget instanceof OptionInstance.OptionInstanceSliderButton<?> optioninstancesliderbutton) {
                        optioninstancesliderbutton.applyUnsavedValue();
                    }
                }
            }
        }
    }

    public Optional<GuiEventListener> getMouseOver(double pMouseX, double pMouseY) {
        for (OptionsList.Entry optionslist$entry : this.children()) {
            for (GuiEventListener guieventlistener : optionslist$entry.children()) {
                if (guieventlistener.isMouseOver(pMouseX, pMouseY)) {
                    return Optional.of(guieventlistener);
                }
            }
        }

        return Optional.empty();
    }

    @OnlyIn(Dist.CLIENT)
    protected static class Entry extends ContainerObjectSelectionList.Entry<OptionsList.Entry> {
        private final List<AbstractWidget> children;
        private final Screen screen;
        private static final int X_OFFSET = 160;

        Entry(List<AbstractWidget> pChildren, Screen pScreen) {
            this.children = ImmutableList.copyOf(pChildren);
            this.screen = pScreen;
        }

        public static OptionsList.Entry big(List<AbstractWidget> pOptions, Screen pScreen) {
            return new OptionsList.Entry(pOptions, pScreen);
        }

        public static OptionsList.Entry small(AbstractWidget pLeftOption, @Nullable AbstractWidget pRightOption, Screen pScreen) {
            return pRightOption == null
                ? new OptionsList.Entry(ImmutableList.of(pLeftOption), pScreen)
                : new OptionsList.Entry(ImmutableList.of(pLeftOption, pRightOption), pScreen);
        }

        @Override
        public void render(
            GuiGraphics pGuiGraphics,
            int pIndex,
            int pTop,
            int pLeft,
            int pWidth,
            int pHeight,
            int pMouseX,
            int pMouseY,
            boolean pHovering,
            float pPartialTick
        ) {
            int i = 0;
            int j = this.screen.width / 2 - 155;

            for (AbstractWidget abstractwidget : this.children) {
                abstractwidget.setPosition(j + i, pTop);
                abstractwidget.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
                i += 160;
            }
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return this.children;
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return this.children;
        }
    }

    @OnlyIn(Dist.CLIENT)
    protected static class OptionEntry extends OptionsList.Entry {
        final Map<OptionInstance<?>, AbstractWidget> options;

        private OptionEntry(Map<OptionInstance<?>, AbstractWidget> pOptions, OptionsSubScreen pScreen) {
            super(ImmutableList.copyOf(pOptions.values()), pScreen);
            this.options = pOptions;
        }

        public static OptionsList.OptionEntry big(Options pOptions, OptionInstance<?> pOption, OptionsSubScreen pScreen) {
            return new OptionsList.OptionEntry(ImmutableMap.of(pOption, pOption.createButton(pOptions, 0, 0, 310)), pScreen);
        }

        public static OptionsList.OptionEntry small(
            Options pOptions, OptionInstance<?> pLeftOption, @Nullable OptionInstance<?> pRightOption, OptionsSubScreen pScreen
        ) {
            AbstractWidget abstractwidget = pLeftOption.createButton(pOptions);
            return pRightOption == null
                ? new OptionsList.OptionEntry(ImmutableMap.of(pLeftOption, abstractwidget), pScreen)
                : new OptionsList.OptionEntry(ImmutableMap.of(pLeftOption, abstractwidget, pRightOption, pRightOption.createButton(pOptions)), pScreen);
        }
    }
}