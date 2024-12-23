package net.minecraft.client.gui.screens.inventory.tooltip;

import com.mojang.authlib.yggdrasil.ProfileResult;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ClientActivePlayersTooltip implements ClientTooltipComponent {
    private static final int SKIN_SIZE = 10;
    private static final int PADDING = 2;
    private final List<ProfileResult> activePlayers;

    public ClientActivePlayersTooltip(ClientActivePlayersTooltip.ActivePlayersTooltip pTooltip) {
        this.activePlayers = pTooltip.profiles();
    }

    @Override
    public int getHeight() {
        return this.activePlayers.size() * 12 + 2;
    }

    @Override
    public int getWidth(Font pFont) {
        int i = 0;

        for (ProfileResult profileresult : this.activePlayers) {
            int j = pFont.width(profileresult.profile().getName());
            if (j > i) {
                i = j;
            }
        }

        return i + 10 + 6;
    }

    @Override
    public void renderImage(Font pFont, int pX, int pY, GuiGraphics pGuiGraphics) {
        for (int i = 0; i < this.activePlayers.size(); i++) {
            ProfileResult profileresult = this.activePlayers.get(i);
            int j = pY + 2 + i * 12;
            PlayerFaceRenderer.draw(pGuiGraphics, Minecraft.getInstance().getSkinManager().getInsecureSkin(profileresult.profile()), pX + 2, j, 10);
            pGuiGraphics.drawString(pFont, profileresult.profile().getName(), pX + 10 + 4, j + 2, -1);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static record ActivePlayersTooltip(List<ProfileResult> profiles) implements TooltipComponent {
    }
}