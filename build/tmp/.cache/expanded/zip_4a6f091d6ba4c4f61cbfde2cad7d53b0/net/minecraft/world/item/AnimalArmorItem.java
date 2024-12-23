package net.minecraft.world.item;

import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

public class AnimalArmorItem extends ArmorItem {
    private final ResourceLocation textureLocation;
    @Nullable
    private final ResourceLocation overlayTextureLocation;
    private final AnimalArmorItem.BodyType bodyType;

    public AnimalArmorItem(Holder<ArmorMaterial> pArmorMaterial, AnimalArmorItem.BodyType pBodyType, boolean pHasOverlay, Item.Properties pProperties) {
        super(pArmorMaterial, ArmorItem.Type.BODY, pProperties);
        this.bodyType = pBodyType;
        ResourceLocation resourcelocation = pBodyType.textureLocator.apply(pArmorMaterial.unwrapKey().orElseThrow().location());
        this.textureLocation = resourcelocation.withSuffix(".png");
        if (pHasOverlay) {
            this.overlayTextureLocation = resourcelocation.withSuffix("_overlay.png");
        } else {
            this.overlayTextureLocation = null;
        }
    }

    public ResourceLocation getTexture() {
        return this.textureLocation;
    }

    @Nullable
    public ResourceLocation getOverlayTexture() {
        return this.overlayTextureLocation;
    }

    public AnimalArmorItem.BodyType getBodyType() {
        return this.bodyType;
    }

    @Override
    public SoundEvent getBreakingSound() {
        return this.bodyType.breakingSound;
    }

    @Override
    public boolean isEnchantable(ItemStack pStack) {
        return false;
    }

    public static enum BodyType implements net.minecraftforge.common.IExtensibleEnum {
        EQUESTRIAN(p_331659_ -> p_331659_.withPath(p_329177_ -> "textures/entity/horse/armor/horse_armor_" + p_329177_), SoundEvents.ITEM_BREAK),
        CANINE(p_333424_ -> p_333424_.withPath("textures/entity/wolf/wolf_armor"), SoundEvents.WOLF_ARMOR_BREAK);

        final Function<ResourceLocation, ResourceLocation> textureLocator;
        final SoundEvent breakingSound;

        private BodyType(final Function<ResourceLocation, ResourceLocation> pTextureLocator, final SoundEvent pBreakingSound) {
            this.textureLocator = pTextureLocator;
            this.breakingSound = pBreakingSound;
        }

        public static BodyType create(String name, final Function<ResourceLocation, ResourceLocation> path, SoundEvent sound) {
            throw new IllegalStateException("Enum not extended");
        }
    }
}
