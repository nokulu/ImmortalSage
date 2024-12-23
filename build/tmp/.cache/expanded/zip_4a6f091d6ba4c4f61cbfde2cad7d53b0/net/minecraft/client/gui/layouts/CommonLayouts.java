package net.minecraft.client.gui.layouts;

import java.util.function.Consumer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CommonLayouts {
    private static final int LABEL_SPACING = 4;

    private CommonLayouts() {
    }

    public static Layout labeledElement(Font pFont, LayoutElement pElement, Component pLabel) {
        return labeledElement(pFont, pElement, pLabel, p_297385_ -> {
        });
    }

    public static Layout labeledElement(Font pFont, LayoutElement pElement, Component pLabel, Consumer<LayoutSettings> pLayoutSettings) {
        LinearLayout linearlayout = LinearLayout.vertical().spacing(4);
        linearlayout.addChild(new StringWidget(pLabel, pFont));
        linearlayout.addChild(pElement, pLayoutSettings);
        return linearlayout;
    }
}