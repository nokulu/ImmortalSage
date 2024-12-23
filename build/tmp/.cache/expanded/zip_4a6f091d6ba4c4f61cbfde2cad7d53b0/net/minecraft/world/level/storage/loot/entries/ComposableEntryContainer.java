package net.minecraft.world.level.storage.loot.entries;

import java.util.Objects;
import java.util.function.Consumer;
import net.minecraft.world.level.storage.loot.LootContext;

/**
 * Base interface for loot pool entry containers.
 * A loot pool entry container holds one or more loot pools and will expand into those.
 * Additionally, the container can either succeed or fail, based on its conditions.
 */
@FunctionalInterface
interface ComposableEntryContainer {
    ComposableEntryContainer ALWAYS_FALSE = (p_79418_, p_79419_) -> false;
    ComposableEntryContainer ALWAYS_TRUE = (p_79409_, p_79410_) -> true;

    boolean expand(LootContext pLootContext, Consumer<LootPoolEntry> pEntryConsumer);

    default ComposableEntryContainer and(ComposableEntryContainer pEntry) {
        Objects.requireNonNull(pEntry);
        return (p_79424_, p_79425_) -> this.expand(p_79424_, p_79425_) && pEntry.expand(p_79424_, p_79425_);
    }

    default ComposableEntryContainer or(ComposableEntryContainer pEntry) {
        Objects.requireNonNull(pEntry);
        return (p_79415_, p_79416_) -> this.expand(p_79415_, p_79416_) || pEntry.expand(p_79415_, p_79416_);
    }
}