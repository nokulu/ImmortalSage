package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.Optional;

public class TippedArrowPotionToItemFix extends NamedEntityWriteReadFix {
    public TippedArrowPotionToItemFix(Schema pOutputSchema) {
        super(pOutputSchema, false, "TippedArrowPotionToItemFix", References.ENTITY, "minecraft:arrow");
    }

    @Override
    protected <T> Dynamic<T> fix(Dynamic<T> pTag) {
        Optional<Dynamic<T>> optional = pTag.get("Potion").result();
        Optional<Dynamic<T>> optional1 = pTag.get("custom_potion_effects").result();
        Optional<Dynamic<T>> optional2 = pTag.get("Color").result();
        return optional.isEmpty() && optional1.isEmpty() && optional2.isEmpty()
            ? pTag
            : pTag.remove("Potion").remove("custom_potion_effects").remove("Color").update("item", p_333381_ -> {
                Dynamic<?> dynamic = p_333381_.get("tag").orElseEmptyMap();
                if (optional.isPresent()) {
                    dynamic = dynamic.set("Potion", optional.get());
                }

                if (optional1.isPresent()) {
                    dynamic = dynamic.set("custom_potion_effects", optional1.get());
                }

                if (optional2.isPresent()) {
                    dynamic = dynamic.set("CustomPotionColor", optional2.get());
                }

                return p_333381_.set("tag", dynamic);
            });
    }
}