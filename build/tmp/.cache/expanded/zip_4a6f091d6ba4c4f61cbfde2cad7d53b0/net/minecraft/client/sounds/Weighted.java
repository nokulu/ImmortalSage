package net.minecraft.client.sounds;

import net.minecraft.util.RandomSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * The Weighted interface represents an element with a weight in a weighted collection.
 * It is used to provide weighted selection and retrieval of elements.
 * 
 * @param <T> The type of the element
 */
@OnlyIn(Dist.CLIENT)
public interface Weighted<T> {
    int getWeight();

    T getSound(RandomSource pRandomSource);

    void preloadIfRequired(SoundEngine pEngine);
}