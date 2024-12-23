package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.OptionalDynamic;
import net.minecraft.util.datafix.ExtraDataFixUtils;

public class LegacyDragonFightFix extends DataFix {
    public LegacyDragonFightFix(Schema pOutputSchema) {
        super(pOutputSchema, false);
    }

    private static <T> Dynamic<T> fixDragonFight(Dynamic<T> pData) {
        return pData.update("ExitPortalLocation", ExtraDataFixUtils::fixBlockPos);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped(
            "LegacyDragonFightFix", this.getInputSchema().getType(References.LEVEL), p_289787_ -> p_289787_.update(DSL.remainderFinder(), p_326607_ -> {
                    OptionalDynamic<?> optionaldynamic = p_326607_.get("DragonFight");
                    if (optionaldynamic.result().isPresent()) {
                        return p_326607_;
                    } else {
                        Dynamic<?> dynamic = p_326607_.get("DimensionData").get("1").get("DragonFight").orElseEmptyMap();
                        return p_326607_.set("DragonFight", fixDragonFight(dynamic));
                    }
                })
        );
    }
}