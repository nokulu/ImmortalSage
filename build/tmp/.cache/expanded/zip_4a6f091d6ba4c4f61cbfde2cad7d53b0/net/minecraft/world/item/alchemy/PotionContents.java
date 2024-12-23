package net.minecraft.world.item.alchemy;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.FastColor;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;

public record PotionContents(Optional<Holder<Potion>> potion, Optional<Integer> customColor, List<MobEffectInstance> customEffects) {
    public static final PotionContents EMPTY = new PotionContents(Optional.empty(), Optional.empty(), List.of());
    private static final Component NO_EFFECT = Component.translatable("effect.none").withStyle(ChatFormatting.GRAY);
    private static final int BASE_POTION_COLOR = -13083194;
    private static final Codec<PotionContents> FULL_CODEC = RecordCodecBuilder.create(
        p_341577_ -> p_341577_.group(
                    Potion.CODEC.optionalFieldOf("potion").forGetter(PotionContents::potion),
                    Codec.INT.optionalFieldOf("custom_color").forGetter(PotionContents::customColor),
                    MobEffectInstance.CODEC.listOf().optionalFieldOf("custom_effects", List.of()).forGetter(PotionContents::customEffects)
                )
                .apply(p_341577_, PotionContents::new)
    );
    public static final Codec<PotionContents> CODEC = Codec.withAlternative(FULL_CODEC, Potion.CODEC, PotionContents::new);
    public static final StreamCodec<RegistryFriendlyByteBuf, PotionContents> STREAM_CODEC = StreamCodec.composite(
        Potion.STREAM_CODEC.apply(ByteBufCodecs::optional),
        PotionContents::potion,
        ByteBufCodecs.INT.apply(ByteBufCodecs::optional),
        PotionContents::customColor,
        MobEffectInstance.STREAM_CODEC.apply(ByteBufCodecs.list()),
        PotionContents::customEffects,
        PotionContents::new
    );

    public PotionContents(Holder<Potion> p_335062_) {
        this(Optional.of(p_335062_), Optional.empty(), List.of());
    }

    public static ItemStack createItemStack(Item pItem, Holder<Potion> pPotion) {
        ItemStack itemstack = new ItemStack(pItem);
        itemstack.set(DataComponents.POTION_CONTENTS, new PotionContents(pPotion));
        return itemstack;
    }

    public boolean is(Holder<Potion> pPotion) {
        return this.potion.isPresent() && this.potion.get().is(pPotion) && this.customEffects.isEmpty();
    }

    public Iterable<MobEffectInstance> getAllEffects() {
        if (this.potion.isEmpty()) {
            return this.customEffects;
        } else {
            return (Iterable<MobEffectInstance>)(this.customEffects.isEmpty()
                ? this.potion.get().value().getEffects()
                : Iterables.concat(this.potion.get().value().getEffects(), this.customEffects));
        }
    }

    public void forEachEffect(Consumer<MobEffectInstance> pAction) {
        if (this.potion.isPresent()) {
            for (MobEffectInstance mobeffectinstance : this.potion.get().value().getEffects()) {
                pAction.accept(new MobEffectInstance(mobeffectinstance));
            }
        }

        for (MobEffectInstance mobeffectinstance1 : this.customEffects) {
            pAction.accept(new MobEffectInstance(mobeffectinstance1));
        }
    }

    public PotionContents withPotion(Holder<Potion> pPotion) {
        return new PotionContents(Optional.of(pPotion), this.customColor, this.customEffects);
    }

    public PotionContents withEffectAdded(MobEffectInstance pEffect) {
        return new PotionContents(this.potion, this.customColor, Util.copyAndAdd(this.customEffects, pEffect));
    }

    public int getColor() {
        return this.customColor.isPresent() ? this.customColor.get() : getColor(this.getAllEffects());
    }

    public static int getColor(Holder<Potion> pPotion) {
        return getColor(pPotion.value().getEffects());
    }

    public static int getColor(Iterable<MobEffectInstance> pEffects) {
        return getColorOptional(pEffects).orElse(-13083194);
    }

