package net.minecraft.world.level.storage.loot.entries;

import java.util.function.Consumer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;

/**
 * A loot pool entry generates zero or more stacks of items based on the LootContext.
 * Each loot pool entry has a weight that determines how likely it is to be generated within a given loot pool.
 */
public interface LootPoolEntry {
    int getWeight(float pLuck);

    void createItemStack(Consumer<ItemStack> pStackConsumer, LootContext pLootContext);
}