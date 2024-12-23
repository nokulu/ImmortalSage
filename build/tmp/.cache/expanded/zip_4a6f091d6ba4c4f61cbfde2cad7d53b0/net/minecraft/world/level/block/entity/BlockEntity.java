package net.minecraft.world.level.block.entity;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.CrashReportCategory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;

public abstract class BlockEntity extends net.minecraftforge.common.capabilities.CapabilityProvider<BlockEntity> implements net.minecraftforge.common.extensions.IForgeBlockEntity {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final BlockEntityType<?> type;
    @Nullable
    protected Level level;
    protected final BlockPos worldPosition;
    protected boolean remove;
    private BlockState blockState;
    private DataComponentMap components = DataComponentMap.EMPTY;

    public BlockEntity(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(BlockEntity.class);
        this.type = pType;
        this.worldPosition = pPos.immutable();
        this.validateBlockState(pBlockState);
        this.blockState = pBlockState;
        this.gatherCapabilities();
    }

    private void validateBlockState(BlockState p_345558_) {
        if (!this.isValidBlockState(p_345558_)) {
            throw new IllegalStateException("Invalid block entity " + this.getNameForReporting() + " state at " + this.worldPosition + ", got " + p_345558_);
        }
    }

    public boolean isValidBlockState(BlockState p_345570_) {
        return this.getType().isValid(p_345570_);
    }

    public static BlockPos getPosFromTag(CompoundTag pTag) {
        return new BlockPos(pTag.getInt("x"), pTag.getInt("y"), pTag.getInt("z"));
    }

    @Nullable
    public Level getLevel() {
        return this.level;
    }

    public void setLevel(Level pLevel) {
        this.level = pLevel;
    }

    public boolean hasLevel() {
        return this.level != null;
    }

    protected void loadAdditional(CompoundTag pTag, HolderLookup.Provider pRegistries) {
        if (getCapabilities() != null && pTag.contains("ForgeCaps")) deserializeCaps(pRegistries, pTag.getCompound("ForgeCaps"));
    }

    public final void loadWithComponents(CompoundTag pTag, HolderLookup.Provider pRegistries) {
        this.loadAdditional(pTag, pRegistries);
        BlockEntity.ComponentHelper.COMPONENTS_CODEC
            .parse(pRegistries.createSerializationContext(NbtOps.INSTANCE), pTag)
            .resultOrPartial(p_327293_ -> LOGGER.warn("Failed to load components: {}", p_327293_))
            .ifPresent(p_327298_ -> this.components = p_327298_);
    }

    public final void loadCustomOnly(CompoundTag pTag, HolderLookup.Provider pRegistries) {
        this.loadAdditional(pTag, pRegistries);
    }

    protected void saveAdditional(CompoundTag pTag, HolderLookup.Provider pRegistries) {
        if (getCapabilities() != null) pTag.put("ForgeCaps", serializeCaps(pRegistries));
    }

    public final CompoundTag saveWithFullMetadata(HolderLookup.Provider pRegistries) {
        CompoundTag compoundtag = this.saveWithoutMetadata(pRegistries);
        this.saveMetadata(compoundtag);
        return compoundtag;
    }

    public final CompoundTag saveWithId(HolderLookup.Provider pRegistries) {
        CompoundTag compoundtag = this.saveWithoutMetadata(pRegistries);
        this.saveId(compoundtag);
        return compoundtag;
    }

    public final CompoundTag saveWithoutMetadata(HolderLookup.Provider pRegistries) {
        CompoundTag compoundtag = new CompoundTag();
        this.saveAdditional(compoundtag, pRegistries);
        BlockEntity.ComponentHelper.COMPONENTS_CODEC
            .encodeStart(pRegistries.createSerializationContext(NbtOps.INSTANCE), this.components)
            .resultOrPartial(p_327292_ -> LOGGER.warn("Failed to save components: {}", p_327292_))
            .ifPresent(p_327300_ -> compoundtag.merge((CompoundTag)p_327300_));
        return compoundtag;
    }

    public final CompoundTag saveCustomOnly(HolderLookup.Provider pRegistries) {
        CompoundTag compoundtag = new CompoundTag();
        this.saveAdditional(compoundtag, pRegistries);
        return compoundtag;
    }

    public final CompoundTag saveCustomAndMetadata(HolderLookup.Provider pRegistries) {
        CompoundTag compoundtag = this.saveCustomOnly(pRegistries);
        this.saveMetadata(compoundtag);
        return compoundtag;
    }

    private void saveId(CompoundTag pTag) {
        ResourceLocation resourcelocation = BlockEntityType.getKey(this.getType());
        if (resourcelocation == null) {
            throw new RuntimeException(this.getClass() + " is missing a mapping! This is a bug!");
        } else {
            pTag.putString("id", resourcelocation.toString());
        }
    }

    public static void addEntityType(CompoundTag pTag, BlockEntityType<?> pEntityType) {
        pTag.putString("id", BlockEntityType.getKey(pEntityType).toString());
    }

    public void saveToItem(ItemStack pStack, HolderLookup.Provider pRegistries) {
        CompoundTag compoundtag = this.saveCustomOnly(pRegistries);
        this.removeComponentsFromTag(compoundtag);
        BlockItem.setBlockEntityData(pStack, this.getType(), compoundtag);
        pStack.applyComponents(this.collectComponents());
    }

    private void saveMetadata(CompoundTag pTag) {
        this.saveId(pTag);
        pTag.putInt("x", this.worldPosition.getX());
        pTag.putInt("y", this.worldPosition.getY());
        pTag.putInt("z", this.worldPosition.getZ());
    }

