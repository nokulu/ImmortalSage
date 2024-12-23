package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;

public abstract class ItemStackComponentRemainderFix extends DataFix {
    private final String name;
    private final String componentId;
    private final String newComponentId;

    public ItemStackComponentRemainderFix(Schema pOutputSchema, String pName, String pComponentId) {
        this(pOutputSchema, pName, pComponentId, pComponentId);
    }

    public ItemStackComponentRemainderFix(Schema pOutputSchema, String pName, String pComponentId, String pNewComponentId) {
        super(pOutputSchema, false);
        this.name = pName;
        this.componentId = pComponentId;
        this.newComponentId = pNewComponentId;
    }

    @Override
    public final TypeRewriteRule makeRule() {
        Type<?> type = this.getInputSchema().getType(References.ITEM_STACK);
        OpticFinder<?> opticfinder = type.findField("components");
        return this.fixTypeEverywhereTyped(
            this.name,
            type,
            p_329992_ -> p_329992_.updateTyped(
                    opticfinder,
                    p_332858_ -> p_332858_.update(
                            DSL.remainderFinder(), p_330335_ -> p_330335_.renameAndFixField(this.componentId, this.newComponentId, this::fixComponent)
                        )
                )
        );
    }

    protected abstract <T> Dynamic<T> fixComponent(Dynamic<T> p_330625_);
}