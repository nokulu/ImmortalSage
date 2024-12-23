package net.minecraft.client.gui.components.tabs;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.TabButton;
import net.minecraft.client.gui.components.events.AbstractContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TabNavigationBar extends AbstractContainerEventHandler implements Renderable, NarratableEntry {
    private static final int NO_TAB = -1;
    private static final int MAX_WIDTH = 400;
    private static final int HEIGHT = 24;
    private static final int MARGIN = 14;
    private static final Component USAGE_NARRATION = Component.translatable("narration.tab_navigation.usage");
    private final LinearLayout layout = LinearLayout.horizontal();
    private int width;
    private final TabManager tabManager;
    private final ImmutableList<Tab> tabs;
    private final ImmutableList<TabButton> tabButtons;

    TabNavigationBar(int pWidth, TabManager pTabManager, Iterable<Tab> pTabs) {
        this.width = pWidth;
        this.tabManager = pTabManager;
        this.tabs = ImmutableList.copyOf(pTabs);
        this.layout.defaultCellSetting().alignHorizontallyCenter();
        ImmutableList.Builder<TabButton> builder = ImmutableList.builder();

        for (Tab tab : pTabs) {
            builder.add(this.layout.addChild(new TabButton(pTabManager, tab, 0, 24)));
        }

        this.tabButtons = builder.build();
    }

    public static TabNavigationBar.Builder builder(TabManager pTabManager, int pWidth) {
        return new TabNavigationBar.Builder(pTabManager, pWidth);
    }

    public void setWidth(int pWidth) {
        this.width = pWidth;
    }

    @Override
    public void setFocused(boolean pFocused) {
        super.setFocused(pFocused);
        if (this.getFocused() != null) {
            this.getFocused().setFocused(pFocused);
        }
    }

    @Override
    public void setFocused(@Nullable GuiEventListener pListener) {
        super.setFocused(pListener);
        if (pListener instanceof TabButton tabbutton) {
            this.tabManager.setCurrentTab(tabbutton.tab(), true);
        }
    }

    @Nullable
    @Override
    public ComponentPath nextFocusPath(FocusNavigationEvent pEvent) {
        if (!this.isFocused()) {
            TabButton tabbutton = this.currentTabButton();
            if (tabbutton != null) {
                return ComponentPath.path(this, ComponentPath.leaf(tabbutton));
            }
        }

        return pEvent instanceof FocusNavigationEvent.TabNavigation ? null : super.nextFocusPath(pEvent);
    }

    @Override
    public List<? extends GuiEventListener> children() {
        return this.tabButtons;
    }

    @Override
    public NarratableEntry.NarrationPriority narrationPriority() {
        return this.tabButtons.stream().map(AbstractWidget::narrationPriority).max(Comparator.naturalOrder()).orElse(NarratableEntry.NarrationPriority.NONE);
    }

    @Override
    public void updateNarration(NarrationElementOutput pNarrationElementOutput) {
        Optional<TabButton> optional = this.tabButtons.stream().filter(AbstractWidget::isHovered).findFirst().or(() -> Optional.ofNullable(this.currentTabButton()));
        optional.ifPresent(p_274663_ -> {
            this.narrateListElementPosition(pNarrationElementOutput.nest(), p_274663_);
            p_274663_.updateNarration(pNarrationElementOutput);
        });
        if (this.isFocused()) {
            pNarrationElementOutput.add(NarratedElementType.USAGE, USAGE_NARRATION);
        }
    }

    protected void narrateListElementPosition(NarrationElementOutput pNarrationElementOutput, TabButton pTabButton) {
        if (this.tabs.size() > 1) {
            int i = this.tabButtons.indexOf(pTabButton);
            if (i != -1) {
                pNarrationElementOutput.add(NarratedElementType.POSITION, Component.translatable("narrator.position.tab", i + 1, this.tabs.size()));
            }
        }
    }

    @Override
    public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        RenderSystem.enableBlend();
        pGuiGraphics.blit(
            Screen.HEADER_SEPARATOR, 0, this.layout.getY() + this.layout.getHeight() - 2, 0.0F, 0.0F, this.tabButtons.get(0).getX(), 2, 32, 2
        );
        int i = this.tabButtons.get(this.tabButtons.size() - 1).getRight();
        pGuiGraphics.blit(Screen.HEADER_SEPARATOR, i, this.layout.getY() + this.layout.getHeight() - 2, 0.0F, 0.0F, this.width, 2, 32, 2);
        RenderSystem.disableBlend();

        for (TabButton tabbutton : this.tabButtons) {
            tabbutton.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        }
    }

    @Override
    public ScreenRectangle getRectangle() {
        return this.layout.getRectangle();
    }

    public void arrangeElements() {
        int i = Math.min(400, this.width) - 28;
        int j = Mth.roundToward(i / this.tabs.size(), 2);

        for (TabButton tabbutton : this.tabButtons) {
            tabbutton.setWidth(j);
        }

        this.layout.arrangeElements();
        this.layout.setX(Mth.roundToward((this.width - i) / 2, 2));
        this.layout.setY(0);
    }

    public void selectTab(int pIndex, boolean pPlayClickSound) {
        if (this.isFocused()) {
            this.setFocused(this.tabButtons.get(pIndex));
        } else {
            this.tabManager.setCurrentTab(this.tabs.get(pIndex), pPlayClickSound);
        }
    }

    public boolean keyPressed(int pKeycode) {
        if (Screen.hasControlDown()) {
            int i = this.getNextTabIndex(pKeycode);
            if (i != -1) {
                this.selectTab(Mth.clamp(i, 0, this.tabs.size() - 1), true);
                return true;
            }
        }

        return false;
    }

    private int getNextTabIndex(int pKeycode) {
        if (pKeycode >= 49 && pKeycode <= 57) {
            return pKeycode - 49;
        } else {
            if (pKeycode == 258) {
                int i = this.currentTabIndex();
                if (i != -1) {
                    int j = Screen.hasShiftDown() ? i - 1 : i + 1;
                    return Math.floorMod(j, this.tabs.size());
                }
            }

            return -1;
        }
    }

    private int currentTabIndex() {
        Tab tab = this.tabManager.getCurrentTab();
        int i = this.tabs.indexOf(tab);
        return i != -1 ? i : -1;
    }

    @Nullable
    private TabButton currentTabButton() {
        int i = this.currentTabIndex();
        return i != -1 ? this.tabButtons.get(i) : null;
    }

    /**
     * Builder class for creating a TabNavigationBar instance.
     */
    @OnlyIn(Dist.CLIENT)
    public static class Builder {
        private final int width;
        private final TabManager tabManager;
        private final List<Tab> tabs = new ArrayList<>();

        Builder(TabManager pTabManager, int pWidth) {
            this.tabManager = pTabManager;
            this.width = pWidth;
        }

        public TabNavigationBar.Builder addTabs(Tab... pTabs) {
            Collections.addAll(this.tabs, pTabs);
            return this;
        }

        public TabNavigationBar build() {
            return new TabNavigationBar(this.width, this.tabManager, this.tabs);
        }
    }
}