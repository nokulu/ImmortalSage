package net.minecraft.client.gui;

import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.AttackIndicatorStatus;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.BossHealthOverlay;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.gui.components.SubtitleOverlay;
import net.minecraft.client.gui.components.spectator.SpectatorGui;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.MobEffectTextureManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.numbers.NumberFormat;
import net.minecraft.network.chat.numbers.StyledFormat;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringUtil;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PlayerRideableJumping;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerScoreEntry;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4fStack;

@OnlyIn(Dist.CLIENT)
public class Gui {
    private static final ResourceLocation CROSSHAIR_SPRITE = ResourceLocation.withDefaultNamespace("hud/crosshair");
    private static final ResourceLocation CROSSHAIR_ATTACK_INDICATOR_FULL_SPRITE = ResourceLocation.withDefaultNamespace("hud/crosshair_attack_indicator_full");
    private static final ResourceLocation CROSSHAIR_ATTACK_INDICATOR_BACKGROUND_SPRITE = ResourceLocation.withDefaultNamespace("hud/crosshair_attack_indicator_background");
    private static final ResourceLocation CROSSHAIR_ATTACK_INDICATOR_PROGRESS_SPRITE = ResourceLocation.withDefaultNamespace("hud/crosshair_attack_indicator_progress");
    private static final ResourceLocation EFFECT_BACKGROUND_AMBIENT_SPRITE = ResourceLocation.withDefaultNamespace("hud/effect_background_ambient");
    private static final ResourceLocation EFFECT_BACKGROUND_SPRITE = ResourceLocation.withDefaultNamespace("hud/effect_background");
    private static final ResourceLocation HOTBAR_SPRITE = ResourceLocation.withDefaultNamespace("hud/hotbar");
    private static final ResourceLocation HOTBAR_SELECTION_SPRITE = ResourceLocation.withDefaultNamespace("hud/hotbar_selection");
    private static final ResourceLocation HOTBAR_OFFHAND_LEFT_SPRITE = ResourceLocation.withDefaultNamespace("hud/hotbar_offhand_left");
    private static final ResourceLocation HOTBAR_OFFHAND_RIGHT_SPRITE = ResourceLocation.withDefaultNamespace("hud/hotbar_offhand_right");
    private static final ResourceLocation HOTBAR_ATTACK_INDICATOR_BACKGROUND_SPRITE = ResourceLocation.withDefaultNamespace("hud/hotbar_attack_indicator_background");
    private static final ResourceLocation HOTBAR_ATTACK_INDICATOR_PROGRESS_SPRITE = ResourceLocation.withDefaultNamespace("hud/hotbar_attack_indicator_progress");
    private static final ResourceLocation JUMP_BAR_BACKGROUND_SPRITE = ResourceLocation.withDefaultNamespace("hud/jump_bar_background");
    private static final ResourceLocation JUMP_BAR_COOLDOWN_SPRITE = ResourceLocation.withDefaultNamespace("hud/jump_bar_cooldown");
    private static final ResourceLocation JUMP_BAR_PROGRESS_SPRITE = ResourceLocation.withDefaultNamespace("hud/jump_bar_progress");
    private static final ResourceLocation EXPERIENCE_BAR_BACKGROUND_SPRITE = ResourceLocation.withDefaultNamespace("hud/experience_bar_background");
    private static final ResourceLocation EXPERIENCE_BAR_PROGRESS_SPRITE = ResourceLocation.withDefaultNamespace("hud/experience_bar_progress");
    private static final ResourceLocation ARMOR_EMPTY_SPRITE = ResourceLocation.withDefaultNamespace("hud/armor_empty");
    private static final ResourceLocation ARMOR_HALF_SPRITE = ResourceLocation.withDefaultNamespace("hud/armor_half");
    private static final ResourceLocation ARMOR_FULL_SPRITE = ResourceLocation.withDefaultNamespace("hud/armor_full");
    private static final ResourceLocation FOOD_EMPTY_HUNGER_SPRITE = ResourceLocation.withDefaultNamespace("hud/food_empty_hunger");
    private static final ResourceLocation FOOD_HALF_HUNGER_SPRITE = ResourceLocation.withDefaultNamespace("hud/food_half_hunger");
    private static final ResourceLocation FOOD_FULL_HUNGER_SPRITE = ResourceLocation.withDefaultNamespace("hud/food_full_hunger");
    private static final ResourceLocation FOOD_EMPTY_SPRITE = ResourceLocation.withDefaultNamespace("hud/food_empty");
    private static final ResourceLocation FOOD_HALF_SPRITE = ResourceLocation.withDefaultNamespace("hud/food_half");
    private static final ResourceLocation FOOD_FULL_SPRITE = ResourceLocation.withDefaultNamespace("hud/food_full");
    private static final ResourceLocation AIR_SPRITE = ResourceLocation.withDefaultNamespace("hud/air");
    private static final ResourceLocation AIR_BURSTING_SPRITE = ResourceLocation.withDefaultNamespace("hud/air_bursting");
    private static final ResourceLocation HEART_VEHICLE_CONTAINER_SPRITE = ResourceLocation.withDefaultNamespace("hud/heart/vehicle_container");
    private static final ResourceLocation HEART_VEHICLE_FULL_SPRITE = ResourceLocation.withDefaultNamespace("hud/heart/vehicle_full");
    private static final ResourceLocation HEART_VEHICLE_HALF_SPRITE = ResourceLocation.withDefaultNamespace("hud/heart/vehicle_half");
    private static final ResourceLocation VIGNETTE_LOCATION = ResourceLocation.withDefaultNamespace("textures/misc/vignette.png");
    private static final ResourceLocation PUMPKIN_BLUR_LOCATION = ResourceLocation.withDefaultNamespace("textures/misc/pumpkinblur.png");
    private static final ResourceLocation SPYGLASS_SCOPE_LOCATION = ResourceLocation.withDefaultNamespace("textures/misc/spyglass_scope.png");
    private static final ResourceLocation POWDER_SNOW_OUTLINE_LOCATION = ResourceLocation.withDefaultNamespace("textures/misc/powder_snow_outline.png");
    private static final Comparator<PlayerScoreEntry> SCORE_DISPLAY_ORDER = Comparator.comparing(PlayerScoreEntry::value)
        .reversed()
        .thenComparing(PlayerScoreEntry::owner, String.CASE_INSENSITIVE_ORDER);
    private static final Component DEMO_EXPIRED_TEXT = Component.translatable("demo.demoExpired");
    private static final Component SAVING_TEXT = Component.translatable("menu.savingLevel");
    private static final float MIN_CROSSHAIR_ATTACK_SPEED = 5.0F;
    private static final int NUM_HEARTS_PER_ROW = 10;
    private static final int LINE_HEIGHT = 10;
    private static final String SPACER = ": ";
    private static final float PORTAL_OVERLAY_ALPHA_MIN = 0.2F;
    private static final int HEART_SIZE = 9;
    private static final int HEART_SEPARATION = 8;
    private static final float AUTOSAVE_FADE_SPEED_FACTOR = 0.2F;
    private final RandomSource random = RandomSource.create();
    private final Minecraft minecraft;
    private final ChatComponent chat;
    private int tickCount;
    @Nullable
    private Component overlayMessageString;
    private int overlayMessageTime;
    private boolean animateOverlayMessageColor;
    private boolean chatDisabledByPlayerShown;
    public float vignetteBrightness = 1.0F;
    private int toolHighlightTimer;
    private ItemStack lastToolHighlight = ItemStack.EMPTY;
    protected DebugScreenOverlay debugOverlay;
    private final SubtitleOverlay subtitleOverlay;
    private final SpectatorGui spectatorGui;
    private final PlayerTabOverlay tabList;
    private final BossHealthOverlay bossOverlay;
    private int titleTime;
    @Nullable
    private Component title;
    @Nullable
    private Component subtitle;
    private int titleFadeInTime;
    private int titleStayTime;
    private int titleFadeOutTime;
    private int lastHealth;
    private int displayHealth;
    private long lastHealthTime;
    private long healthBlinkTime;
    private float autosaveIndicatorValue;
    private float lastAutosaveIndicatorValue;
    private final LayeredDraw layers = new LayeredDraw();
    private float scopeScale;

