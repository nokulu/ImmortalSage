package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.TaggedChoice.TaggedChoiceType;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.OptionalDynamic;
import java.util.Map;
import net.minecraft.util.datafix.ComponentDataFixUtils;

public class BannerEntityCustomNameToOverrideComponentFix extends DataFix {
    public BannerEntityCustomNameToOverrideComponentFix(Schema pOutputSchema) {
        super(pOutputSchema, false);
    }

    @Override
    public TypeRewriteRule makeRule() {
        Type<?> type = this.getInputSchema().getType(References.BLOCK_ENTITY);
        TaggedChoiceType<?> taggedchoicetype = this.getInputSchema().findChoiceType(References.BLOCK_ENTITY);
        OpticFinder<?> opticfinder = type.findField("components");
        return this.fixTypeEverywhereTyped("Banner entity custom_name to item_name component fix", type, p_332332_ -> {
            Object object = p_332332_.get(taggedchoicetype.finder()).getFirst();
            return object.equals("minecraft:banner") ? this.fix(p_332332_, opticfinder) : p_332332_;
        });
    }

    private Typed<?> fix(Typed<?> pData, OpticFinder<?> pFinder) {
        Dynamic<?> dynamic = pData.getOptional(DSL.remainderFinder()).orElseThrow();
        OptionalDynamic<?> optionaldynamic = dynamic.get("CustomName");
        boolean flag = optionaldynamic.asString()
            .result()
            .flatMap(ComponentDataFixUtils::extractTranslationString)
            .filter(p_334057_ -> p_334057_.equals("block.minecraft.ominous_banner"))
            .isPresent();
        if (flag) {
            Typed<?> typed = pData.getOrCreateTyped(pFinder)
                .update(
                    DSL.remainderFinder(),
                    p_335676_ -> p_335676_.set("minecraft:item_name", optionaldynamic.result().get())
                            .set("minecraft:hide_additional_tooltip", p_335676_.createMap(Map.of()))
                );
            return pData.set(pFinder, typed).set(DSL.remainderFinder(), dynamic.remove("CustomName"));
        } else {
            return pData;
        }
    }
}