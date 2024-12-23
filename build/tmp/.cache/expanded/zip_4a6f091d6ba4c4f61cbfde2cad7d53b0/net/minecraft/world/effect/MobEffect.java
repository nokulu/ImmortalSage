package net.minecraft.world.effect;

import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.flag.FeatureElement;
import net.minecraft.world.flag.FeatureFlag;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;


public class MobEffect implements FeatureElement, net.minecraftforge.common.extensions.IForgeMobEffect {
    public static final Codec<Holder<MobEffect>> CODEC = BuiltInRegistries.MOB_EFFECT.holderByNameCodec();
    public static final StreamCodec<RegistryFriendlyByteBuf, Holder<MobEffect>> STREAM_CODEC = ByteBufCodecs.holderRegistry(Registries.MOB_EFFECT);
    private static final int AMBIENT_ALPHA = Mth.floor(38.25F);
    private final Map<Holder<Attribute>, MobEffect.AttributeTemplate> attributeModifiers = new Object2ObjectOpenHashMap<>();
    private final MobEffectCategory category;
    private final int color;
    private final Function<MobEffectInstance, ParticleOptions> particleFactory;
    @Nullable
    private String descriptionId;
    private int blendDurationTicks;
    private Optional<SoundEvent> soundOnAdded = Optional.empty();
    private FeatureFlagSet requiredFeatures = FeatureFlags.VANILLA_SET;

    protected MobEffect(MobEffectCategory pCategory, int pColor) {
        this.category = pCategory;
        this.color = pColor;
        this.particleFactory = p_326747_ -> {
            int i = p_326747_.isAmbient() ? AMBIENT_ALPHA : 255;
            return ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, FastColor.ARGB32.color(i, pColor));
        };
        initClient();
    }

    protected MobEffect(MobEffectCategory pCategory, int pColor, ParticleOptions pParticle) {
        this.category = pCategory;
        this.color = pColor;
        this.particleFactory = p_326745_ -> pParticle;
        initClient();
    }

    public int getBlendDurationTicks() {
        return this.blendDurationTicks;
    }

    public boolean applyEffectTick(LivingEntity pLivingEntity, int pAmplifier) {
        return true;
    }

    public void applyInstantenousEffect(@Nullable Entity pSource, @Nullable Entity pIndirectSource, LivingEntity pLivingEntity, int pAmplifier, double pHealth) {
        this.applyEffectTick(pLivingEntity, pAmplifier);
    }

    public boolean shouldApplyEffectTickThisTick(int pDuration, int pAmplifier) {
        return false;
    }

    public void onEffectStarted(LivingEntity pLivingEntity, int pAmplifier) {
    }

    public void onEffectAdded(LivingEntity pLivingEntity, int pAmplifier) {
        this.soundOnAdded
            .ifPresent(
                p_341258_ -> pLivingEntity.level()
                        .playSound(null, pLivingEntity.getX(), pLivingEntity.getY(), pLivingEntity.getZ(), p_341258_, pLivingEntity.getSoundSource(), 1.0F, 1.0F)
            );
    }

    public void onMobRemoved(LivingEntity pLivingEntity, int pAmplifier, Entity.RemovalReason pReason) {
    }

    public void onMobHurt(LivingEntity pLivingEntity, int pAmplifier, DamageSource pDamageSource, float pAmount) {
    }

    public boolean isInstantenous() {
        return false;
    }

    protected String getOrCreateDescriptionId() {
        if (this.descriptionId == null) {
            this.descriptionId = Util.makeDescriptionId("effect", BuiltInRegistries.MOB_EFFECT.getKey(this));
        }

        return this.descriptionId;
    }

    public String getDescriptionId() {
        return this.getOrCreateDescriptionId();
    }

    public Component getDisplayName() {
        return Component.translatable(this.getDescriptionId());
    }

    public MobEffectCategory getCategory() {
        return this.category;
    }

    public int getColor() {
        return this.color;
    }

    public MobEffect addAttributeModifier(Holder<Attribute> pAttribute, ResourceLocation pId, double pAmount, AttributeModifier.Operation pOperation) {
        this.attributeModifiers.put(pAttribute, new MobEffect.AttributeTemplate(pId, pAmount, pOperation));
        return this;
    }

    public MobEffect setBlendDuration(int pBlendDuration) {
        this.blendDurationTicks = pBlendDuration;
        return this;
    }

    public void createModifiers(int pAmplifier, BiConsumer<Holder<Attribute>, AttributeModifier> pOutput) {
        this.attributeModifiers.forEach((p_341255_, p_341256_) -> pOutput.accept((Holder<Attribute>)p_341255_, p_341256_.create(pAmplifier)));
    }

    public void removeAttributeModifiers(AttributeMap pAttributeMap) {
        for (Entry<Holder<Attribute>, MobEffect.AttributeTemplate> entry : this.attributeModifiers.entrySet()) {
            AttributeInstance attributeinstance = pAttributeMap.getInstance(entry.getKey());
            if (attributeinstance != null) {
                attributeinstance.removeModifier(entry.getValue().id());
            }
        }
    }

    public void addAttributeModifiers(AttributeMap pAttributeMap, int pAmplifier) {
        for (Entry<Holder<Attribute>, MobEffect.AttributeTemplate> entry : this.attributeModifiers.entrySet()) {
            AttributeInstance attributeinstance = pAttributeMap.getInstance(entry.getKey());
            if (attributeinstance != null) {
                attributeinstance.removeModifier(entry.getValue().id());
                attributeinstance.addPermanentModifier(entry.getValue().create(pAmplifier));
            }
        }
    }

    public boolean isBeneficial() {
        return this.category == MobEffectCategory.BENEFICIAL;
    }

    public ParticleOptions createParticleOptions(MobEffectInstance pEffect) {
        return this.particleFactory.apply(pEffect);
    }

    public MobEffect withSoundOnAdded(SoundEvent pSound) {
        this.soundOnAdded = Optional.of(pSound);
        return this;
    }

    public MobEffect requiredFeatures(FeatureFlag... pRequiredFeatures) {
        this.requiredFeatures = FeatureFlags.REGISTRY.subset(pRequiredFeatures);
        return this;
    }

    @Override
    public FeatureFlagSet requiredFeatures() {
        return this.requiredFeatures;
    }

    static record AttributeTemplate(ResourceLocation id, double amount, AttributeModifier.Operation operation) {
        public AttributeModifier create(int pLevel) {
            return new AttributeModifier(this.id, this.amount * (double)(pLevel + 1), this.operation);
        }
    }

    // FORGE START
    private Object effectRenderer;

    /*
       DO NOT CALL, IT WILL DISAPPEAR IN THE FUTURE
       Call RenderProperties.getEffectRenderer instead
     */
    public Object getEffectRendererInternal() {
        return effectRenderer;
    }

    private void initClient() {
        // Minecraft instance isn't available in datagen, so don't call initializeClient if in datagen
        if (net.minecraftforge.fml.loading.FMLEnvironment.dist == net.minecraftforge.api.distmarker.Dist.CLIENT && !net.minecraftforge.fml.loading.FMLLoader.getLaunchHandler().isData()) {
            initializeClient(properties -> {
                this.effectRenderer = properties;
            });
        }
    }

    public void initializeClient(java.util.function.Consumer<net.minecraftforge.client.extensions.common.IClientMobEffectExtensions> consumer) {
    }
    // END FORGE
}