    public Gui(Minecraft pMinecraft) {
        this.minecraft = pMinecraft;
        this.debugOverlay = new DebugScreenOverlay(pMinecraft);
        this.spectatorGui = new SpectatorGui(pMinecraft);
        this.chat = new ChatComponent(pMinecraft);
        this.tabList = new PlayerTabOverlay(pMinecraft, this);
        this.bossOverlay = new BossHealthOverlay(pMinecraft);
        this.subtitleOverlay = new SubtitleOverlay(pMinecraft);
        this.resetTitleTimes();
        LayeredDraw layereddraw = new LayeredDraw()
            .add(this::renderCameraOverlays)
            .add(this::renderCrosshair)
            .add(this::renderHotbarAndDecorations)
            .add(this::renderExperienceLevel)
            .add(this::renderEffects)
            .add((p_325313_, p_342678_) -> this.bossOverlay.render(p_325313_));
        LayeredDraw layereddraw1 = new LayeredDraw()
            .add(this::renderDemoOverlay)
            .add((p_325315_, p_344521_) -> {
                if (this.debugOverlay.showDebugScreen()) {
                    this.debugOverlay.render(p_325315_);
                }
            })
            .add(this::renderScoreboardSidebar)
            .add(this::renderOverlayMessage)
            .add(this::renderTitle)
            .add(this::renderChat)
            .add(this::renderTabList)
            .add((p_325317_, p_342874_) -> this.subtitleOverlay.render(p_325317_));
        this.layers
            .add(layereddraw, () -> !pMinecraft.options.hideGui)
            .add(this::renderSleepOverlay)
            .add(layereddraw1, () -> !pMinecraft.options.hideGui);
    }

    public void resetTitleTimes() {
        this.titleFadeInTime = 10;
        this.titleStayTime = 70;
        this.titleFadeOutTime = 20;
    }

    public void render(GuiGraphics pGuiGraphics, DeltaTracker pDeltaTracker) {
        RenderSystem.enableDepthTest();
        this.layers.render(pGuiGraphics, pDeltaTracker);
        RenderSystem.disableDepthTest();
    }

    private void renderCameraOverlays(GuiGraphics p_333627_, DeltaTracker p_344236_) {
        if (Minecraft.useFancyGraphics()) {
            this.renderVignette(p_333627_, this.minecraft.getCameraEntity());
        }

        float f = p_344236_.getGameTimeDeltaTicks();
        this.scopeScale = Mth.lerp(0.5F * f, this.scopeScale, 1.125F);
        if (this.minecraft.options.getCameraType().isFirstPerson()) {
            if (this.minecraft.player.isScoping()) {
                this.renderSpyglassOverlay(p_333627_, this.scopeScale);
            } else {
                this.scopeScale = 0.5F;
                ItemStack itemstack = this.minecraft.player.getInventory().getArmor(3);
                if (itemstack.is(Blocks.CARVED_PUMPKIN.asItem())) {
                    this.renderTextureOverlay(p_333627_, PUMPKIN_BLUR_LOCATION, 1.0F);
                }
            }
        }

        if (this.minecraft.player.getTicksFrozen() > 0) {
            this.renderTextureOverlay(p_333627_, POWDER_SNOW_OUTLINE_LOCATION, this.minecraft.player.getPercentFrozen());
        }

        float f1 = Mth.lerp(p_344236_.getGameTimeDeltaPartialTick(false), this.minecraft.player.oSpinningEffectIntensity, this.minecraft.player.spinningEffectIntensity);
        if (f1 > 0.0F && !this.minecraft.player.hasEffect(MobEffects.CONFUSION)) {
            this.renderPortalOverlay(p_333627_, f1);
        }
    }

    private void renderSleepOverlay(GuiGraphics p_329087_, DeltaTracker p_345225_) {
        if (this.minecraft.player.getSleepTimer() > 0) {
            this.minecraft.getProfiler().push("sleep");
            float f = (float)this.minecraft.player.getSleepTimer();
            float f1 = f / 100.0F;
            if (f1 > 1.0F) {
                f1 = 1.0F - (f - 100.0F) / 10.0F;
            }

            int i = (int)(220.0F * f1) << 24 | 1052704;
            p_329087_.fill(RenderType.guiOverlay(), 0, 0, p_329087_.guiWidth(), p_329087_.guiHeight(), i);
            this.minecraft.getProfiler().pop();
        }
    }

    private void renderOverlayMessage(GuiGraphics p_330258_, DeltaTracker p_345514_) {
        Font font = this.getFont();
        if (this.overlayMessageString != null && this.overlayMessageTime > 0) {
            this.minecraft.getProfiler().push("overlayMessage");
            float f = (float)this.overlayMessageTime - p_345514_.getGameTimeDeltaPartialTick(false);
            int i = (int)(f * 255.0F / 20.0F);
            if (i > 255) {
                i = 255;
            }

            if (i > 8) {
                p_330258_.pose().pushPose();
                p_330258_.pose().translate((float)(p_330258_.guiWidth() / 2), (float)(p_330258_.guiHeight() - 68), 0.0F);
                int j;
                if (this.animateOverlayMessageColor) {
                    j = Mth.hsvToArgb(f / 50.0F, 0.7F, 0.6F, i);
                } else {
                    j = FastColor.ARGB32.color(i, -1);
                }

                int k = font.width(this.overlayMessageString);
                p_330258_.drawStringWithBackdrop(font, this.overlayMessageString, -k / 2, -4, k, j);
                p_330258_.pose().popPose();
            }

            this.minecraft.getProfiler().pop();
        }
    }

    private void renderTitle(GuiGraphics p_331218_, DeltaTracker p_344700_) {
        if (this.title != null && this.titleTime > 0) {
            Font font = this.getFont();
            this.minecraft.getProfiler().push("titleAndSubtitle");
            float f = (float)this.titleTime - p_344700_.getGameTimeDeltaPartialTick(false);
            int i = 255;
            if (this.titleTime > this.titleFadeOutTime + this.titleStayTime) {
                float f1 = (float)(this.titleFadeInTime + this.titleStayTime + this.titleFadeOutTime) - f;
                i = (int)(f1 * 255.0F / (float)this.titleFadeInTime);
            }

            if (this.titleTime <= this.titleFadeOutTime) {
                i = (int)(f * 255.0F / (float)this.titleFadeOutTime);
            }

            i = Mth.clamp(i, 0, 255);
            if (i > 8) {
                p_331218_.pose().pushPose();
                p_331218_.pose().translate((float)(p_331218_.guiWidth() / 2), (float)(p_331218_.guiHeight() / 2), 0.0F);
                p_331218_.pose().pushPose();
                p_331218_.pose().scale(4.0F, 4.0F, 4.0F);
                int l = font.width(this.title);
                int j = FastColor.ARGB32.color(i, -1);
                p_331218_.drawStringWithBackdrop(font, this.title, -l / 2, -10, l, j);
                p_331218_.pose().popPose();
                if (this.subtitle != null) {
                    p_331218_.pose().pushPose();
                    p_331218_.pose().scale(2.0F, 2.0F, 2.0F);
                    int k = font.width(this.subtitle);
                    p_331218_.drawStringWithBackdrop(font, this.subtitle, -k / 2, 5, k, j);
                    p_331218_.pose().popPose();
                }

                p_331218_.pose().popPose();
            }

            this.minecraft.getProfiler().pop();
        }
    }

    private void renderChat(GuiGraphics p_329202_, DeltaTracker p_342328_) {
        if (!this.chat.isChatFocused()) {
            Window window = this.minecraft.getWindow();
            int i = Mth.floor(this.minecraft.mouseHandler.xpos() * (double)window.getGuiScaledWidth() / (double)window.getScreenWidth());
            int j = Mth.floor(this.minecraft.mouseHandler.ypos() * (double)window.getGuiScaledHeight() / (double)window.getScreenHeight());
            net.minecraftforge.client.ForgeHooksClient.onCustomizeChatEvent(p_329202_, this.chat, window, i, j, this.tickCount);
        }
    }

    private void renderScoreboardSidebar(GuiGraphics p_332744_, DeltaTracker p_344235_) {
        Scoreboard scoreboard = this.minecraft.level.getScoreboard();
        Objective objective = null;
        PlayerTeam playerteam = scoreboard.getPlayersTeam(this.minecraft.player.getScoreboardName());
        if (playerteam != null) {
            DisplaySlot displayslot = DisplaySlot.teamColorToSlot(playerteam.getColor());
            if (displayslot != null) {
                objective = scoreboard.getDisplayObjective(displayslot);
            }
        }

        Objective objective1 = objective != null ? objective : scoreboard.getDisplayObjective(DisplaySlot.SIDEBAR);
        if (objective1 != null) {
            this.displayScoreboardSidebar(p_332744_, objective1);
        }
    }

