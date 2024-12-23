package com.mojang.realmsclient.gui.screens;

import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.RealmsMainScreen;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.PendingInvite;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.gui.RealmsDataFetcher;
import com.mojang.realmsclient.gui.RowButton;
import com.mojang.realmsclient.util.RealmsUtil;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.realms.RealmsObjectSelectionList;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class RealmsPendingInvitesScreen extends RealmsScreen {
    static final ResourceLocation ACCEPT_HIGHLIGHTED_SPRITE = ResourceLocation.withDefaultNamespace("pending_invite/accept_highlighted");
    static final ResourceLocation ACCEPT_SPRITE = ResourceLocation.withDefaultNamespace("pending_invite/accept");
    static final ResourceLocation REJECT_HIGHLIGHTED_SPRITE = ResourceLocation.withDefaultNamespace("pending_invite/reject_highlighted");
    static final ResourceLocation REJECT_SPRITE = ResourceLocation.withDefaultNamespace("pending_invite/reject");
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Component NO_PENDING_INVITES_TEXT = Component.translatable("mco.invites.nopending");
    static final Component ACCEPT_INVITE = Component.translatable("mco.invites.button.accept");
    static final Component REJECT_INVITE = Component.translatable("mco.invites.button.reject");
    private final Screen lastScreen;
    private final CompletableFuture<List<PendingInvite>> pendingInvites = CompletableFuture.supplyAsync(() -> {
        try {
            return RealmsClient.create().pendingInvites().pendingInvites;
        } catch (RealmsServiceException realmsserviceexception) {
            LOGGER.error("Couldn't list invites", (Throwable)realmsserviceexception);
            return List.of();
        }
    }, Util.ioPool());
    @Nullable
    Component toolTip;
    RealmsPendingInvitesScreen.PendingInvitationSelectionList pendingInvitationSelectionList;
    int selectedInvite = -1;
    private Button acceptButton;
    private Button rejectButton;

    public RealmsPendingInvitesScreen(Screen pLastScreen, Component pTitle) {
        super(pTitle);
        this.lastScreen = pLastScreen;
    }

    @Override
    public void init() {
        RealmsMainScreen.refreshPendingInvites();
        this.pendingInvitationSelectionList = new RealmsPendingInvitesScreen.PendingInvitationSelectionList();
        this.pendingInvites.thenAcceptAsync(p_296071_ -> {
            List<RealmsPendingInvitesScreen.Entry> list = p_296071_.stream().map(p_296073_ -> new RealmsPendingInvitesScreen.Entry(p_296073_)).toList();
            this.pendingInvitationSelectionList.replaceEntries(list);
            if (list.isEmpty()) {
                this.minecraft.getNarrator().say(NO_PENDING_INVITES_TEXT);
            }
        }, this.screenExecutor);
        this.addRenderableWidget(this.pendingInvitationSelectionList);
        this.acceptButton = this.addRenderableWidget(Button.builder(ACCEPT_INVITE, p_296067_ -> {
            this.handleInvitation(this.selectedInvite, true);
            this.selectedInvite = -1;
            this.updateButtonStates();
        }).bounds(this.width / 2 - 174, this.height - 32, 100, 20).build());
        this.addRenderableWidget(
            Button.builder(CommonComponents.GUI_DONE, p_296072_ -> this.onClose())
                .bounds(this.width / 2 - 50, this.height - 32, 100, 20)
                .build()
        );
        this.rejectButton = this.addRenderableWidget(Button.builder(REJECT_INVITE, p_296070_ -> {
            this.handleInvitation(this.selectedInvite, false);
            this.selectedInvite = -1;
            this.updateButtonStates();
        }).bounds(this.width / 2 + 74, this.height - 32, 100, 20).build());
        this.updateButtonStates();
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.lastScreen);
    }

    void handleInvitation(int pIndex, boolean pShouldAccept) {
        if (pIndex < this.pendingInvitationSelectionList.getItemCount()) {
            String s = this.pendingInvitationSelectionList.children().get(pIndex).pendingInvite.invitationId;
            CompletableFuture.<Boolean>supplyAsync(() -> {
                try {
                    RealmsClient realmsclient = RealmsClient.create();
                    if (pShouldAccept) {
                        realmsclient.acceptInvitation(s);
                    } else {
                        realmsclient.rejectInvitation(s);
                    }

                    return true;
                } catch (RealmsServiceException realmsserviceexception) {
                    LOGGER.error("Couldn't handle invite", (Throwable)realmsserviceexception);
                    return false;
                }
            }, Util.ioPool()).thenAcceptAsync(p_296066_ -> {
                if (p_296066_) {
                    this.pendingInvitationSelectionList.removeAtIndex(pIndex);
                    RealmsDataFetcher realmsdatafetcher = this.minecraft.realmsDataFetcher();
                    if (pShouldAccept) {
                        realmsdatafetcher.serverListUpdateTask.reset();
                    }

                    realmsdatafetcher.pendingInvitesTask.reset();
                }
            }, this.screenExecutor);
        }
    }

    @Override
    public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        this.toolTip = null;
        pGuiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 12, -1);
        if (this.toolTip != null) {
            pGuiGraphics.renderTooltip(this.font, this.toolTip, pMouseX, pMouseY);
        }

        if (this.pendingInvites.isDone() && this.pendingInvitationSelectionList.getItemCount() == 0) {
            pGuiGraphics.drawCenteredString(this.font, NO_PENDING_INVITES_TEXT, this.width / 2, this.height / 2 - 20, -1);
        }
    }

    void updateButtonStates() {
        this.acceptButton.visible = this.shouldAcceptAndRejectButtonBeVisible(this.selectedInvite);
        this.rejectButton.visible = this.shouldAcceptAndRejectButtonBeVisible(this.selectedInvite);
    }

    private boolean shouldAcceptAndRejectButtonBeVisible(int pSelectedInvite) {
        return pSelectedInvite != -1;
    }

    @OnlyIn(Dist.CLIENT)
    class Entry extends ObjectSelectionList.Entry<RealmsPendingInvitesScreen.Entry> {
        private static final int TEXT_LEFT = 38;
        final PendingInvite pendingInvite;
        private final List<RowButton> rowButtons;

        Entry(final PendingInvite pPendingInvite) {
            this.pendingInvite = pPendingInvite;
            this.rowButtons = Arrays.asList(new RealmsPendingInvitesScreen.Entry.AcceptRowButton(), new RealmsPendingInvitesScreen.Entry.RejectRowButton());
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
            this.renderPendingInvitationItem(pGuiGraphics, this.pendingInvite, pLeft, pTop, pMouseX, pMouseY);
        }

        @Override
        public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
            RowButton.rowButtonMouseClicked(RealmsPendingInvitesScreen.this.pendingInvitationSelectionList, this, this.rowButtons, pButton, pMouseX, pMouseY);
            return super.mouseClicked(pMouseX, pMouseY, pButton);
        }

        private void renderPendingInvitationItem(GuiGraphics pGuiGraphics, PendingInvite pPendingInvite, int pX, int pY, int pMouseX, int pMouseY) {
            pGuiGraphics.drawString(RealmsPendingInvitesScreen.this.font, pPendingInvite.realmName, pX + 38, pY + 1, -1, false);
            pGuiGraphics.drawString(RealmsPendingInvitesScreen.this.font, pPendingInvite.realmOwnerName, pX + 38, pY + 12, 7105644, false);
            pGuiGraphics.drawString(
                RealmsPendingInvitesScreen.this.font, RealmsUtil.convertToAgePresentationFromInstant(pPendingInvite.date), pX + 38, pY + 24, 7105644, false
            );
            RowButton.drawButtonsInRow(pGuiGraphics, this.rowButtons, RealmsPendingInvitesScreen.this.pendingInvitationSelectionList, pX, pY, pMouseX, pMouseY);
            RealmsUtil.renderPlayerFace(pGuiGraphics, pX, pY, 32, pPendingInvite.realmOwnerUuid);
        }

        @Override
        public Component getNarration() {
            Component component = CommonComponents.joinLines(
                Component.literal(this.pendingInvite.realmName), Component.literal(this.pendingInvite.realmOwnerName), RealmsUtil.convertToAgePresentationFromInstant(this.pendingInvite.date)
            );
            return Component.translatable("narrator.select", component);
        }

        @OnlyIn(Dist.CLIENT)
        class AcceptRowButton extends RowButton {
            AcceptRowButton() {
                super(15, 15, 215, 5);
            }

            @Override
            protected void draw(GuiGraphics p_282151_, int p_283695_, int p_282436_, boolean p_282168_) {
                p_282151_.blitSprite(p_282168_ ? RealmsPendingInvitesScreen.ACCEPT_HIGHLIGHTED_SPRITE : RealmsPendingInvitesScreen.ACCEPT_SPRITE, p_283695_, p_282436_, 18, 18);
                if (p_282168_) {
                    RealmsPendingInvitesScreen.this.toolTip = RealmsPendingInvitesScreen.ACCEPT_INVITE;
                }
            }

            @Override
            public void onClick(int p_89029_) {
                RealmsPendingInvitesScreen.this.handleInvitation(p_89029_, true);
            }
        }

        @OnlyIn(Dist.CLIENT)
        class RejectRowButton extends RowButton {
            RejectRowButton() {
                super(15, 15, 235, 5);
            }

            @Override
            protected void draw(GuiGraphics p_282457_, int p_281421_, int p_281260_, boolean p_281476_) {
                p_282457_.blitSprite(p_281476_ ? RealmsPendingInvitesScreen.REJECT_HIGHLIGHTED_SPRITE : RealmsPendingInvitesScreen.REJECT_SPRITE, p_281421_, p_281260_, 18, 18);
                if (p_281476_) {
                    RealmsPendingInvitesScreen.this.toolTip = RealmsPendingInvitesScreen.REJECT_INVITE;
                }
            }

            @Override
            public void onClick(int p_89039_) {
                RealmsPendingInvitesScreen.this.handleInvitation(p_89039_, false);
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    class PendingInvitationSelectionList extends RealmsObjectSelectionList<RealmsPendingInvitesScreen.Entry> {
        public PendingInvitationSelectionList() {
            super(RealmsPendingInvitesScreen.this.width, RealmsPendingInvitesScreen.this.height - 72, 32, 36);
        }

        public void removeAtIndex(int pIndex) {
            this.remove(pIndex);
        }

        @Override
        public int getMaxPosition() {
            return this.getItemCount() * 36;
        }

        @Override
        public int getRowWidth() {
            return 260;
        }

        @Override
        public void selectItem(int pIndex) {
            super.selectItem(pIndex);
            this.selectInviteListItem(pIndex);
        }

        public void selectInviteListItem(int pIndex) {
            RealmsPendingInvitesScreen.this.selectedInvite = pIndex;
            RealmsPendingInvitesScreen.this.updateButtonStates();
        }

        public void setSelected(@Nullable RealmsPendingInvitesScreen.Entry pSelected) {
            super.setSelected(pSelected);
            RealmsPendingInvitesScreen.this.selectedInvite = this.children().indexOf(pSelected);
            RealmsPendingInvitesScreen.this.updateButtonStates();
        }
    }
}