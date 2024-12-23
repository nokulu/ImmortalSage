package net.minecraft.world.item;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;

public class FishingRodItem extends Item {
    public FishingRodItem(Item.Properties pProperties) {
        super(pProperties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pHand) {
        ItemStack itemstack = pPlayer.getItemInHand(pHand);
        if (pPlayer.fishing != null) {
            if (!pLevel.isClientSide) {
                int i = pPlayer.fishing.retrieve(itemstack);
                ItemStack original = itemstack.copy();
                itemstack.hurtAndBreak(i, pPlayer, LivingEntity.getSlotForHand(pHand));
                if (itemstack.isEmpty()) {
                    net.minecraftforge.event.ForgeEventFactory.onPlayerDestroyItem(pPlayer, original, LivingEntity.getSlotForHand(pHand));
                }
            }

            pLevel.playSound(
                null,
                pPlayer.getX(),
                pPlayer.getY(),
                pPlayer.getZ(),
                SoundEvents.FISHING_BOBBER_RETRIEVE,
                SoundSource.NEUTRAL,
                1.0F,
                0.4F / (pLevel.getRandom().nextFloat() * 0.4F + 0.8F)
            );
            pPlayer.gameEvent(GameEvent.ITEM_INTERACT_FINISH);
        } else {
            pLevel.playSound(
                null,
                pPlayer.getX(),
                pPlayer.getY(),
                pPlayer.getZ(),
                SoundEvents.FISHING_BOBBER_THROW,
                SoundSource.NEUTRAL,
                0.5F,
                0.4F / (pLevel.getRandom().nextFloat() * 0.4F + 0.8F)
            );
            if (pLevel instanceof ServerLevel serverlevel) {
                int j = (int)(EnchantmentHelper.getFishingTimeReduction(serverlevel, itemstack, pPlayer) * 20.0F);
                int k = EnchantmentHelper.getFishingLuckBonus(serverlevel, itemstack, pPlayer);
                pLevel.addFreshEntity(new FishingHook(pPlayer, pLevel, k, j));
            }

            pPlayer.awardStat(Stats.ITEM_USED.get(this));
            pPlayer.gameEvent(GameEvent.ITEM_INTERACT_START);
        }

        return InteractionResultHolder.sidedSuccess(itemstack, pLevel.isClientSide());
    }

    @Override
    public int getEnchantmentValue() {
        return 1;
    }

    @Override
    public boolean canPerformAction(ItemStack stack, net.minecraftforge.common.ToolAction toolAction) {
        return net.minecraftforge.common.ToolActions.DEFAULT_FISHING_ROD_ACTIONS.contains(toolAction);
    }
}