    private void renderTabList(GuiGraphics p_330031_, DeltaTracker p_343599_) {
        Scoreboard scoreboard = this.minecraft.level.getScoreboard();
        Objective objective = scoreboard.getDisplayObjective(DisplaySlot.LIST);
        if (!this.minecraft.options.keyPlayerList.isDown()
            || this.minecraft.isLocalServer() && this.minecraft.player.connection.getListedOnlinePlayers().size() <= 1 && objective == null) {
            this.tabList.setVisible(false);
        } else {
            this.tabList.setVisible(true);
            this.tabList.render(p_330031_, p_330031_.guiWidth(), scoreboard, objective);
        }
    }

    private void renderCrosshair(GuiGraphics p_282828_, DeltaTracker p_343490_) {
        Options options = this.minecraft.options;
        if (options.getCameraType().isFirstPerson()) {
            if (this.minecraft.gameMode.getPlayerMode() != GameType.SPECTATOR || this.canRenderCrosshairForSpectator(this.minecraft.hitResult)) {
                RenderSystem.enableBlend();
                if (this.debugOverlay.showDebugScreen() && !this.minecraft.player.isReducedDebugInfo() && !options.reducedDebugInfo().get()) {
                    Camera camera = this.minecraft.gameRenderer.getMainCamera();
                    Matrix4fStack matrix4fstack = RenderSystem.getModelViewStack();
                    matrix4fstack.pushMatrix();
                    matrix4fstack.mul(p_282828_.pose().last().pose());
                    matrix4fstack.translate((float)(p_282828_.guiWidth() / 2), (float)(p_282828_.guiHeight() / 2), 0.0F);
                    matrix4fstack.rotateX(-camera.getXRot() * (float) (Math.PI / 180.0));
                    matrix4fstack.rotateY(camera.getYRot() * (float) (Math.PI / 180.0));
                    matrix4fstack.scale(-1.0F, -1.0F, -1.0F);
                    RenderSystem.applyModelViewMatrix();
                    RenderSystem.renderCrosshair(10);
                    matrix4fstack.popMatrix();
                    RenderSystem.applyModelViewMatrix();
                } else {
                    RenderSystem.blendFuncSeparate(
                        GlStateManager.SourceFactor.ONE_MINUS_DST_COLOR,
                        GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR,
                        GlStateManager.SourceFactor.ONE,
                        GlStateManager.DestFactor.ZERO
                    );
                    int i = 15;
                    p_282828_.blitSprite(CROSSHAIR_SPRITE, (p_282828_.guiWidth() - 15) / 2, (p_282828_.guiHeight() - 15) / 2, 15, 15);
                    if (this.minecraft.options.attackIndicator().get() == AttackIndicatorStatus.CROSSHAIR) {
                        float f = this.minecraft.player.getAttackStrengthScale(0.0F);
                        boolean flag = false;
                        if (this.minecraft.crosshairPickEntity != null && this.minecraft.crosshairPickEntity instanceof LivingEntity && f >= 1.0F) {
                            flag = this.minecraft.player.getCurrentItemAttackStrengthDelay() > 5.0F;
                            flag &= this.minecraft.crosshairPickEntity.isAlive();
                        }

                        int j = p_282828_.guiHeight() / 2 - 7 + 16;
                        int k = p_282828_.guiWidth() / 2 - 8;
                        if (flag) {
                            p_282828_.blitSprite(CROSSHAIR_ATTACK_INDICATOR_FULL_SPRITE, k, j, 16, 16);
                        } else if (f < 1.0F) {
                            int l = (int)(f * 17.0F);
                            p_282828_.blitSprite(CROSSHAIR_ATTACK_INDICATOR_BACKGROUND_SPRITE, k, j, 16, 4);
                            p_282828_.blitSprite(CROSSHAIR_ATTACK_INDICATOR_PROGRESS_SPRITE, 16, 4, 0, 0, k, j, l, 4);
                        }
                    }

                    RenderSystem.defaultBlendFunc();
                }

                RenderSystem.disableBlend();
            }
        }
    }

    private boolean canRenderCrosshairForSpectator(@Nullable HitResult pRayTrace) {
        if (pRayTrace == null) {
            return false;
        } else if (pRayTrace.getType() == HitResult.Type.ENTITY) {
            return ((EntityHitResult)pRayTrace).getEntity() instanceof MenuProvider;
        } else if (pRayTrace.getType() == HitResult.Type.BLOCK) {
            BlockPos blockpos = ((BlockHitResult)pRayTrace).getBlockPos();
            Level level = this.minecraft.level;
            return level.getBlockState(blockpos).getMenuProvider(level, blockpos) != null;
        } else {
            return false;
        }
    }

    private void renderEffects(GuiGraphics p_282812_, DeltaTracker p_343719_) {
        Collection<MobEffectInstance> collection = this.minecraft.player.getActiveEffects();
        if (!collection.isEmpty()) {
            if (this.minecraft.screen instanceof EffectRenderingInventoryScreen effectrenderinginventoryscreen && effectrenderinginventoryscreen.canSeeEffects()) {
                return;
            }

            RenderSystem.enableBlend();
            int j1 = 0;
            int k1 = 0;
            MobEffectTextureManager mobeffecttexturemanager = this.minecraft.getMobEffectTextures();
            List<Runnable> list = Lists.newArrayListWithExpectedSize(collection.size());

            for (MobEffectInstance mobeffectinstance : Ordering.natural().reverse().sortedCopy(collection)) {
                Holder<MobEffect> holder = mobeffectinstance.getEffect();
                var renderer = net.minecraftforge.client.extensions.common.IClientMobEffectExtensions.of(mobeffectinstance);
                if (!renderer.isVisibleInGui(mobeffectinstance)) continue;
                if (mobeffectinstance.showIcon()) {
                    int i = p_282812_.guiWidth();
                    int j = 1;
                    if (this.minecraft.isDemo()) {
                        j += 15;
                    }

                    if (holder.value().isBeneficial()) {
                        j1++;
                        i -= 25 * j1;
                    } else {
                        k1++;
                        i -= 25 * k1;
                        j += 26;
                    }

                    float f = 1.0F;
                    if (mobeffectinstance.isAmbient()) {
                        p_282812_.blitSprite(EFFECT_BACKGROUND_AMBIENT_SPRITE, i, j, 24, 24);
                    } else {
                        p_282812_.blitSprite(EFFECT_BACKGROUND_SPRITE, i, j, 24, 24);
                        if (mobeffectinstance.endsWithin(200)) {
                            int k = mobeffectinstance.getDuration();
                            int l = 10 - k / 20;
                            f = Mth.clamp((float)k / 10.0F / 5.0F * 0.5F, 0.0F, 0.5F)
                                + Mth.cos((float)k * (float) Math.PI / 5.0F) * Mth.clamp((float)l / 10.0F * 0.25F, 0.0F, 0.25F);
                        }
                    }

                    if (renderer.renderGuiIcon(mobeffectinstance, this, p_282812_, i, j, 0, f)) continue;
                    TextureAtlasSprite textureatlassprite = mobeffecttexturemanager.get(holder);
                    int l1 = i;
                    int i1 = j;
                    float f1 = f;
                    list.add(() -> {
                        p_282812_.setColor(1.0F, 1.0F, 1.0F, f1);
                        p_282812_.blit(l1 + 3, i1 + 3, 0, 18, 18, textureatlassprite);
                        p_282812_.setColor(1.0F, 1.0F, 1.0F, 1.0F);
                    });
                }
            }

            list.forEach(Runnable::run);
            RenderSystem.disableBlend();
        }
    }

