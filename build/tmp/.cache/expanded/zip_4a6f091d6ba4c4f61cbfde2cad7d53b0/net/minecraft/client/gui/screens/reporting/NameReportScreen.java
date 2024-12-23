package net.minecraft.client.gui.screens.reporting;

import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.CommonLayouts;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.chat.report.NameReport;
import net.minecraft.client.multiplayer.chat.report.ReportingContext;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class NameReportScreen extends AbstractReportScreen<NameReport.Builder> {
    private static final Component TITLE = Component.translatable("gui.abuseReport.name.title");
    private MultiLineEditBox commentBox;

    private NameReportScreen(Screen pLastScreen, ReportingContext pReportingContext, NameReport.Builder pReportBuilder) {
        super(TITLE, pLastScreen, pReportingContext, pReportBuilder);
    }

    public NameReportScreen(Screen pLastScreen, ReportingContext pReportingContext, UUID pReportedProfileId, String pReportedName) {
        this(pLastScreen, pReportingContext, new NameReport.Builder(pReportedProfileId, pReportedName, pReportingContext.sender().reportLimits()));
    }

    public NameReportScreen(Screen pLastScreen, ReportingContext pReportingContext, NameReport pReport) {
        this(pLastScreen, pReportingContext, new NameReport.Builder(pReport, pReportingContext.sender().reportLimits()));
    }

    @Override
    protected void addContent() {
        Component component = Component.literal(this.reportBuilder.report().getReportedName()).withStyle(ChatFormatting.YELLOW);
        this.layout
            .addChild(
                new StringWidget(Component.translatable("gui.abuseReport.name.reporting", component), this.font),
                p_297722_ -> p_297722_.alignHorizontallyLeft().padding(0, 8)
            );
        this.commentBox = this.createCommentBox(280, 9 * 8, p_340822_ -> {
            this.reportBuilder.setComments(p_340822_);
            this.onReportChanged();
        });
        this.layout.addChild(CommonLayouts.labeledElement(this.font, this.commentBox, MORE_COMMENTS_LABEL, p_299823_ -> p_299823_.paddingBottom(12)));
    }

    @Override
    public boolean mouseReleased(double pMouseX, double pMouseY, int pButton) {
        return super.mouseReleased(pMouseX, pMouseY, pButton) ? true : this.commentBox.mouseReleased(pMouseX, pMouseY, pButton);
    }
}