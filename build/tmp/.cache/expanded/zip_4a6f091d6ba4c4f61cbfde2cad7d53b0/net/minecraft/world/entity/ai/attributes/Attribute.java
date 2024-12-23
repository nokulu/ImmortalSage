package net.minecraft.world.entity.ai.attributes;

import com.mojang.serialization.Codec;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * Defines an entity attribute. These are properties of entities that can be dynamically modified.
 * @see net.minecraft.core.Registry#ATTRIBUTE
 */
public class Attribute {
    public static final Codec<Holder<Attribute>> CODEC = BuiltInRegistries.ATTRIBUTE.holderByNameCodec();
    public static final StreamCodec<RegistryFriendlyByteBuf, Holder<Attribute>> STREAM_CODEC = ByteBufCodecs.holderRegistry(Registries.ATTRIBUTE);
    private final double defaultValue;
    private boolean syncable;
    private final String descriptionId;
    private Attribute.Sentiment sentiment = Attribute.Sentiment.POSITIVE;

    protected Attribute(String pDescriptionId, double pDefaultValue) {
        this.defaultValue = pDefaultValue;
        this.descriptionId = pDescriptionId;
    }

    public double getDefaultValue() {
        return this.defaultValue;
    }

    public boolean isClientSyncable() {
        return this.syncable;
    }

    public Attribute setSyncable(boolean pWatch) {
        this.syncable = pWatch;
        return this;
    }

    public Attribute setSentiment(Attribute.Sentiment pSentiment) {
        this.sentiment = pSentiment;
        return this;
    }

    public double sanitizeValue(double pValue) {
        return pValue;
    }

    public String getDescriptionId() {
        return this.descriptionId;
    }

    public ChatFormatting getStyle(boolean pIsPositive) {
        return this.sentiment.getStyle(pIsPositive);
    }

    public static enum Sentiment {
        POSITIVE,
        NEUTRAL,
        NEGATIVE;

        public ChatFormatting getStyle(boolean pIsPositive) {
            return switch (this) {
                case POSITIVE -> pIsPositive ? ChatFormatting.BLUE : ChatFormatting.RED;
                case NEUTRAL -> ChatFormatting.GRAY;
                case NEGATIVE -> pIsPositive ? ChatFormatting.RED : ChatFormatting.BLUE;
            };
        }
    }
}