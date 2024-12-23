package net.minecraft.util.datafix.fixes;

import com.google.common.collect.Streams;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.Optional;

public class HorseBodyArmorItemFix extends NamedEntityWriteReadFix {
    private final String previousBodyArmorTag;
    private final boolean clearArmorItems;

    public HorseBodyArmorItemFix(Schema pOutputSchema, String pEntityName, String pPreviousBodyArmorTag, boolean pClearArmorItems) {
        super(pOutputSchema, true, "Horse armor fix for " + pEntityName, References.ENTITY, pEntityName);
        this.previousBodyArmorTag = pPreviousBodyArmorTag;
        this.clearArmorItems = pClearArmorItems;
    }

    @Override
    protected <T> Dynamic<T> fix(Dynamic<T> pTag) {
        Optional<? extends Dynamic<?>> optional = pTag.get(this.previousBodyArmorTag).result();
        if (optional.isPresent()) {
            Dynamic<?> dynamic = (Dynamic<?>)optional.get();
            Dynamic<T> dynamic1 = pTag.remove(this.previousBodyArmorTag);
            if (this.clearArmorItems) {
                dynamic1 = dynamic1.update(
                    "ArmorItems",
                    p_333243_ -> p_333243_.createList(
                            Streams.mapWithIndex(p_333243_.asStream(), (p_328879_, p_335895_) -> p_335895_ == 2L ? p_328879_.emptyMap() : p_328879_)
                        )
                );
                dynamic1 = dynamic1.update(
                    "ArmorDropChances",
                    p_335133_ -> p_335133_.createList(
                            Streams.mapWithIndex(p_335133_.asStream(), (p_333050_, p_334688_) -> p_334688_ == 2L ? p_333050_.createFloat(0.085F) : p_333050_)
                        )
                );
            }

            dynamic1 = dynamic1.set("body_armor_item", dynamic);
            return dynamic1.set("body_armor_drop_chance", pTag.createFloat(2.0F));
        } else {
            return pTag;
        }
    }
}