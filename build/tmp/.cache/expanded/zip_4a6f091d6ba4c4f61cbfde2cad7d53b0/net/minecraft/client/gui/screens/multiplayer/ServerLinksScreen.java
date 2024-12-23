package net.minecraft.client.gui.screens.multiplayer;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.ServerLinks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ServerLinksScreen extends Screen {
    private static final int LINK_BUTTON_WIDTH = 310;
    private static final int DEFAULT_ITEM_HEIGHT = 25;
    private static final Component TITLE = Component.translatable("menu.server_links.title");
    private final Screen lastScreen;
    @Nullable
    private ServerLinksScreen.LinkList list;
    final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);
    final ServerLinks links;

    public ServerLinksScreen(Screen pLastScreen, ServerLinks pLinks) {
        super(TITLE);
        this.lastScreen = pLastScreen;
        this.links = pLinks;
    }

    @Override
    protected void init() {
        this.layout.addTitleHeader(this.title, this.font);
        this.list = this.layout.addToContents(new ServerLinksScreen.LinkList(this.minecraft, this.width, this));
        this.layout.addToFooter(Button.builder(CommonComponents.GUI_BACK, p_344159_ -> this.onClose()).width(200).build());
        this.layout.visitWidgets(p_345150_ -> {
            AbstractWidget abstractwidget = this.addRenderableWidget(p_345150_);
        });
        this.repositionElements();
    }

    @Override
    protected void repositionElements() {
        this.layout.arrangeElements();
        if (this.list != null) {
            this.list.updateSize(this.width, this.layout);
        }
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.lastScreen);
    }

    @OnlyIn(Dist.CLIENT)
    static class LinkList extends ContainerObjectSelectionList<ServerLinksScreen.LinkListEntry> {
        public LinkList(Minecraft pMinecraft, int pWidth, ServerLinksScreen pParent) {
            super(pMinecraft, pWidth, pParent.layout.getContentHeight(), pParent.layout.getHeaderHeight(), 25);
            pParent.links.entries().forEach(p_345105_ -> this.addEntry(new ServerLinksScreen.LinkListEntry(pParent, p_345105_)));
        }

        @Override
        public int getRowWidth() {
            return 310;
        }

        @Override
        public void updateSize(int pWidth, HeaderAndFooterLayout pLayout) {
            super.updateSize(pWidth, pLayout);
            int i = pWidth / 2 - 155;
            this.children().forEach(p_342392_ -> p_342392_.button.setX(i));
        }
    }

    @OnlyIn(Dist.CLIENT)
    static class LinkListEntry extends ContainerObjectSelectionList.Entry<ServerLinksScreen.LinkListEntry> {
        final AbstractWidget button;

        LinkListEntry(Screen pScreen, ServerLinks.Entry pEntry) {
            this.button = Button.builder(pEntry.displayName(), ConfirmLinkScreen.confirmLink(pScreen, pEntry.link(), false))
                .width(310)
                .build();
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
            this.button.setY(pTop);
            this.button.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return List.of(this.button);
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return List.of(this.button);
        }
    }
}