    @Nullable
    public static BlockEntity loadStatic(BlockPos pPos, BlockState pState, CompoundTag pTag, HolderLookup.Provider pRegistries) {
        String s = pTag.getString("id");
        ResourceLocation resourcelocation = ResourceLocation.tryParse(s);
        if (resourcelocation == null) {
            LOGGER.error("Block entity has invalid type: {}", s);
            return null;
        } else {
            return BuiltInRegistries.BLOCK_ENTITY_TYPE.getOptional(resourcelocation).map(p_155240_ -> {
                try {
                    return p_155240_.create(pPos, pState);
                } catch (Throwable throwable) {
                    LOGGER.error("Failed to create block entity {}", s, throwable);
                    return null;
                }
            }).map(p_327297_ -> {
                try {
                    p_327297_.loadWithComponents(pTag, pRegistries);
                    return (BlockEntity)p_327297_;
                } catch (Throwable throwable) {
                    LOGGER.error("Failed to load data for block entity {}", s, throwable);
                    return null;
                }
            }).orElseGet(() -> {
                LOGGER.warn("Skipping BlockEntity with id {}", s);
                return null;
            });
        }
    }

    public void setChanged() {
        if (this.level != null) {
            setChanged(this.level, this.worldPosition, this.blockState);
        }
    }

    protected static void setChanged(Level pLevel, BlockPos pPos, BlockState pState) {
        pLevel.blockEntityChanged(pPos);
        if (!pState.isAir()) {
            pLevel.updateNeighbourForOutputSignal(pPos, pState.getBlock());
        }
    }

    public BlockPos getBlockPos() {
        return this.worldPosition;
    }

    public BlockState getBlockState() {
        return this.blockState;
    }

    @Nullable
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return null;
    }

    public CompoundTag getUpdateTag(HolderLookup.Provider pRegistries) {
        return new CompoundTag();
    }

    public boolean isRemoved() {
        return this.remove;
    }

    public void setRemoved() {
        this.remove = true;
        this.invalidateCaps();
        requestModelDataUpdate();
    }

    @Override
    public void onChunkUnloaded() {
        this.invalidateCaps();
    }

    public void clearRemoved() {
        this.remove = false;
    }

    public boolean triggerEvent(int pId, int pType) {
        return false;
    }

    public void fillCrashReportCategory(CrashReportCategory pReportCategory) {
        pReportCategory.setDetail("Name", this::getNameForReporting);
        if (this.level != null) {
            CrashReportCategory.populateBlockDetails(pReportCategory, this.level, this.worldPosition, this.getBlockState());
            CrashReportCategory.populateBlockDetails(pReportCategory, this.level, this.worldPosition, this.level.getBlockState(this.worldPosition));
        }
    }

    private String getNameForReporting() {
        return BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(this.getType()) + " // " + this.getClass().getCanonicalName();
    }

    public boolean onlyOpCanSetNbt() {
        return false;
    }

    public BlockEntityType<?> getType() {
        return this.type;
    }

    @Deprecated
    public void setBlockState(BlockState pBlockState) {
        this.validateBlockState(pBlockState);
        this.blockState = pBlockState;
    }

    protected void applyImplicitComponents(BlockEntity.DataComponentInput pComponentInput) {
    }

    public final void applyComponentsFromItemStack(ItemStack pStack) {
        this.applyComponents(pStack.getPrototype(), pStack.getComponentsPatch());
    }

    public final void applyComponents(DataComponentMap pComponents, DataComponentPatch pPatch) {
        final Set<DataComponentType<?>> set = new HashSet<>();
        set.add(DataComponents.BLOCK_ENTITY_DATA);
        final DataComponentMap datacomponentmap = PatchedDataComponentMap.fromPatch(pComponents, pPatch);
        this.applyImplicitComponents(new BlockEntity.DataComponentInput() {
            @Nullable
            @Override
            public <T> T get(DataComponentType<T> p_335233_) {
                set.add(p_335233_);
                return datacomponentmap.get(p_335233_);
            }

            @Override
            public <T> T getOrDefault(DataComponentType<? extends T> p_334887_, T p_333244_) {
                set.add(p_334887_);
                return datacomponentmap.getOrDefault(p_334887_, p_333244_);
            }
        });
        DataComponentPatch datacomponentpatch = pPatch.forget(set::contains);
        this.components = datacomponentpatch.split().added();
    }

    protected void collectImplicitComponents(DataComponentMap.Builder pComponents) {
    }

    @Deprecated
    public void removeComponentsFromTag(CompoundTag pTag) {
    }

    public final DataComponentMap collectComponents() {
        DataComponentMap.Builder datacomponentmap$builder = DataComponentMap.builder();
        datacomponentmap$builder.addAll(this.components);
        this.collectImplicitComponents(datacomponentmap$builder);
        return datacomponentmap$builder.build();
    }

    public DataComponentMap components() {
        return this.components;
    }

    public void setComponents(DataComponentMap pComponents) {
        this.components = pComponents;
    }

    @Nullable
    public static Component parseCustomNameSafe(String pCustomName, HolderLookup.Provider pRegistries) {
        try {
            return Component.Serializer.fromJson(pCustomName, pRegistries);
        } catch (Exception exception) {
            LOGGER.warn("Failed to parse custom name from string '{}', discarding", pCustomName, exception);
            return null;
        }
    }

    static class ComponentHelper {
        public static final Codec<DataComponentMap> COMPONENTS_CODEC = DataComponentMap.CODEC.optionalFieldOf("components", DataComponentMap.EMPTY).codec();

        private ComponentHelper() {
        }
    }

    protected interface DataComponentInput {
        @Nullable
        <T> T get(DataComponentType<T> pComponent);

        <T> T getOrDefault(DataComponentType<? extends T> pComponent, T pDefaultValue);
    }
}
