package net.minecraft.world.level.block.entity.vault;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public enum VaultState implements StringRepresentable {
    INACTIVE("inactive", VaultState.LightLevel.HALF_LIT) {
        @Override
        protected void onEnter(ServerLevel p_330824_, BlockPos p_329235_, VaultConfig p_334137_, VaultSharedData p_334678_, boolean p_331058_) {
            p_334678_.setDisplayItem(ItemStack.EMPTY);
            p_330824_.levelEvent(3016, p_329235_, p_331058_ ? 1 : 0);
        }
    },
    ACTIVE("active", VaultState.LightLevel.LIT) {
        @Override
        protected void onEnter(ServerLevel p_329909_, BlockPos p_333646_, VaultConfig p_333985_, VaultSharedData p_334965_, boolean p_328901_) {
            if (!p_334965_.hasDisplayItem()) {
                VaultBlockEntity.Server.cycleDisplayItemFromLootTable(p_329909_, this, p_333985_, p_334965_, p_333646_);
            }

            p_329909_.levelEvent(3015, p_333646_, p_328901_ ? 1 : 0);
        }
    },
    UNLOCKING("unlocking", VaultState.LightLevel.LIT) {
        @Override
        protected void onEnter(ServerLevel p_334760_, BlockPos p_334274_, VaultConfig p_331308_, VaultSharedData p_327978_, boolean p_332846_) {
            p_334760_.playSound(null, p_334274_, SoundEvents.VAULT_INSERT_ITEM, SoundSource.BLOCKS);
        }
    },
    EJECTING("ejecting", VaultState.LightLevel.LIT) {
        @Override
        protected void onEnter(ServerLevel p_331552_, BlockPos p_331920_, VaultConfig p_332952_, VaultSharedData p_328350_, boolean p_333805_) {
            p_331552_.playSound(null, p_331920_, SoundEvents.VAULT_OPEN_SHUTTER, SoundSource.BLOCKS);
        }

        @Override
        protected void onExit(ServerLevel p_329515_, BlockPos p_333072_, VaultConfig p_328058_, VaultSharedData p_332218_) {
            p_329515_.playSound(null, p_333072_, SoundEvents.VAULT_CLOSE_SHUTTER, SoundSource.BLOCKS);
        }
    };

    private static final int UPDATE_CONNECTED_PLAYERS_TICK_RATE = 20;
    private static final int DELAY_BETWEEN_EJECTIONS_TICKS = 20;
    private static final int DELAY_AFTER_LAST_EJECTION_TICKS = 20;
    private static final int DELAY_BEFORE_FIRST_EJECTION_TICKS = 20;
    private final String stateName;
    private final VaultState.LightLevel lightLevel;

    VaultState(final String pStateName, final VaultState.LightLevel pLightLevel) {
        this.stateName = pStateName;
        this.lightLevel = pLightLevel;
    }

    @Override
    public String getSerializedName() {
        return this.stateName;
    }

    public int lightLevel() {
        return this.lightLevel.value;
    }

    public VaultState tickAndGetNext(ServerLevel pLevel, BlockPos pPos, VaultConfig pConfig, VaultServerData pServerData, VaultSharedData pSharedData) {
        return switch (this) {
            case INACTIVE -> updateStateForConnectedPlayers(pLevel, pPos, pConfig, pServerData, pSharedData, pConfig.activationRange());
            case ACTIVE -> updateStateForConnectedPlayers(pLevel, pPos, pConfig, pServerData, pSharedData, pConfig.deactivationRange());
            case UNLOCKING -> {
                pServerData.pauseStateUpdatingUntil(pLevel.getGameTime() + 20L);
                yield EJECTING;
            }
            case EJECTING -> {
                if (pServerData.getItemsToEject().isEmpty()) {
                    pServerData.markEjectionFinished();
                    yield updateStateForConnectedPlayers(pLevel, pPos, pConfig, pServerData, pSharedData, pConfig.deactivationRange());
                } else {
                    float f = pServerData.ejectionProgress();
                    this.ejectResultItem(pLevel, pPos, pServerData.popNextItemToEject(), f);
                    pSharedData.setDisplayItem(pServerData.getNextItemToEject());
                    boolean flag = pServerData.getItemsToEject().isEmpty();
                    int i = flag ? 20 : 20;
                    pServerData.pauseStateUpdatingUntil(pLevel.getGameTime() + (long)i);
                    yield EJECTING;
                }
            }
        };
    }

    private static VaultState updateStateForConnectedPlayers(
        ServerLevel pLevel, BlockPos pPos, VaultConfig pConfig, VaultServerData pSeverData, VaultSharedData pSharedData, double pDeactivationRange
    ) {
        pSharedData.updateConnectedPlayersWithinRange(pLevel, pPos, pSeverData, pConfig, pDeactivationRange);
        pSeverData.pauseStateUpdatingUntil(pLevel.getGameTime() + 20L);
        return pSharedData.hasConnectedPlayers() ? ACTIVE : INACTIVE;
    }

    public void onTransition(ServerLevel pLevel, BlockPos pPos, VaultState pState, VaultConfig pConfig, VaultSharedData pSharedData, boolean pIsOminous) {
        this.onExit(pLevel, pPos, pConfig, pSharedData);
        pState.onEnter(pLevel, pPos, pConfig, pSharedData, pIsOminous);
    }

    protected void onEnter(ServerLevel pLevel, BlockPos pPos, VaultConfig pConfig, VaultSharedData pSharedData, boolean pIsOminous) {
    }

    protected void onExit(ServerLevel pLevel, BlockPos pPos, VaultConfig pConfig, VaultSharedData pSharedData) {
    }

    private void ejectResultItem(ServerLevel pLevel, BlockPos pPos, ItemStack pStack, float pEjectionProgress) {
        DefaultDispenseItemBehavior.spawnItem(pLevel, pStack, 2, Direction.UP, Vec3.atBottomCenterOf(pPos).relative(Direction.UP, 1.2));
        pLevel.levelEvent(3017, pPos, 0);
        pLevel.playSound(null, pPos, SoundEvents.VAULT_EJECT_ITEM, SoundSource.BLOCKS, 1.0F, 0.8F + 0.4F * pEjectionProgress);
    }

    static enum LightLevel {
        HALF_LIT(6),
        LIT(12);

        final int value;

        private LightLevel(final int pValue) {
            this.value = pValue;
        }
    }
}