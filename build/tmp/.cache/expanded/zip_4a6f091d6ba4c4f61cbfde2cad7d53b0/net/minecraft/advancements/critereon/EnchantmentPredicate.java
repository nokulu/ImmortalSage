package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

public record EnchantmentPredicate(Optional<HolderSet<Enchantment>> enchantments, MinMaxBounds.Ints level) {
    public static final Codec<EnchantmentPredicate> CODEC = RecordCodecBuilder.create(
        p_340753_ -> p_340753_.group(
                    RegistryCodecs.homogeneousList(Registries.ENCHANTMENT).optionalFieldOf("enchantments").forGetter(EnchantmentPredicate::enchantments),
                    MinMaxBounds.Ints.CODEC.optionalFieldOf("levels", MinMaxBounds.Ints.ANY).forGetter(EnchantmentPredicate::level)
                )
                .apply(p_340753_, EnchantmentPredicate::new)
    );

    public EnchantmentPredicate(Holder<Enchantment> pEnchantment, MinMaxBounds.Ints pLevel) {
        this(Optional.of(HolderSet.direct(pEnchantment)), pLevel);
    }

    public EnchantmentPredicate(HolderSet<Enchantment> pEnchantments, MinMaxBounds.Ints pLevel) {
        this(Optional.of(pEnchantments), pLevel);
    }

    public boolean containedIn(ItemEnchantments pEnchantments) {
        if (this.enchantments.isPresent()) {
            for (Holder<Enchantment> holder : this.enchantments.get()) {
                if (this.matchesEnchantment(pEnchantments, holder)) {
                    return true;
                }
            }

            return false;
        } else if (this.level != MinMaxBounds.Ints.ANY) {
            for (Entry<Holder<Enchantment>> entry : pEnchantments.entrySet()) {
                if (this.level.matches(entry.getIntValue())) {
                    return true;
                }
            }

            return false;
        } else {
            return !pEnchantments.isEmpty();
        }
    }

    private boolean matchesEnchantment(ItemEnchantments pItemEnchantments, Holder<Enchantment> pEnchantment) {
        int i = pItemEnchantments.getLevel(pEnchantment);
        if (i == 0) {
            return false;
        } else {
            return this.level == MinMaxBounds.Ints.ANY ? true : this.level.matches(i);
        }
    }
}