    private void renderHotbarAndDecorations(GuiGraphics p_333625_, DeltaTracker p_344796_) {
        if (this.minecraft.gameMode.getPlayerMode() == GameType.SPECTATOR) {
            this.spectatorGui.renderHotbar(p_333625_);
        } else {
            this.renderItemHotbar(p_333625_, p_344796_);
        }

        int i = p_333625_.guiWidth() / 2 - 91;
        PlayerRideableJumping playerrideablejumping = this.minecraft.player.jumpableVehicle();
        if (playerrideablejumping != null) {
            this.renderJumpMeter(playerrideablejumping, p_333625_, i);
        } else if (this.isExperienceBarVisible()) {
            this.renderExperienceBar(p_333625_, i);
        }

        if (this.minecraft.gameMode.canHurtPlayer()) {
            this.renderPlayerHealth(p_333625_);
        }

        this.renderVehicleHealth(p_333625_);
        if (this.minecraft.gameMode.getPlayerMode() != GameType.SPECTATOR) {
            this.renderSelectedItemName(p_333625_);
        } else if (this.minecraft.player.isSpectator()) {
            this.spectatorGui.renderTooltip(p_333625_);
        }
    }

    private void renderItemHotbar(GuiGraphics pGuiGraphics, DeltaTracker pDeltaTracker) {
        Player player = this.getCameraPlayer();
        if (player != null) {
            ItemStack itemstack = player.getOffhandItem();
            HumanoidArm humanoidarm = player.getMainArm().getOpposite();
            int i = pGuiGraphics.guiWidth() / 2;
            int j = 182;
            int k = 91;
            RenderSystem.enableBlend();
            pGuiGraphics.pose().pushPose();
            pGuiGraphics.pose().translate(0.0F, 0.0F, -90.0F);
            pGuiGraphics.blitSprite(HOTBAR_SPRITE, i - 91, pGuiGraphics.guiHeight() - 22, 182, 22);
            pGuiGraphics.blitSprite(HOTBAR_SELECTION_SPRITE, i - 91 - 1 + player.getInventory().selected * 20, pGuiGraphics.guiHeight() - 22 - 1, 24, 23);
            if (!itemstack.isEmpty()) {
                if (humanoidarm == HumanoidArm.LEFT) {
                    pGuiGraphics.blitSprite(HOTBAR_OFFHAND_LEFT_SPRITE, i - 91 - 29, pGuiGraphics.guiHeight() - 23, 29, 24);
                } else {
                    pGuiGraphics.blitSprite(HOTBAR_OFFHAND_RIGHT_SPRITE, i + 91, pGuiGraphics.guiHeight() - 23, 29, 24);
                }
            }

            pGuiGraphics.pose().popPose();
            RenderSystem.disableBlend();
            int l = 1;

            for (int i1 = 0; i1 < 9; i1++) {
                int j1 = i - 90 + i1 * 20 + 2;
                int k1 = pGuiGraphics.guiHeight() - 16 - 3;
                this.renderSlot(pGuiGraphics, j1, k1, pDeltaTracker, player, player.getInventory().items.get(i1), l++);
            }

            if (!itemstack.isEmpty()) {
                int i2 = pGuiGraphics.guiHeight() - 16 - 3;
                if (humanoidarm == HumanoidArm.LEFT) {
                    this.renderSlot(pGuiGraphics, i - 91 - 26, i2, pDeltaTracker, player, itemstack, l++);
                } else {
                    this.renderSlot(pGuiGraphics, i + 91 + 10, i2, pDeltaTracker, player, itemstack, l++);
                }
            }

            if (this.minecraft.options.attackIndicator().get() == AttackIndicatorStatus.HOTBAR) {
                RenderSystem.enableBlend();
                float f = this.minecraft.player.getAttackStrengthScale(0.0F);
                if (f < 1.0F) {
                    int j2 = pGuiGraphics.guiHeight() - 20;
                    int k2 = i + 91 + 6;
                    if (humanoidarm == HumanoidArm.RIGHT) {
                        k2 = i - 91 - 22;
                    }

                    int l1 = (int)(f * 19.0F);
                    pGuiGraphics.blitSprite(HOTBAR_ATTACK_INDICATOR_BACKGROUND_SPRITE, k2, j2, 18, 18);
                    pGuiGraphics.blitSprite(HOTBAR_ATTACK_INDICATOR_PROGRESS_SPRITE, 18, 18, 0, 18 - l1, k2, j2 + 18 - l1, 18, l1);
                }

                RenderSystem.disableBlend();
            }
        }
    }

    private void renderJumpMeter(PlayerRideableJumping pRideable, GuiGraphics pGuiGraphics, int pX) {
        this.minecraft.getProfiler().push("jumpBar");
        float f = this.minecraft.player.getJumpRidingScale();
        int i = 182;
        int j = (int)(f * 183.0F);
        int k = pGuiGraphics.guiHeight() - 32 + 3;
        RenderSystem.enableBlend();
        pGuiGraphics.blitSprite(JUMP_BAR_BACKGROUND_SPRITE, pX, k, 182, 5);
        if (pRideable.getJumpCooldown() > 0) {
            pGuiGraphics.blitSprite(JUMP_BAR_COOLDOWN_SPRITE, pX, k, 182, 5);
        } else if (j > 0) {
            pGuiGraphics.blitSprite(JUMP_BAR_PROGRESS_SPRITE, 182, 5, 0, 0, pX, k, j, 5);
        }

        RenderSystem.disableBlend();
        this.minecraft.getProfiler().pop();
    }

    private void renderExperienceBar(GuiGraphics pGuiGraphics, int pX) {
        this.minecraft.getProfiler().push("expBar");
        int i = this.minecraft.player.getXpNeededForNextLevel();
        if (i > 0) {
            int j = 182;
            int k = (int)(this.minecraft.player.experienceProgress * 183.0F);
            int l = pGuiGraphics.guiHeight() - 32 + 3;
            RenderSystem.enableBlend();
            pGuiGraphics.blitSprite(EXPERIENCE_BAR_BACKGROUND_SPRITE, pX, l, 182, 5);
            if (k > 0) {
                pGuiGraphics.blitSprite(EXPERIENCE_BAR_PROGRESS_SPRITE, 182, 5, 0, 0, pX, l, k, 5);
            }

            RenderSystem.disableBlend();
        }

        this.minecraft.getProfiler().pop();
    }

    private void renderExperienceLevel(GuiGraphics p_335340_, DeltaTracker p_344840_) {
        int i = this.minecraft.player.experienceLevel;
        if (this.isExperienceBarVisible() && i > 0) {
            this.minecraft.getProfiler().push("expLevel");
            String s = i + "";
            int j = (p_335340_.guiWidth() - this.getFont().width(s)) / 2;
            int k = p_335340_.guiHeight() - 31 - 4;
            p_335340_.drawString(this.getFont(), s, j + 1, k, 0, false);
            p_335340_.drawString(this.getFont(), s, j - 1, k, 0, false);
            p_335340_.drawString(this.getFont(), s, j, k + 1, 0, false);
            p_335340_.drawString(this.getFont(), s, j, k - 1, 0, false);
            p_335340_.drawString(this.getFont(), s, j, k, 8453920, false);
            this.minecraft.getProfiler().pop();
        }
    }

    private boolean isExperienceBarVisible() {
        return this.minecraft.player.jumpableVehicle() == null && this.minecraft.gameMode.hasExperience();
    }

    private void renderSelectedItemName(GuiGraphics pGuiGraphics) {
        renderSelectedItemName(pGuiGraphics, 0);
    }

    public void renderSelectedItemName(GuiGraphics pGuiGraphics, int yShift) {
        this.minecraft.getProfiler().push("selectedItemName");
        if (this.toolHighlightTimer > 0 && !this.lastToolHighlight.isEmpty()) {
            MutableComponent mutablecomponent = Component.empty().append(this.lastToolHighlight.getHoverName()).withStyle(this.lastToolHighlight.getRarity().color());
            if (this.lastToolHighlight.has(DataComponents.CUSTOM_NAME)) {
                mutablecomponent.withStyle(ChatFormatting.ITALIC);
            }

            Component highlightTip = this.lastToolHighlight.getHighlightTip(mutablecomponent);
            Font font = net.minecraftforge.client.extensions.common.IClientItemExtensions.of(lastToolHighlight).getFont(lastToolHighlight, net.minecraftforge.client.extensions.common.IClientItemExtensions.FontContext.SELECTED_ITEM_NAME);
            if (font == null)
                font = this.getFont();
            int i = font.width(highlightTip);
            int j = (pGuiGraphics.guiWidth() - i) / 2;
            int k = pGuiGraphics.guiHeight() - Math.max(yShift, 59);
            if (!this.minecraft.gameMode.canHurtPlayer()) {
                k += 14;
            }

            int l = (int)((float)this.toolHighlightTimer * 256.0F / 10.0F);
            if (l > 255) {
                l = 255;
            }

            if (l > 0) {
                pGuiGraphics.drawStringWithBackdrop(font, highlightTip, j, k, i, FastColor.ARGB32.color(l, -1));
            }
        }

        this.minecraft.getProfiler().pop();
    }

