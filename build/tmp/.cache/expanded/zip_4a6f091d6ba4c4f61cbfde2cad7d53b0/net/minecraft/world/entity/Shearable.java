package net.minecraft.world.entity;

import net.minecraft.sounds.SoundSource;

public interface Shearable extends net.minecraftforge.common.IForgeShearable {
    /** @deprecated Use {@link net.minecraftforge.common.IForgeShearable#onSheared} */
    void shear(SoundSource pSource);

    /** @deprecated Use {@link net.minecraftforge.common.IForgeShearable#isShearable} */
    boolean readyForShearing();

    default boolean isShearable(net.minecraft.world.item.ItemStack item, net.minecraft.world.level.Level level, net.minecraft.core.BlockPos pos) {
        return readyForShearing();
    }
}
