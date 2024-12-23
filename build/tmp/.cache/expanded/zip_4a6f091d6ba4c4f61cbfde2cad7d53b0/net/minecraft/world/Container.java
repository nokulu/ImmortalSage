package net.minecraft.world;

import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public interface Container extends Clearable {
    float DEFAULT_DISTANCE_BUFFER = 4.0F;

    int getContainerSize();

    boolean isEmpty();

    ItemStack getItem(int pSlot);

    ItemStack removeItem(int pSlot, int pAmount);

    ItemStack removeItemNoUpdate(int pSlot);

    void setItem(int pSlot, ItemStack pStack);

    default int getMaxStackSize() {
        return 99;
    }

    default int getMaxStackSize(ItemStack pStack) {
        return Math.min(this.getMaxStackSize(), pStack.getMaxStackSize());
    }

    void setChanged();

    boolean stillValid(Player pPlayer);

    default void startOpen(Player pPlayer) {
    }

    default void stopOpen(Player pPlayer) {
    }

    default boolean canPlaceItem(int pSlot, ItemStack pStack) {
        return true;
    }

    default boolean canTakeItem(Container pTarget, int pSlot, ItemStack pStack) {
        return true;
    }

    default int countItem(Item pItem) {
        int i = 0;

        for (int j = 0; j < this.getContainerSize(); j++) {
            ItemStack itemstack = this.getItem(j);
            if (itemstack.getItem().equals(pItem)) {
                i += itemstack.getCount();
            }
        }

        return i;
    }

    default boolean hasAnyOf(Set<Item> pSet) {
        return this.hasAnyMatching(p_216873_ -> !p_216873_.isEmpty() && pSet.contains(p_216873_.getItem()));
    }

    default boolean hasAnyMatching(Predicate<ItemStack> pPredicate) {
        for (int i = 0; i < this.getContainerSize(); i++) {
            ItemStack itemstack = this.getItem(i);
            if (pPredicate.test(itemstack)) {
                return true;
            }
        }

        return false;
    }

    static boolean stillValidBlockEntity(BlockEntity pBlockEntity, Player pPlayer) {
        return stillValidBlockEntity(pBlockEntity, pPlayer, 4.0F);
    }

    static boolean stillValidBlockEntity(BlockEntity pBlockEntity, Player pPlayer, float pDistance) {
        Level level = pBlockEntity.getLevel();
        BlockPos blockpos = pBlockEntity.getBlockPos();
        if (level == null) {
            return false;
        } else {
            return level.getBlockEntity(blockpos) != pBlockEntity ? false : pPlayer.canInteractWithBlock(blockpos, (double)pDistance);
        }
    }
}