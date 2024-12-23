package net.minecraft.world.level.storage.loot.functions;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.slf4j.Logger;

public class FunctionReference extends LootItemConditionalFunction {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final MapCodec<FunctionReference> CODEC = RecordCodecBuilder.mapCodec(
        p_327570_ -> commonFields(p_327570_)
                .and(ResourceKey.codec(Registries.ITEM_MODIFIER).fieldOf("name").forGetter(p_327571_ -> p_327571_.name))
                .apply(p_327570_, FunctionReference::new)
    );
    private final ResourceKey<LootItemFunction> name;

    private FunctionReference(List<LootItemCondition> p_298565_, ResourceKey<LootItemFunction> p_327927_) {
        super(p_298565_);
        this.name = p_327927_;
    }

    @Override
    public LootItemFunctionType<FunctionReference> getType() {
        return LootItemFunctions.REFERENCE;
    }

    @Override
    public void validate(ValidationContext pContext) {
        if (!pContext.allowsReferences()) {
            pContext.reportProblem("Uses reference to " + this.name.location() + ", but references are not allowed");
        } else if (pContext.hasVisitedElement(this.name)) {
            pContext.reportProblem("Function " + this.name.location() + " is recursively called");
        } else {
            super.validate(pContext);
            pContext.resolver()
                .get(Registries.ITEM_MODIFIER, this.name)
                .ifPresentOrElse(
                    p_327573_ -> p_327573_.value().validate(pContext.enterElement(".{" + this.name.location() + "}", this.name)),
                    () -> pContext.reportProblem("Unknown function table called " + this.name.location())
                );
        }
    }

    @Override
    protected ItemStack run(ItemStack pStack, LootContext pContext) {
        LootItemFunction lootitemfunction = pContext.getResolver().get(Registries.ITEM_MODIFIER, this.name).map(Holder::value).orElse(null);
        if (lootitemfunction == null) {
            LOGGER.warn("Unknown function: {}", this.name.location());
            return pStack;
        } else {
            LootContext.VisitedEntry<?> visitedentry = LootContext.createVisitedEntry(lootitemfunction);
            if (pContext.pushVisitedElement(visitedentry)) {
                ItemStack itemstack;
                try {
                    itemstack = lootitemfunction.apply(pStack, pContext);
                } finally {
                    pContext.popVisitedElement(visitedentry);
                }

                return itemstack;
            } else {
                LOGGER.warn("Detected infinite loop in loot tables");
                return pStack;
            }
        }
    }

    public static LootItemConditionalFunction.Builder<?> functionReference(ResourceKey<LootItemFunction> pKey) {
        return simpleBuilder(p_327575_ -> new FunctionReference(p_327575_, pKey));
    }
}