    private void renderDemoOverlay(GuiGraphics p_281825_, DeltaTracker p_343325_) {
        if (this.minecraft.isDemo()) {
            this.minecraft.getProfiler().push("demo");
            Component component;
            if (this.minecraft.level.getGameTime() >= 120500L) {
                component = DEMO_EXPIRED_TEXT;
            } else {
                component = Component.translatable(
                    "demo.remainingTime",
                    StringUtil.formatTickDuration((int)(120500L - this.minecraft.level.getGameTime()), this.minecraft.level.tickRateManager().tickrate())
                );
            }

            int i = this.getFont().width(component);
            int j = p_281825_.guiWidth() - i - 10;
            int k = 5;
            p_281825_.drawStringWithBackdrop(this.getFont(), component, j, 5, i, -1);
            this.minecraft.getProfiler().pop();
        }
    }

    private void displayScoreboardSidebar(GuiGraphics pGuiGraphics, Objective pObjective) {
        Scoreboard scoreboard = pObjective.getScoreboard();
        NumberFormat numberformat = pObjective.numberFormatOrDefault(StyledFormat.SIDEBAR_DEFAULT);

        @OnlyIn(Dist.CLIENT)
        record DisplayEntry(Component name, Component score, int scoreWidth) {
        }

        DisplayEntry[] agui$1displayentry = scoreboard.listPlayerScores(pObjective)
            .stream()
            .filter(p_308174_ -> !p_308174_.isHidden())
            .sorted(SCORE_DISPLAY_ORDER)
            .limit(15L)
            .map(p_308178_ -> {
                PlayerTeam playerteam = scoreboard.getPlayersTeam(p_308178_.owner());
                Component component1 = p_308178_.ownerName();
                Component component2 = PlayerTeam.formatNameForTeam(playerteam, component1);
                Component component3 = p_308178_.formatValue(numberformat);
                int i1 = this.getFont().width(component3);
                return new DisplayEntry(component2, component3, i1);
            })
            .toArray(DisplayEntry[]::new);
        Component component = pObjective.getDisplayName();
        int i = this.getFont().width(component);
        int j = i;
        int k = this.getFont().width(": ");

        for (DisplayEntry gui$1displayentry : agui$1displayentry) {
            j = Math.max(j, this.getFont().width(gui$1displayentry.name) + (gui$1displayentry.scoreWidth > 0 ? k + gui$1displayentry.scoreWidth : 0));
        }

        int l = j;
        pGuiGraphics.drawManaged(() -> {
            int i1 = agui$1displayentry.length;
            int j1 = i1 * 9;
            int k1 = pGuiGraphics.guiHeight() / 2 + j1 / 3;
            int l1 = 3;
            int i2 = pGuiGraphics.guiWidth() - l - 3;
            int j2 = pGuiGraphics.guiWidth() - 3 + 2;
            int k2 = this.minecraft.options.getBackgroundColor(0.3F);
            int l2 = this.minecraft.options.getBackgroundColor(0.4F);
            int i3 = k1 - i1 * 9;
            pGuiGraphics.fill(i2 - 2, i3 - 9 - 1, j2, i3 - 1, l2);
            pGuiGraphics.fill(i2 - 2, i3 - 1, j2, k1, k2);
            pGuiGraphics.drawString(this.getFont(), component, i2 + l / 2 - i / 2, i3 - 9, -1, false);

            for (int j3 = 0; j3 < i1; j3++) {
                DisplayEntry gui$1displayentry1 = agui$1displayentry[j3];
                int k3 = k1 - (i1 - j3) * 9;
                pGuiGraphics.drawString(this.getFont(), gui$1displayentry1.name, i2, k3, -1, false);
                pGuiGraphics.drawString(this.getFont(), gui$1displayentry1.score, j2 - gui$1displayentry1.scoreWidth, k3, -1, false);
            }
        });
    }

    @Nullable
    private Player getCameraPlayer() {
        return this.minecraft.getCameraEntity() instanceof Player player ? player : null;
    }

    @Nullable
    private LivingEntity getPlayerVehicleWithHealth() {
        Player player = this.getCameraPlayer();
        if (player != null) {
            Entity entity = player.getVehicle();
            if (entity == null) {
                return null;
            }

            if (entity instanceof LivingEntity) {
                return (LivingEntity)entity;
            }
        }

        return null;
    }

    private int getVehicleMaxHearts(@Nullable LivingEntity pVehicle) {
        if (pVehicle != null && pVehicle.showVehicleHealth()) {
            float f = pVehicle.getMaxHealth();
            int i = (int)(f + 0.5F) / 2;
            if (i > 30) {
                i = 30;
            }

            return i;
        } else {
            return 0;
        }
    }

    private int getVisibleVehicleHeartRows(int pVehicleHealth) {
        return (int)Math.ceil((double)pVehicleHealth / 10.0);
    }

    private void renderPlayerHealth(GuiGraphics pGuiGraphics) {
        Player player = this.getCameraPlayer();
        if (player != null) {
            int i = Mth.ceil(player.getHealth());
            boolean flag = this.healthBlinkTime > (long)this.tickCount && (this.healthBlinkTime - (long)this.tickCount) / 3L % 2L == 1L;
            long j = Util.getMillis();
            if (i < this.lastHealth && player.invulnerableTime > 0) {
                this.lastHealthTime = j;
                this.healthBlinkTime = (long)(this.tickCount + 20);
            } else if (i > this.lastHealth && player.invulnerableTime > 0) {
                this.lastHealthTime = j;
                this.healthBlinkTime = (long)(this.tickCount + 10);
            }

            if (j - this.lastHealthTime > 1000L) {
                this.lastHealth = i;
                this.displayHealth = i;
                this.lastHealthTime = j;
            }

            this.lastHealth = i;
            int k = this.displayHealth;
            this.random.setSeed((long)(this.tickCount * 312871));
            int l = pGuiGraphics.guiWidth() / 2 - 91;
            int i1 = pGuiGraphics.guiWidth() / 2 + 91;
            int j1 = pGuiGraphics.guiHeight() - 39;
            float f = Math.max((float)player.getAttributeValue(Attributes.MAX_HEALTH), (float)Math.max(k, i));
            int k1 = Mth.ceil(player.getAbsorptionAmount());
            int l1 = Mth.ceil((f + (float)k1) / 2.0F / 10.0F);
            int i2 = Math.max(10 - (l1 - 2), 3);
            int j2 = j1 - 10;
            int k2 = -1;
            if (player.hasEffect(MobEffects.REGENERATION)) {
                k2 = this.tickCount % Mth.ceil(f + 5.0F);
            }

            this.minecraft.getProfiler().push("armor");
            renderArmor(pGuiGraphics, player, j1, l1, i2, l);
            this.minecraft.getProfiler().popPush("health");
            this.renderHearts(pGuiGraphics, player, l, j1, i2, k2, f, i, k, k1, flag);
            LivingEntity livingentity = this.getPlayerVehicleWithHealth();
            int l2 = this.getVehicleMaxHearts(livingentity);
            if (l2 == 0) {
                this.minecraft.getProfiler().popPush("food");
                this.renderFood(pGuiGraphics, player, j1, i1);
                j2 -= 10;
            }

            this.minecraft.getProfiler().popPush("air");
            int i3 = player.getMaxAirSupply();
            int j3 = Math.min(player.getAirSupply(), i3);
            if (player.isEyeInFluid(FluidTags.WATER) || j3 < i3) {
                int k3 = this.getVisibleVehicleHeartRows(l2) - 1;
                j2 -= k3 * 10;
                int l3 = Mth.ceil((double)(j3 - 2) * 10.0 / (double)i3);
                int i4 = Mth.ceil((double)j3 * 10.0 / (double)i3) - l3;
                RenderSystem.enableBlend();

                for (int j4 = 0; j4 < l3 + i4; j4++) {
                    if (j4 < l3) {
                        pGuiGraphics.blitSprite(AIR_SPRITE, i1 - j4 * 8 - 9, j2, 9, 9);
                    } else {
                        pGuiGraphics.blitSprite(AIR_BURSTING_SPRITE, i1 - j4 * 8 - 9, j2, 9, 9);
                    }
                }

                RenderSystem.disableBlend();
            }

            this.minecraft.getProfiler().pop();
        }
    }

