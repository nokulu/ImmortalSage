package net.minecraft.client.gui.screens.options;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.Monitor;
import com.mojang.blaze3d.platform.VideoMode;
import com.mojang.blaze3d.platform.Window;
import java.util.List;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.client.GraphicsStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GpuWarnlistManager;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class VideoSettingsScreen extends OptionsSubScreen {
    private static final Component TITLE = Component.translatable("options.videoTitle");
    private static final Component FABULOUS = Component.translatable("options.graphics.fabulous").withStyle(ChatFormatting.ITALIC);
    private static final Component WARNING_MESSAGE = Component.translatable("options.graphics.warning.message", FABULOUS, FABULOUS);
    private static final Component WARNING_TITLE = Component.translatable("options.graphics.warning.title").withStyle(ChatFormatting.RED);
    private static final Component BUTTON_ACCEPT = Component.translatable("options.graphics.warning.accept");
    private static final Component BUTTON_CANCEL = Component.translatable("options.graphics.warning.cancel");
    private final GpuWarnlistManager gpuWarnlistManager;
    private final int oldMipmaps;

    private static OptionInstance<?>[] options(Options pOptions) {
        return new OptionInstance[]{
            pOptions.graphicsMode(),
            pOptions.renderDistance(),
            pOptions.prioritizeChunkUpdates(),
            pOptions.simulationDistance(),
            pOptions.ambientOcclusion(),
            pOptions.framerateLimit(),
            pOptions.enableVsync(),
            pOptions.bobView(),
            pOptions.guiScale(),
            pOptions.attackIndicator(),
            pOptions.gamma(),
            pOptions.cloudStatus(),
            pOptions.fullscreen(),
            pOptions.particles(),
            pOptions.mipmapLevels(),
            pOptions.entityShadows(),
            pOptions.screenEffectScale(),
            pOptions.entityDistanceScaling(),
            pOptions.fovEffectScale(),
            pOptions.showAutosaveIndicator(),
            pOptions.glintSpeed(),
            pOptions.glintStrength(),
            pOptions.menuBackgroundBlurriness()
        };
    }

    public VideoSettingsScreen(Screen pLastScreen, Minecraft pMinecraft, Options pOptions) {
        super(pLastScreen, pOptions, TITLE);
        this.gpuWarnlistManager = pMinecraft.getGpuWarnlistManager();
        this.gpuWarnlistManager.resetWarnings();
        if (pOptions.graphicsMode().get() == GraphicsStatus.FABULOUS) {
            this.gpuWarnlistManager.dismissWarning();
        }

        this.oldMipmaps = pOptions.mipmapLevels().get();
    }

    @Override
    protected void addOptions() {
        int i = -1;
        Window window = this.minecraft.getWindow();
        Monitor monitor = window.findBestMonitor();
        int j;
        if (monitor == null) {
            j = -1;
        } else {
            Optional<VideoMode> optional = window.getPreferredFullscreenVideoMode();
            j = optional.map(monitor::getVideoModeIndex).orElse(-1);
        }

        OptionInstance<Integer> optioninstance = new OptionInstance<>(
            "options.fullscreen.resolution",
            OptionInstance.noTooltip(),
            (p_344242_, p_344033_) -> {
                if (monitor == null) {
                    return Component.translatable("options.fullscreen.unavailable");
                } else if (p_344033_ == -1) {
                    return Options.genericValueLabel(p_344242_, Component.translatable("options.fullscreen.current"));
                } else {
                    VideoMode videomode = monitor.getMode(p_344033_);
                    return Options.genericValueLabel(
                        p_344242_,
                        Component.translatable(
                            "options.fullscreen.entry",
                            videomode.getWidth(),
                            videomode.getHeight(),
                            videomode.getRefreshRate(),
                            videomode.getRedBits() + videomode.getGreenBits() + videomode.getBlueBits()
                        )
                    );
                }
            },
            new OptionInstance.IntRange(-1, monitor != null ? monitor.getModeCount() - 1 : -1),
            j,
            p_345267_ -> {
                if (monitor != null) {
                    window.setPreferredFullscreenVideoMode(p_345267_ == -1 ? Optional.empty() : Optional.of(monitor.getMode(p_345267_)));
                }
            }
        );
        this.list.addBig(optioninstance);
        this.list.addBig(this.options.biomeBlendRadius());
        this.list.addSmall(options(this.options));
    }

    @Override
    public void onClose() {
        this.minecraft.getWindow().changeFullscreenVideoMode();
        super.onClose();
    }

    @Override
    public void removed() {
        if (this.options.mipmapLevels().get() != this.oldMipmaps) {
            this.minecraft.updateMaxMipLevel(this.options.mipmapLevels().get());
            this.minecraft.delayTextureReload();
        }

        super.removed();
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        if (super.mouseClicked(pMouseX, pMouseY, pButton)) {
            if (this.gpuWarnlistManager.isShowingWarning()) {
                List<Component> list = Lists.newArrayList(WARNING_MESSAGE, CommonComponents.NEW_LINE);
                String s = this.gpuWarnlistManager.getRendererWarnings();
                if (s != null) {
                    list.add(CommonComponents.NEW_LINE);
                    list.add(Component.translatable("options.graphics.warning.renderer", s).withStyle(ChatFormatting.GRAY));
                }

                String s1 = this.gpuWarnlistManager.getVendorWarnings();
                if (s1 != null) {
                    list.add(CommonComponents.NEW_LINE);
                    list.add(Component.translatable("options.graphics.warning.vendor", s1).withStyle(ChatFormatting.GRAY));
                }

                String s2 = this.gpuWarnlistManager.getVersionWarnings();
                if (s2 != null) {
                    list.add(CommonComponents.NEW_LINE);
                    list.add(Component.translatable("options.graphics.warning.version", s2).withStyle(ChatFormatting.GRAY));
                }

                this.minecraft
                    .setScreen(
                        new UnsupportedGraphicsWarningScreen(
                            WARNING_TITLE, list, ImmutableList.of(new UnsupportedGraphicsWarningScreen.ButtonOption(BUTTON_ACCEPT, p_343553_ -> {
                                this.options.graphicsMode().set(GraphicsStatus.FABULOUS);
                                Minecraft.getInstance().levelRenderer.allChanged();
                                this.gpuWarnlistManager.dismissWarning();
                                this.minecraft.setScreen(this);
                            }), new UnsupportedGraphicsWarningScreen.ButtonOption(BUTTON_CANCEL, p_342805_ -> {
                                this.gpuWarnlistManager.dismissWarningAndSkipFabulous();
                                this.minecraft.setScreen(this);
                            }))
                        )
                    );
            }

            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean mouseScrolled(double pMouseX, double pMouseY, double pScrollX, double pScrollY) {
        if (Screen.hasControlDown()) {
            OptionInstance<Integer> optioninstance = this.options.guiScale();
            if (optioninstance.values() instanceof OptionInstance.ClampingLazyMaxIntRange optioninstance$clampinglazymaxintrange) {
                int k = optioninstance.get();
                int i = k == 0 ? optioninstance$clampinglazymaxintrange.maxInclusive() + 1 : k;
                int j = i + (int)Math.signum(pScrollY);
                if (j != 0 && j <= optioninstance$clampinglazymaxintrange.maxInclusive() && j >= optioninstance$clampinglazymaxintrange.minInclusive()) {
                    CycleButton<Integer> cyclebutton = (CycleButton<Integer>)this.list.findOption(optioninstance);
                    if (cyclebutton != null) {
                        optioninstance.set(j);
                        cyclebutton.setValue(j);
                        this.list.setScrollAmount(0.0);
                        return true;
                    }
                }
            }

            return false;
        } else {
            return super.mouseScrolled(pMouseX, pMouseY, pScrollX, pScrollY);
        }
    }
}