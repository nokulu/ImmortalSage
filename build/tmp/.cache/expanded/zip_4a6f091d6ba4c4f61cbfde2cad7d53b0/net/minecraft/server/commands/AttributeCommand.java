package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.Dynamic3CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public class AttributeCommand {
    private static final DynamicCommandExceptionType ERROR_NOT_LIVING_ENTITY = new DynamicCommandExceptionType(
        p_308625_ -> Component.translatableEscape("commands.attribute.failed.entity", p_308625_)
    );
    private static final Dynamic2CommandExceptionType ERROR_NO_SUCH_ATTRIBUTE = new Dynamic2CommandExceptionType(
        (p_308616_, p_308617_) -> Component.translatableEscape("commands.attribute.failed.no_attribute", p_308616_, p_308617_)
    );
    private static final Dynamic3CommandExceptionType ERROR_NO_SUCH_MODIFIER = new Dynamic3CommandExceptionType(
        (p_308629_, p_308630_, p_308631_) -> Component.translatableEscape("commands.attribute.failed.no_modifier", p_308630_, p_308629_, p_308631_)
    );
    private static final Dynamic3CommandExceptionType ERROR_MODIFIER_ALREADY_PRESENT = new Dynamic3CommandExceptionType(
        (p_308626_, p_308627_, p_308628_) -> Component.translatableEscape("commands.attribute.failed.modifier_already_present", p_308628_, p_308627_, p_308626_)
    );

    public static void register(CommandDispatcher<CommandSourceStack> pDispatcher, CommandBuildContext pContext) {
        pDispatcher.register(
            Commands.literal("attribute")
                .requires(p_212441_ -> p_212441_.hasPermission(2))
                .then(
                    Commands.argument("target", EntityArgument.entity())
                        .then(
                            Commands.argument("attribute", ResourceArgument.resource(pContext, Registries.ATTRIBUTE))
                                .then(
                                    Commands.literal("get")
                                        .executes(
                                            p_248109_ -> getAttributeValue(
                                                    p_248109_.getSource(),
                                                    EntityArgument.getEntity(p_248109_, "target"),
                                                    ResourceArgument.getAttribute(p_248109_, "attribute"),
                                                    1.0
                                                )
                                        )
                                        .then(
                                            Commands.argument("scale", DoubleArgumentType.doubleArg())
                                                .executes(
                                                    p_248104_ -> getAttributeValue(
                                                            p_248104_.getSource(),
                                                            EntityArgument.getEntity(p_248104_, "target"),
                                                            ResourceArgument.getAttribute(p_248104_, "attribute"),
                                                            DoubleArgumentType.getDouble(p_248104_, "scale")
                                                        )
                                                )
                                        )
                                )
                                .then(
                                    Commands.literal("base")
                                        .then(
                                            Commands.literal("set")
                                                .then(
                                                    Commands.argument("value", DoubleArgumentType.doubleArg())
                                                        .executes(
                                                            p_248102_ -> setAttributeBase(
                                                                    p_248102_.getSource(),
                                                                    EntityArgument.getEntity(p_248102_, "target"),
                                                                    ResourceArgument.getAttribute(p_248102_, "attribute"),
                                                                    DoubleArgumentType.getDouble(p_248102_, "value")
                                                                )
                                                        )
                                                )
                                        )
                                        .then(
                                            Commands.literal("get")
                                                .executes(
                                                    p_248112_ -> getAttributeBase(
                                                            p_248112_.getSource(),
                                                            EntityArgument.getEntity(p_248112_, "target"),
                                                            ResourceArgument.getAttribute(p_248112_, "attribute"),
                                                            1.0
                                                        )
                                                )
                                                .then(
                                                    Commands.argument("scale", DoubleArgumentType.doubleArg())
                                                        .executes(
                                                            p_248106_ -> getAttributeBase(
                                                                    p_248106_.getSource(),
                                                                    EntityArgument.getEntity(p_248106_, "target"),
                                                                    ResourceArgument.getAttribute(p_248106_, "attribute"),
                                                                    DoubleArgumentType.getDouble(p_248106_, "scale")
                                                                )
                                                        )
                                                )
                                        )
                                )
                                .then(
                                    Commands.literal("modifier")
                                        .then(
                                            Commands.literal("add")
                                                .then(
                                                    Commands.argument("id", ResourceLocationArgument.id())
                                                        .then(
                                                            Commands.argument("value", DoubleArgumentType.doubleArg())
                                                                .then(
                                                                    Commands.literal("add_value")
                                                                        .executes(
                                                                            p_341134_ -> addModifier(
                                                                                    p_341134_.getSource(),
                                                                                    EntityArgument.getEntity(p_341134_, "target"),
                                                                                    ResourceArgument.getAttribute(p_341134_, "attribute"),
                                                                                    ResourceLocationArgument.getId(p_341134_, "id"),
                                                                                    DoubleArgumentType.getDouble(p_341134_, "value"),
                                                                                    AttributeModifier.Operation.ADD_VALUE
                                                                                )
                                                                        )
                                                                )
                                                                .then(
                                                                    Commands.literal("add_multiplied_base")
                                                                        .executes(
                                                                            p_341141_ -> addModifier(
                                                                                    p_341141_.getSource(),
                                                                                    EntityArgument.getEntity(p_341141_, "target"),
                                                                                    ResourceArgument.getAttribute(p_341141_, "attribute"),
                                                                                    ResourceLocationArgument.getId(p_341141_, "id"),
                                                                                    DoubleArgumentType.getDouble(p_341141_, "value"),
                                                                                    AttributeModifier.Operation.ADD_MULTIPLIED_BASE
                                                                                )
                                                                        )
                                                                )
                                                                .then(
                                                                    Commands.literal("add_multiplied_total")
                                                                        .executes(
                                                                            p_341139_ -> addModifier(
                                                                                    p_341139_.getSource(),
                                                                                    EntityArgument.getEntity(p_341139_, "target"),
                                                                                    ResourceArgument.getAttribute(p_341139_, "attribute"),
                                                                                    ResourceLocationArgument.getId(p_341139_, "id"),
                                                                                    DoubleArgumentType.getDouble(p_341139_, "value"),
                                                                                    AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
                                                                                )
                                                                        )
                                                                )
                                                        )
                                                )
                                        )
                                        .then(
                                            Commands.literal("remove")
                                                .then(
                                                    Commands.argument("id", ResourceLocationArgument.id())
                                                        .executes(
                                                            p_341140_ -> removeModifier(
                                                                    p_341140_.getSource(),
                                                                    EntityArgument.getEntity(p_341140_, "target"),
                                                                    ResourceArgument.getAttribute(p_341140_, "attribute"),
                                                                    ResourceLocationArgument.getId(p_341140_, "id")
                                                                )
                                                        )
                                                )
                                        )
                                        .then(
                                            Commands.literal("value")
                                                .then(
                                                    Commands.literal("get")
                                                        .then(
                                                            Commands.argument("id", ResourceLocationArgument.id())
                                                                .executes(
                                                                    p_341142_ -> getAttributeModifier(
                                                                            p_341142_.getSource(),
                                                                            EntityArgument.getEntity(p_341142_, "target"),
                                                                            ResourceArgument.getAttribute(p_341142_, "attribute"),
                                                                            ResourceLocationArgument.getId(p_341142_, "id"),
                                                                            1.0
                                                                        )
                                                                )
                                                                .then(
                                                                    Commands.argument("scale", DoubleArgumentType.doubleArg())
                                                                        .executes(
                                                                            p_341135_ -> getAttributeModifier(
                                                                                    p_341135_.getSource(),
                                                                                    EntityArgument.getEntity(p_341135_, "target"),
                                                                                    ResourceArgument.getAttribute(p_341135_, "attribute"),
                                                                                    ResourceLocationArgument.getId(p_341135_, "id"),
                                                                                    DoubleArgumentType.getDouble(p_341135_, "scale")
                                                                                )
                                                                        )
                                                                )
                                                        )
                                                )
                                        )
                                )
                        )
                )
        );
    }

    private static AttributeInstance getAttributeInstance(Entity pEntity, Holder<Attribute> pAttribute) throws CommandSyntaxException {
        AttributeInstance attributeinstance = getLivingEntity(pEntity).getAttributes().getInstance(pAttribute);
        if (attributeinstance == null) {
            throw ERROR_NO_SUCH_ATTRIBUTE.create(pEntity.getName(), getAttributeDescription(pAttribute));
        } else {
            return attributeinstance;
        }
    }

    private static LivingEntity getLivingEntity(Entity pTarget) throws CommandSyntaxException {
        if (!(pTarget instanceof LivingEntity)) {
            throw ERROR_NOT_LIVING_ENTITY.create(pTarget.getName());
        } else {
            return (LivingEntity)pTarget;
        }
    }

    private static LivingEntity getEntityWithAttribute(Entity pEntity, Holder<Attribute> pAttribute) throws CommandSyntaxException {
        LivingEntity livingentity = getLivingEntity(pEntity);
        if (!livingentity.getAttributes().hasAttribute(pAttribute)) {
            throw ERROR_NO_SUCH_ATTRIBUTE.create(pEntity.getName(), getAttributeDescription(pAttribute));
        } else {
            return livingentity;
        }
    }

    private static int getAttributeValue(CommandSourceStack pSource, Entity pEntity, Holder<Attribute> pAttribute, double pScale) throws CommandSyntaxException {
        LivingEntity livingentity = getEntityWithAttribute(pEntity, pAttribute);
        double d0 = livingentity.getAttributeValue(pAttribute);
        pSource.sendSuccess(() -> Component.translatable("commands.attribute.value.get.success", getAttributeDescription(pAttribute), pEntity.getName(), d0), false);
        return (int)(d0 * pScale);
    }

    private static int getAttributeBase(CommandSourceStack pSource, Entity pEntity, Holder<Attribute> pAttribute, double pScale) throws CommandSyntaxException {
        LivingEntity livingentity = getEntityWithAttribute(pEntity, pAttribute);
        double d0 = livingentity.getAttributeBaseValue(pAttribute);
        pSource.sendSuccess(() -> Component.translatable("commands.attribute.base_value.get.success", getAttributeDescription(pAttribute), pEntity.getName(), d0), false);
        return (int)(d0 * pScale);
    }

    private static int getAttributeModifier(CommandSourceStack pSource, Entity pEntity, Holder<Attribute> pAttribute, ResourceLocation pId, double pScale) throws CommandSyntaxException {
        LivingEntity livingentity = getEntityWithAttribute(pEntity, pAttribute);
        AttributeMap attributemap = livingentity.getAttributes();
        if (!attributemap.hasModifier(pAttribute, pId)) {
            throw ERROR_NO_SUCH_MODIFIER.create(pEntity.getName(), getAttributeDescription(pAttribute), pId);
        } else {
            double d0 = attributemap.getModifierValue(pAttribute, pId);
            pSource.sendSuccess(
                () -> Component.translatable(
                        "commands.attribute.modifier.value.get.success", Component.translationArg(pId), getAttributeDescription(pAttribute), pEntity.getName(), d0
                    ),
                false
            );
            return (int)(d0 * pScale);
        }
    }

    private static int setAttributeBase(CommandSourceStack pSource, Entity pEntity, Holder<Attribute> pAttribute, double pValue) throws CommandSyntaxException {
        getAttributeInstance(pEntity, pAttribute).setBaseValue(pValue);
        pSource.sendSuccess(() -> Component.translatable("commands.attribute.base_value.set.success", getAttributeDescription(pAttribute), pEntity.getName(), pValue), false);
        return 1;
    }

    private static int addModifier(
        CommandSourceStack pSource,
        Entity pEntity,
        Holder<Attribute> pAttribute,
        ResourceLocation pId,
        double pAmount,
        AttributeModifier.Operation pOperation
    ) throws CommandSyntaxException {
        AttributeInstance attributeinstance = getAttributeInstance(pEntity, pAttribute);
        AttributeModifier attributemodifier = new AttributeModifier(pId, pAmount, pOperation);
        if (attributeinstance.hasModifier(pId)) {
            throw ERROR_MODIFIER_ALREADY_PRESENT.create(pEntity.getName(), getAttributeDescription(pAttribute), pId);
        } else {
            attributeinstance.addPermanentModifier(attributemodifier);
            pSource.sendSuccess(
                () -> Component.translatable("commands.attribute.modifier.add.success", Component.translationArg(pId), getAttributeDescription(pAttribute), pEntity.getName()),
                false
            );
            return 1;
        }
    }

    private static int removeModifier(CommandSourceStack pSource, Entity pEntity, Holder<Attribute> pAttribute, ResourceLocation pId) throws CommandSyntaxException {
        AttributeInstance attributeinstance = getAttributeInstance(pEntity, pAttribute);
        if (attributeinstance.removeModifier(pId)) {
            pSource.sendSuccess(
                () -> Component.translatable(
                        "commands.attribute.modifier.remove.success", Component.translationArg(pId), getAttributeDescription(pAttribute), pEntity.getName()
                    ),
                false
            );
            return 1;
        } else {
            throw ERROR_NO_SUCH_MODIFIER.create(pEntity.getName(), getAttributeDescription(pAttribute), pId);
        }
    }

    private static Component getAttributeDescription(Holder<Attribute> pAttribute) {
        return Component.translatable(pAttribute.value().getDescriptionId());
    }
}