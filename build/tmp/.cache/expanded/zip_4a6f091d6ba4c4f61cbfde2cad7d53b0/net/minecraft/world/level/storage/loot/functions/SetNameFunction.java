package net.minecraft.world.level.storage.loot.functions;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.UnaryOperator;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.slf4j.Logger;

/**
 * LootItemFunction that sets a stack's name.
 * The Component for the name is optionally resolved relative to a given {@link LootContext.EntityTarget} for entity-
 * sensitive component data such as scoreboard scores.
 */
public class SetNameFunction extends LootItemConditionalFunction {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final MapCodec<SetNameFunction> CODEC = RecordCodecBuilder.mapCodec(
        p_327622_ -> commonFields(p_327622_)
                .and(
                    p_327622_.group(
                        ComponentSerialization.CODEC.optionalFieldOf("name").forGetter(p_297155_ -> p_297155_.name),
                        LootContext.EntityTarget.CODEC.optionalFieldOf("entity").forGetter(p_297165_ -> p_297165_.resolutionContext),
                        SetNameFunction.Target.CODEC
                            .optionalFieldOf("target", SetNameFunction.Target.CUSTOM_NAME)
                            .forGetter(p_327618_ -> p_327618_.target)
                    )
                )
                .apply(p_327622_, SetNameFunction::new)
    );
    private final Optional<Component> name;
    private final Optional<LootContext.EntityTarget> resolutionContext;
    private final SetNameFunction.Target target;

    private SetNameFunction(
        List<LootItemCondition> p_298434_, Optional<Component> p_299902_, Optional<LootContext.EntityTarget> p_300668_, SetNameFunction.Target p_327809_
    ) {
        super(p_298434_);
        this.name = p_299902_;
        this.resolutionContext = p_300668_;
        this.target = p_327809_;
    }

    @Override
    public LootItemFunctionType<SetNameFunction> getType() {
        return LootItemFunctions.SET_NAME;
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return this.resolutionContext.<Set<LootContextParam<?>>>map(p_297154_ -> Set.of(p_297154_.getParam())).orElse(Set.of());
    }

    public static UnaryOperator<Component> createResolver(LootContext pLootContext, @Nullable LootContext.EntityTarget pResolutionContext) {
        if (pResolutionContext != null) {
            Entity entity = pLootContext.getParamOrNull(pResolutionContext.getParam());
            if (entity != null) {
                CommandSourceStack commandsourcestack = entity.createCommandSourceStack().withPermission(2);
                return p_81147_ -> {
                    try {
                        return ComponentUtils.updateForEntity(commandsourcestack, p_81147_, entity, 0);
                    } catch (CommandSyntaxException commandsyntaxexception) {
                        LOGGER.warn("Failed to resolve text component", (Throwable)commandsyntaxexception);
                        return p_81147_;
                    }
                };
            }
        }

        return p_81152_ -> p_81152_;
    }

    @Override
    public ItemStack run(ItemStack pStack, LootContext pContext) {
        this.name.ifPresent(p_327617_ -> pStack.set(this.target.component(), createResolver(pContext, this.resolutionContext.orElse(null)).apply(p_327617_)));
        return pStack;
    }

    public static LootItemConditionalFunction.Builder<?> setName(Component pName, SetNameFunction.Target pTarget) {
        return simpleBuilder(p_327621_ -> new SetNameFunction(p_327621_, Optional.of(pName), Optional.empty(), pTarget));
    }

    public static LootItemConditionalFunction.Builder<?> setName(Component pName, SetNameFunction.Target pTarget, LootContext.EntityTarget pResolutionContext) {
        return simpleBuilder(p_327626_ -> new SetNameFunction(p_327626_, Optional.of(pName), Optional.of(pResolutionContext), pTarget));
    }

    public static enum Target implements StringRepresentable {
        CUSTOM_NAME("custom_name"),
        ITEM_NAME("item_name");

        public static final Codec<SetNameFunction.Target> CODEC = StringRepresentable.fromEnum(SetNameFunction.Target::values);
        private final String name;

        private Target(final String pName) {
            this.name = pName;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        public DataComponentType<Component> component() {
            return switch (this) {
                case CUSTOM_NAME -> DataComponents.CUSTOM_NAME;
                case ITEM_NAME -> DataComponents.ITEM_NAME;
            };
        }
    }
}