    private static void renderArmor(GuiGraphics pGuiGraphics, Player pPlayer, int pY, int pHeartRows, int pHeight, int pX) {
        int i = pPlayer.getArmorValue();
        if (i > 0) {
            RenderSystem.enableBlend();
            int j = pY - (pHeartRows - 1) * pHeight - 10;

            for (int k = 0; k < 10; k++) {
                int l = pX + k * 8;
                if (k * 2 + 1 < i) {
                    pGuiGraphics.blitSprite(ARMOR_FULL_SPRITE, l, j, 9, 9);
                }

                if (k * 2 + 1 == i) {
                    pGuiGraphics.blitSprite(ARMOR_HALF_SPRITE, l, j, 9, 9);
                }

                if (k * 2 + 1 > i) {
                    pGuiGraphics.blitSprite(ARMOR_EMPTY_SPRITE, l, j, 9, 9);
                }
            }

            RenderSystem.disableBlend();
        }
    }

    private void renderHearts(
        GuiGraphics pGuiGraphics,
        Player pPlayer,
        int pX,
        int pY,
        int pHeight,
        int pOffsetHeartIndex,
        float pMaxHealth,
        int pCurrentHealth,
        int pDisplayHealth,
        int pAbsorptionAmount,
        boolean pRenderHighlight
    ) {
        Gui.HeartType gui$hearttype = Gui.HeartType.forPlayer(pPlayer);
        boolean flag = pPlayer.level().getLevelData().isHardcore();
        int i = Mth.ceil((double)pMaxHealth / 2.0);
        int j = Mth.ceil((double)pAbsorptionAmount / 2.0);
        int k = i * 2;

        for (int l = i + j - 1; l >= 0; l--) {
            int i1 = l / 10;
            int j1 = l % 10;
            int k1 = pX + j1 * 8;
            int l1 = pY - i1 * pHeight;
            if (pCurrentHealth + pAbsorptionAmount <= 4) {
                l1 += this.random.nextInt(2);
            }

            if (l < i && l == pOffsetHeartIndex) {
                l1 -= 2;
            }

            this.renderHeart(pGuiGraphics, Gui.HeartType.CONTAINER, k1, l1, flag, pRenderHighlight, false);
            int i2 = l * 2;
            boolean flag1 = l >= i;
            if (flag1) {
                int j2 = i2 - k;
                if (j2 < pAbsorptionAmount) {
                    boolean flag2 = j2 + 1 == pAbsorptionAmount;
                    this.renderHeart(pGuiGraphics, gui$hearttype == Gui.HeartType.WITHERED ? gui$hearttype : Gui.HeartType.ABSORBING, k1, l1, flag, false, flag2);
                }
            }

            if (pRenderHighlight && i2 < pDisplayHealth) {
                boolean flag3 = i2 + 1 == pDisplayHealth;
                this.renderHeart(pGuiGraphics, gui$hearttype, k1, l1, flag, true, flag3);
            }

            if (i2 < pCurrentHealth) {
                boolean flag4 = i2 + 1 == pCurrentHealth;
                this.renderHeart(pGuiGraphics, gui$hearttype, k1, l1, flag, false, flag4);
            }
        }
    }

    private void renderHeart(
        GuiGraphics pGuiGraphics, Gui.HeartType pHeartType, int pX, int pY, boolean pHardcore, boolean pHalfHeart, boolean pBlinking
    ) {
        RenderSystem.enableBlend();
        pGuiGraphics.blitSprite(pHeartType.getSprite(pHardcore, pBlinking, pHalfHeart), pX, pY, 9, 9);
        RenderSystem.disableBlend();
    }

    private void renderFood(GuiGraphics pGuiGraphics, Player pPlayer, int pY, int pX) {
        FoodData fooddata = pPlayer.getFoodData();
        int i = fooddata.getFoodLevel();
        RenderSystem.enableBlend();

        for (int j = 0; j < 10; j++) {
            int k = pY;
            ResourceLocation resourcelocation;
            ResourceLocation resourcelocation1;
            ResourceLocation resourcelocation2;
            if (pPlayer.hasEffect(MobEffects.HUNGER)) {
                resourcelocation = FOOD_EMPTY_HUNGER_SPRITE;
                resourcelocation1 = FOOD_HALF_HUNGER_SPRITE;
                resourcelocation2 = FOOD_FULL_HUNGER_SPRITE;
            } else {
                resourcelocation = FOOD_EMPTY_SPRITE;
                resourcelocation1 = FOOD_HALF_SPRITE;
                resourcelocation2 = FOOD_FULL_SPRITE;
            }

            if (pPlayer.getFoodData().getSaturationLevel() <= 0.0F && this.tickCount % (i * 3 + 1) == 0) {
                k = pY + (this.random.nextInt(3) - 1);
            }

            int l = pX - j * 8 - 9;
            pGuiGraphics.blitSprite(resourcelocation, l, k, 9, 9);
            if (j * 2 + 1 < i) {
                pGuiGraphics.blitSprite(resourcelocation2, l, k, 9, 9);
            }

            if (j * 2 + 1 == i) {
                pGuiGraphics.blitSprite(resourcelocation1, l, k, 9, 9);
            }
        }

        RenderSystem.disableBlend();
    }

    private void renderVehicleHealth(GuiGraphics pGuiGraphics) {
        LivingEntity livingentity = this.getPlayerVehicleWithHealth();
        if (livingentity != null) {
            int i = this.getVehicleMaxHearts(livingentity);
            if (i != 0) {
                int j = (int)Math.ceil((double)livingentity.getHealth());
                this.minecraft.getProfiler().popPush("mountHealth");
                int k = pGuiGraphics.guiHeight() - 39;
                int l = pGuiGraphics.guiWidth() / 2 + 91;
                int i1 = k;
                int j1 = 0;
                RenderSystem.enableBlend();

                while (i > 0) {
                    int k1 = Math.min(i, 10);
                    i -= k1;

                    for (int l1 = 0; l1 < k1; l1++) {
                        int i2 = l - l1 * 8 - 9;
                        pGuiGraphics.blitSprite(HEART_VEHICLE_CONTAINER_SPRITE, i2, i1, 9, 9);
                        if (l1 * 2 + 1 + j1 < j) {
                            pGuiGraphics.blitSprite(HEART_VEHICLE_FULL_SPRITE, i2, i1, 9, 9);
                        }

                        if (l1 * 2 + 1 + j1 == j) {
                            pGuiGraphics.blitSprite(HEART_VEHICLE_HALF_SPRITE, i2, i1, 9, 9);
                        }
                    }

                    i1 -= 10;
                    j1 += 20;
                }

                RenderSystem.disableBlend();
            }
        }
    }