    public static OptionalInt getColorOptional(Iterable<MobEffectInstance> pEffects) {
        int i = 0;
        int j = 0;
        int k = 0;
        int l = 0;

        for (MobEffectInstance mobeffectinstance : pEffects) {
            if (mobeffectinstance.isVisible()) {
                int i1 = mobeffectinstance.getEffect().value().getColor();
                int j1 = mobeffectinstance.getAmplifier() + 1;
                i += j1 * FastColor.ARGB32.red(i1);
                j += j1 * FastColor.ARGB32.green(i1);
                k += j1 * FastColor.ARGB32.blue(i1);
                l += j1;
            }
        }

        return l == 0 ? OptionalInt.empty() : OptionalInt.of(FastColor.ARGB32.color(i / l, j / l, k / l));
    }

    public boolean hasEffects() {
        return !this.customEffects.isEmpty() ? true : this.potion.isPresent() && !this.potion.get().value().getEffects().isEmpty();
    }

    public List<MobEffectInstance> customEffects() {
        return Lists.transform(this.customEffects, MobEffectInstance::new);
    }

    public void addPotionTooltip(Consumer<Component> pTooltipAdder, float pDurationFactor, float pTicksPerSecond) {
        addPotionTooltip(this.getAllEffects(), pTooltipAdder, pDurationFactor, pTicksPerSecond);
    }

    public static void addPotionTooltip(Iterable<MobEffectInstance> pEffects, Consumer<Component> pTooltipAdder, float pDurationFactor, float pTicksPerSecond) {
        List<Pair<Holder<Attribute>, AttributeModifier>> list = Lists.newArrayList();
        boolean flag = true;

        for (MobEffectInstance mobeffectinstance : pEffects) {
            flag = false;
            MutableComponent mutablecomponent = Component.translatable(mobeffectinstance.getDescriptionId());
            Holder<MobEffect> holder = mobeffectinstance.getEffect();
            holder.value().createModifiers(mobeffectinstance.getAmplifier(), (p_329075_, p_331827_) -> list.add(new Pair<>(p_329075_, p_331827_)));
            if (mobeffectinstance.getAmplifier() > 0) {
                mutablecomponent = Component.translatable(
                    "potion.withAmplifier", mutablecomponent, Component.translatable("potion.potency." + mobeffectinstance.getAmplifier())
                );
            }

            if (!mobeffectinstance.endsWithin(20)) {
                mutablecomponent = Component.translatable(
                    "potion.withDuration", mutablecomponent, MobEffectUtil.formatDuration(mobeffectinstance, pDurationFactor, pTicksPerSecond)
                );
            }

            pTooltipAdder.accept(mutablecomponent.withStyle(holder.value().getCategory().getTooltipFormatting()));
        }

        if (flag) {
            pTooltipAdder.accept(NO_EFFECT);
        }

        if (!list.isEmpty()) {
            pTooltipAdder.accept(CommonComponents.EMPTY);
            pTooltipAdder.accept(Component.translatable("potion.whenDrank").withStyle(ChatFormatting.DARK_PURPLE));

            for (Pair<Holder<Attribute>, AttributeModifier> pair : list) {
                AttributeModifier attributemodifier = pair.getSecond();
                double d1 = attributemodifier.amount();
                double d0;
                if (attributemodifier.operation() != AttributeModifier.Operation.ADD_MULTIPLIED_BASE
                    && attributemodifier.operation() != AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL) {
                    d0 = attributemodifier.amount();
                } else {
                    d0 = attributemodifier.amount() * 100.0;
                }

                if (d1 > 0.0) {
                    pTooltipAdder.accept(
                        Component.translatable(
                                "attribute.modifier.plus." + attributemodifier.operation().id(),
                                ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(d0),
                                Component.translatable(pair.getFirst().value().getDescriptionId())
                            )
                            .withStyle(ChatFormatting.BLUE)
                    );
                } else if (d1 < 0.0) {
                    d0 *= -1.0;
                    pTooltipAdder.accept(
                        Component.translatable(
                                "attribute.modifier.take." + attributemodifier.operation().id(),
                                ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(d0),
                                Component.translatable(pair.getFirst().value().getDescriptionId())
                            )
                            .withStyle(ChatFormatting.RED)
                    );
                }
            }
        }
    }
}