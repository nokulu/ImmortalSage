package net.minecraft.client.gui.screens;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import java.net.URI;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ConfirmLinkScreen extends ConfirmScreen {
    private static final Component COPY_BUTTON_TEXT = Component.translatable("chat.copy");
    private static final Component WARNING_TEXT = Component.translatable("chat.link.warning");
    private final String url;
    private final boolean showWarning;

    public ConfirmLinkScreen(BooleanConsumer pCallback, String pUrl, boolean pTrusted) {
        this(
            pCallback,
            confirmMessage(pTrusted),
            Component.literal(pUrl),
            pUrl,
            pTrusted ? CommonComponents.GUI_CANCEL : CommonComponents.GUI_NO,
            pTrusted
        );
    }

    public ConfirmLinkScreen(BooleanConsumer pCallback, Component pTitle, String pUrl, boolean pTrusted) {
        this(pCallback, pTitle, confirmMessage(pTrusted, pUrl), pUrl, pTrusted ? CommonComponents.GUI_CANCEL : CommonComponents.GUI_NO, pTrusted);
    }

    public ConfirmLinkScreen(BooleanConsumer pCallback, Component pTitle, URI pUri, boolean pTrusted) {
        this(pCallback, pTitle, pUri.toString(), pTrusted);
    }

    public ConfirmLinkScreen(BooleanConsumer pCallback, Component pTitle, Component pMessage, URI pUri, Component pNoButton, boolean pTrusted) {
        this(pCallback, pTitle, pMessage, pUri.toString(), pNoButton, true);
    }

    public ConfirmLinkScreen(BooleanConsumer pCallback, Component pTitle, Component pMessage, String pUrl, Component pNoButton, boolean pTrusted) {
        super(pCallback, pTitle, pMessage);
        this.yesButton = (Component)(pTrusted ? Component.translatable("chat.link.open") : CommonComponents.GUI_YES);
        this.noButton = pNoButton;
        this.showWarning = !pTrusted;
        this.url = pUrl;
    }

    protected static MutableComponent confirmMessage(boolean pTrusted, String pExtraInfo) {
        return confirmMessage(pTrusted).append(CommonComponents.SPACE).append(Component.literal(pExtraInfo));
    }

    protected static MutableComponent confirmMessage(boolean pTrusted) {
        return Component.translatable(pTrusted ? "chat.link.confirmTrusted" : "chat.link.confirm");
    }

    @Override
    protected void addButtons(int pY) {
        this.addRenderableWidget(
            Button.builder(this.yesButton, p_169249_ -> this.callback.accept(true)).bounds(this.width / 2 - 50 - 105, pY, 100, 20).build()
        );
        this.addRenderableWidget(Button.builder(COPY_BUTTON_TEXT, p_169247_ -> {
            this.copyToClipboard();
            this.callback.accept(false);
        }).bounds(this.width / 2 - 50, pY, 100, 20).build());
        this.addRenderableWidget(
            Button.builder(this.noButton, p_169245_ -> this.callback.accept(false)).bounds(this.width / 2 - 50 + 105, pY, 100, 20).build()
        );
    }

    public void copyToClipboard() {
        this.minecraft.keyboardHandler.setClipboard(this.url);
    }

    @Override
    public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        if (this.showWarning) {
            pGuiGraphics.drawCenteredString(this.font, WARNING_TEXT, this.width / 2, 110, 16764108);
        }
    }

    public static void confirmLinkNow(Screen pLastScreen, String pUrl, boolean pTrusted) {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.setScreen(new ConfirmLinkScreen(p_274671_ -> {
            if (p_274671_) {
                Util.getPlatform().openUri(pUrl);
            }

            minecraft.setScreen(pLastScreen);
        }, pUrl, pTrusted));
    }

    public static void confirmLinkNow(Screen pLastScreen, URI pUri, boolean pTrusted) {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.setScreen(new ConfirmLinkScreen(p_340793_ -> {
            if (p_340793_) {
                Util.getPlatform().openUri(pUri);
            }

            minecraft.setScreen(pLastScreen);
        }, pUri.toString(), pTrusted));
    }

    public static void confirmLinkNow(Screen pLastScreen, URI pUri) {
        confirmLinkNow(pLastScreen, pUri, true);
    }

    public static void confirmLinkNow(Screen pLastScreen, String pUrl) {
        confirmLinkNow(pLastScreen, pUrl, true);
    }

    public static Button.OnPress confirmLink(Screen pLastScreen, String pUrl, boolean pTrusted) {
        return p_340797_ -> confirmLinkNow(pLastScreen, pUrl, pTrusted);
    }

    public static Button.OnPress confirmLink(Screen pLastScreen, URI pUri, boolean pTrusted) {
        return p_340789_ -> confirmLinkNow(pLastScreen, pUri, pTrusted);
    }

    public static Button.OnPress confirmLink(Screen pLastScreen, String pUrl) {
        return confirmLink(pLastScreen, pUrl, true);
    }

    public static Button.OnPress confirmLink(Screen pLastScreen, URI pUri) {
        return confirmLink(pLastScreen, pUri, true);
    }
}