    private void renderTextureOverlay(GuiGraphics pGuiGraphics, ResourceLocation pShaderLocation, float pAlpha) {
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        pGuiGraphics.setColor(1.0F, 1.0F, 1.0F, pAlpha);
        pGuiGraphics.blit(pShaderLocation, 0, 0, -90, 0.0F, 0.0F, pGuiGraphics.guiWidth(), pGuiGraphics.guiHeight(), pGuiGraphics.guiWidth(), pGuiGraphics.guiHeight());
        RenderSystem.disableBlend();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        pGuiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private void renderSpyglassOverlay(GuiGraphics pGuiGraphics, float pScopeScale) {
        float f = (float)Math.min(pGuiGraphics.guiWidth(), pGuiGraphics.guiHeight());
        float f1 = Math.min((float)pGuiGraphics.guiWidth() / f, (float)pGuiGraphics.guiHeight() / f) * pScopeScale;
        int i = Mth.floor(f * f1);
        int j = Mth.floor(f * f1);
        int k = (pGuiGraphics.guiWidth() - i) / 2;
        int l = (pGuiGraphics.guiHeight() - j) / 2;
        int i1 = k + i;
        int j1 = l + j;
        RenderSystem.enableBlend();
        pGuiGraphics.blit(SPYGLASS_SCOPE_LOCATION, k, l, -90, 0.0F, 0.0F, i, j, i, j);
        RenderSystem.disableBlend();
        pGuiGraphics.fill(RenderType.guiOverlay(), 0, j1, pGuiGraphics.guiWidth(), pGuiGraphics.guiHeight(), -90, -16777216);
        pGuiGraphics.fill(RenderType.guiOverlay(), 0, 0, pGuiGraphics.guiWidth(), l, -90, -16777216);
        pGuiGraphics.fill(RenderType.guiOverlay(), 0, l, k, j1, -90, -16777216);
        pGuiGraphics.fill(RenderType.guiOverlay(), i1, l, pGuiGraphics.guiWidth(), j1, -90, -16777216);
    }

    private void updateVignetteBrightness(Entity pEntity) {
        BlockPos blockpos = BlockPos.containing(pEntity.getX(), pEntity.getEyeY(), pEntity.getZ());
        float f = LightTexture.getBrightness(pEntity.level().dimensionType(), pEntity.level().getMaxLocalRawBrightness(blockpos));
        float f1 = Mth.clamp(1.0F - f, 0.0F, 1.0F);
        this.vignetteBrightness = this.vignetteBrightness + (f1 - this.vignetteBrightness) * 0.01F;
    }

    private void renderVignette(GuiGraphics pGuiGraphics, @Nullable Entity pEntity) {
        WorldBorder worldborder = this.minecraft.level.getWorldBorder();
        float f = 0.0F;
        if (pEntity != null) {
            float f1 = (float)worldborder.getDistanceToBorder(pEntity);
            double d0 = Math.min(worldborder.getLerpSpeed() * (double)worldborder.getWarningTime() * 1000.0, Math.abs(worldborder.getLerpTarget() - worldborder.getSize()));
            double d1 = Math.max((double)worldborder.getWarningBlocks(), d0);
            if ((double)f1 < d1) {
                f = 1.0F - (float)((double)f1 / d1);
            }
        }

        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(
            GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
        );
        if (f > 0.0F) {
            f = Mth.clamp(f, 0.0F, 1.0F);
            pGuiGraphics.setColor(0.0F, f, f, 1.0F);
        } else {
            float f2 = this.vignetteBrightness;
            f2 = Mth.clamp(f2, 0.0F, 1.0F);
            pGuiGraphics.setColor(f2, f2, f2, 1.0F);
        }

        pGuiGraphics.blit(VIGNETTE_LOCATION, 0, 0, -90, 0.0F, 0.0F, pGuiGraphics.guiWidth(), pGuiGraphics.guiHeight(), pGuiGraphics.guiWidth(), pGuiGraphics.guiHeight());
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        pGuiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }

    private void renderPortalOverlay(GuiGraphics pGuiGraphics, float pAlpha) {
        if (pAlpha < 1.0F) {
            pAlpha *= pAlpha;
            pAlpha *= pAlpha;
            pAlpha = pAlpha * 0.8F + 0.2F;
        }

        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        pGuiGraphics.setColor(1.0F, 1.0F, 1.0F, pAlpha);
        TextureAtlasSprite textureatlassprite = this.minecraft.getBlockRenderer().getBlockModelShaper().getParticleIcon(Blocks.NETHER_PORTAL.defaultBlockState());
        pGuiGraphics.blit(0, 0, -90, pGuiGraphics.guiWidth(), pGuiGraphics.guiHeight(), textureatlassprite);
        RenderSystem.disableBlend();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        pGuiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private void renderSlot(GuiGraphics pGuiGraphics, int pX, int pY, DeltaTracker pDeltaTracker, Player pPlayer, ItemStack pStack, int pSeed) {
        if (!pStack.isEmpty()) {
            float f = (float)pStack.getPopTime() - pDeltaTracker.getGameTimeDeltaPartialTick(false);
            if (f > 0.0F) {
                float f1 = 1.0F + f / 5.0F;
                pGuiGraphics.pose().pushPose();
                pGuiGraphics.pose().translate((float)(pX + 8), (float)(pY + 12), 0.0F);
                pGuiGraphics.pose().scale(1.0F / f1, (f1 + 1.0F) / 2.0F, 1.0F);
                pGuiGraphics.pose().translate((float)(-(pX + 8)), (float)(-(pY + 12)), 0.0F);
            }

            pGuiGraphics.renderItem(pPlayer, pStack, pX, pY, pSeed);
            if (f > 0.0F) {
                pGuiGraphics.pose().popPose();
            }

            pGuiGraphics.renderItemDecorations(this.minecraft.font, pStack, pX, pY);
        }
    }

    public void tick(boolean pPause) {
        this.tickAutosaveIndicator();
        if (!pPause) {
            this.tick();
        }
    }

    private void tick() {
        if (this.overlayMessageTime > 0) {
            this.overlayMessageTime--;
        }

        if (this.titleTime > 0) {
            this.titleTime--;
            if (this.titleTime <= 0) {
                this.title = null;
                this.subtitle = null;
            }
        }

        this.tickCount++;
        Entity entity = this.minecraft.getCameraEntity();
        if (entity != null) {
            this.updateVignetteBrightness(entity);
        }

        if (this.minecraft.player != null) {
            ItemStack itemstack = this.minecraft.player.getInventory().getSelected();
            if (itemstack.isEmpty()) {
                this.toolHighlightTimer = 0;
            } else if (this.lastToolHighlight.isEmpty() || !itemstack.is(this.lastToolHighlight.getItem()) || !itemstack.getHoverName().equals(this.lastToolHighlight.getHoverName()) || !itemstack.getHighlightTip(itemstack.getHoverName()).equals(lastToolHighlight.getHighlightTip(lastToolHighlight.getHoverName()))) {
                this.toolHighlightTimer = (int)(40.0 * this.minecraft.options.notificationDisplayTime().get());
            } else if (this.toolHighlightTimer > 0) {
                this.toolHighlightTimer--;
            }

            this.lastToolHighlight = itemstack;
        }

        this.chat.tick();
    }

    private void tickAutosaveIndicator() {
        MinecraftServer minecraftserver = this.minecraft.getSingleplayerServer();
        boolean flag = minecraftserver != null && minecraftserver.isCurrentlySaving();
        this.lastAutosaveIndicatorValue = this.autosaveIndicatorValue;
        this.autosaveIndicatorValue = Mth.lerp(0.2F, this.autosaveIndicatorValue, flag ? 1.0F : 0.0F);
    }

    public void setNowPlaying(Component pDisplayName) {
        Component component = Component.translatable("record.nowPlaying", pDisplayName);
        this.setOverlayMessage(component, true);
        this.minecraft.getNarrator().sayNow(component);
    }

    public void setOverlayMessage(Component pComponent, boolean pAnimateColor) {
        this.setChatDisabledByPlayerShown(false);
        this.overlayMessageString = pComponent;
        this.overlayMessageTime = 60;
        this.animateOverlayMessageColor = pAnimateColor;
    }

    public void setChatDisabledByPlayerShown(boolean pChatDisabledByPlayerShown) {
        this.chatDisabledByPlayerShown = pChatDisabledByPlayerShown;
    }

    public boolean isShowingChatDisabledByPlayer() {
        return this.chatDisabledByPlayerShown && this.overlayMessageTime > 0;
    }

    public void setTimes(int pTitleFadeInTime, int pTitleStayTime, int pTitleFadeOutTime) {
        if (pTitleFadeInTime >= 0) {
            this.titleFadeInTime = pTitleFadeInTime;
        }

        if (pTitleStayTime >= 0) {
            this.titleStayTime = pTitleStayTime;
        }

        if (pTitleFadeOutTime >= 0) {
            this.titleFadeOutTime = pTitleFadeOutTime;
        }

        if (this.titleTime > 0) {
            this.titleTime = this.titleFadeInTime + this.titleStayTime + this.titleFadeOutTime;
        }
    }

    public void setSubtitle(Component pSubtitle) {
        this.subtitle = pSubtitle;
    }

    public void setTitle(Component pTitle) {
        this.title = pTitle;
        this.titleTime = this.titleFadeInTime + this.titleStayTime + this.titleFadeOutTime;
    }

    public void clear() {
        this.title = null;
        this.subtitle = null;
        this.titleTime = 0;
    }

    public ChatComponent getChat() {
        return this.chat;
    }

    public int getGuiTicks() {
        return this.tickCount;
    }

    public Font getFont() {
        return this.minecraft.font;
    }

    public SpectatorGui getSpectatorGui() {
        return this.spectatorGui;
    }

    public PlayerTabOverlay getTabList() {
        return this.tabList;
    }

    public void onDisconnected() {
        this.tabList.reset();
        this.bossOverlay.reset();
        this.minecraft.getToasts().clear();
        this.debugOverlay.reset();
        this.chat.clearMessages(true);
    }

    public BossHealthOverlay getBossOverlay() {
        return this.bossOverlay;
    }

    public DebugScreenOverlay getDebugOverlay() {
        return this.debugOverlay;
    }

    public void clearCache() {
        this.debugOverlay.clearChunkCache();
    }

    public void renderSavingIndicator(GuiGraphics pGuiGraphics, DeltaTracker pDeltaTracker) {
        if (this.minecraft.options.showAutosaveIndicator().get() && (this.autosaveIndicatorValue > 0.0F || this.lastAutosaveIndicatorValue > 0.0F)) {
            int i = Mth.floor(255.0F * Mth.clamp(Mth.lerp(pDeltaTracker.getRealtimeDeltaTicks(), this.lastAutosaveIndicatorValue, this.autosaveIndicatorValue), 0.0F, 1.0F));
            if (i > 8) {
                Font font = this.getFont();
                int j = font.width(SAVING_TEXT);
                int k = FastColor.ARGB32.color(i, -1);
                int l = pGuiGraphics.guiWidth() - j - 2;
                int i1 = pGuiGraphics.guiHeight() - 35;
                pGuiGraphics.drawStringWithBackdrop(font, SAVING_TEXT, l, i1, j, k);
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    static enum HeartType {
        CONTAINER(
            ResourceLocation.withDefaultNamespace("hud/heart/container"),
            ResourceLocation.withDefaultNamespace("hud/heart/container_blinking"),
            ResourceLocation.withDefaultNamespace("hud/heart/container"),
            ResourceLocation.withDefaultNamespace("hud/heart/container_blinking"),
            ResourceLocation.withDefaultNamespace("hud/heart/container_hardcore"),
            ResourceLocation.withDefaultNamespace("hud/heart/container_hardcore_blinking"),
            ResourceLocation.withDefaultNamespace("hud/heart/container_hardcore"),
            ResourceLocation.withDefaultNamespace("hud/heart/container_hardcore_blinking")
        ),
        NORMAL(
            ResourceLocation.withDefaultNamespace("hud/heart/full"),
            ResourceLocation.withDefaultNamespace("hud/heart/full_blinking"),
            ResourceLocation.withDefaultNamespace("hud/heart/half"),
            ResourceLocation.withDefaultNamespace("hud/heart/half_blinking"),
            ResourceLocation.withDefaultNamespace("hud/heart/hardcore_full"),
            ResourceLocation.withDefaultNamespace("hud/heart/hardcore_full_blinking"),
            ResourceLocation.withDefaultNamespace("hud/heart/hardcore_half"),
            ResourceLocation.withDefaultNamespace("hud/heart/hardcore_half_blinking")
        ),
        POISIONED(
            ResourceLocation.withDefaultNamespace("hud/heart/poisoned_full"),
            ResourceLocation.withDefaultNamespace("hud/heart/poisoned_full_blinking"),
            ResourceLocation.withDefaultNamespace("hud/heart/poisoned_half"),
            ResourceLocation.withDefaultNamespace("hud/heart/poisoned_half_blinking"),
            ResourceLocation.withDefaultNamespace("hud/heart/poisoned_hardcore_full"),
            ResourceLocation.withDefaultNamespace("hud/heart/poisoned_hardcore_full_blinking"),
            ResourceLocation.withDefaultNamespace("hud/heart/poisoned_hardcore_half"),
            ResourceLocation.withDefaultNamespace("hud/heart/poisoned_hardcore_half_blinking")
        ),
        WITHERED(
            ResourceLocation.withDefaultNamespace("hud/heart/withered_full"),
            ResourceLocation.withDefaultNamespace("hud/heart/withered_full_blinking"),
            ResourceLocation.withDefaultNamespace("hud/heart/withered_half"),
            ResourceLocation.withDefaultNamespace("hud/heart/withered_half_blinking"),
            ResourceLocation.withDefaultNamespace("hud/heart/withered_hardcore_full"),
            ResourceLocation.withDefaultNamespace("hud/heart/withered_hardcore_full_blinking"),
            ResourceLocation.withDefaultNamespace("hud/heart/withered_hardcore_half"),
            ResourceLocation.withDefaultNamespace("hud/heart/withered_hardcore_half_blinking")
        ),
        ABSORBING(
            ResourceLocation.withDefaultNamespace("hud/heart/absorbing_full"),
            ResourceLocation.withDefaultNamespace("hud/heart/absorbing_full_blinking"),
            ResourceLocation.withDefaultNamespace("hud/heart/absorbing_half"),
            ResourceLocation.withDefaultNamespace("hud/heart/absorbing_half_blinking"),
            ResourceLocation.withDefaultNamespace("hud/heart/absorbing_hardcore_full"),
            ResourceLocation.withDefaultNamespace("hud/heart/absorbing_hardcore_full_blinking"),
            ResourceLocation.withDefaultNamespace("hud/heart/absorbing_hardcore_half"),
            ResourceLocation.withDefaultNamespace("hud/heart/absorbing_hardcore_half_blinking")
        ),
        FROZEN(
            ResourceLocation.withDefaultNamespace("hud/heart/frozen_full"),
            ResourceLocation.withDefaultNamespace("hud/heart/frozen_full_blinking"),
            ResourceLocation.withDefaultNamespace("hud/heart/frozen_half"),
            ResourceLocation.withDefaultNamespace("hud/heart/frozen_half_blinking"),
            ResourceLocation.withDefaultNamespace("hud/heart/frozen_hardcore_full"),
            ResourceLocation.withDefaultNamespace("hud/heart/frozen_hardcore_full_blinking"),
            ResourceLocation.withDefaultNamespace("hud/heart/frozen_hardcore_half"),
            ResourceLocation.withDefaultNamespace("hud/heart/frozen_hardcore_half_blinking")
        );

        private final ResourceLocation full;
        private final ResourceLocation fullBlinking;
        private final ResourceLocation half;
        private final ResourceLocation halfBlinking;
        private final ResourceLocation hardcoreFull;
        private final ResourceLocation hardcoreFullBlinking;
        private final ResourceLocation hardcoreHalf;
        private final ResourceLocation hardcoreHalfBlinking;

        private HeartType(
            final ResourceLocation pFull,
            final ResourceLocation pFullBlinking,
            final ResourceLocation pHalf,
            final ResourceLocation pHalfBlinking,
            final ResourceLocation pHardcoreFull,
            final ResourceLocation pHardcoreBlinking,
            final ResourceLocation pHardcoreHalf,
            final ResourceLocation pHardcoreHalfBlinking
        ) {
            this.full = pFull;
            this.fullBlinking = pFullBlinking;
            this.half = pHalf;
            this.halfBlinking = pHalfBlinking;
            this.hardcoreFull = pHardcoreFull;
            this.hardcoreFullBlinking = pHardcoreBlinking;
            this.hardcoreHalf = pHardcoreHalf;
            this.hardcoreHalfBlinking = pHardcoreHalfBlinking;
        }

        public ResourceLocation getSprite(boolean pHardcore, boolean pHalfHeart, boolean pBlinking) {
            if (!pHardcore) {
                if (pHalfHeart) {
                    return pBlinking ? this.halfBlinking : this.half;
                } else {
                    return pBlinking ? this.fullBlinking : this.full;
                }
            } else if (pHalfHeart) {
                return pBlinking ? this.hardcoreHalfBlinking : this.hardcoreHalf;
            } else {
                return pBlinking ? this.hardcoreFullBlinking : this.hardcoreFull;
            }
        }

        static Gui.HeartType forPlayer(Player pPlayer) {
            Gui.HeartType gui$hearttype;
            if (pPlayer.hasEffect(MobEffects.POISON)) {
                gui$hearttype = POISIONED;
            } else if (pPlayer.hasEffect(MobEffects.WITHER)) {
                gui$hearttype = WITHERED;
            } else if (pPlayer.isFullyFrozen()) {
                gui$hearttype = FROZEN;
            } else {
                gui$hearttype = NORMAL;
            }

            return gui$hearttype;
        }
    }
}
