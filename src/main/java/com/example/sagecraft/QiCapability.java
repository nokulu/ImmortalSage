package com.example.sagecraft;

import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent; 
import net.minecraftforge.eventbus.api.IEventBus; 
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import net.minecraft.world.entity.Entity;

import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages Qi capabilities for players in Sagecraft mod.
 * Provides capability registration, storage implementation, and event handling.
 * 
 * Features:
 * - Qi level tracking and modification
 * - Meditation state management
 * - Realm level progression
 * - Path selection and tracking
 * - Data persistence through NBT
 *
 * @version 1.0
 * @since 1.21.1
 */
@Mod.EventBusSubscriber(modid = SagecraftMod.MOD_ID)
public class QiCapability {
    public static final Capability<IQiStorage> CAPABILITY_QI_MANAGER = CapabilityManager.get(new CapabilityToken<>(){});
    private static final Logger LOGGER = LoggerFactory.getLogger(QiCapability.class);

    public static void register(IEventBus modEventBus) {
        // Removed RegisterCapabilitiesEvent as it is deprecated
    }

    /**
     * Attaches QiStorage capability to players when they are created
     * @param event The capability attachment event
     */
    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<Player> event) {
        if (!(event.getObject() instanceof ServerPlayer player)) {
            return;
        }
        LOGGER.debug("Attaching Qi capability to player: {}", player.getName().getString());
        QiStorageImpl storage = new QiStorageImpl();
        event.addCapability(
            ResourceLocation.fromNamespaceAndPath("sagecraft", "qi_manager"),
            storage
        );
        event.addListener(storage::invalidateCapability);
    }

    /**
     * Implementation of IQiStorage that provides Qi management functionality
     * Handles storage, serialization, and capability integration
     */
    private static class QiStorageImpl implements IQiStorage, ICapabilitySerializable<CompoundTag> {
 
        /** Lazy optional holder for capability instance */
        private final LazyOptional<IQiStorage> holder = LazyOptional.of(() -> this);
        
        /** Current Qi amount */
        private int qi = 0;
        
        /** Current meditation state */
        private boolean isMeditating = false;
        
        /** Current realm progression level */
        private int realmLevel = 0;
        
        /** Current cultivation path */
        private String currentPath = "Neutral";

        /** Meditation tick counter */
        private int meditationTicks = 0;
    
        /** Ticks per Qi gain during meditation */
        private static final int MEDITATION_TICK_RATE = 20;
    
        @Override
        public void tickCultivation() {
            if (isMeditating) {
                meditationTicks++;
                if (meditationTicks >= MEDITATION_TICK_RATE) {
                    int qiGain = calculateQiGain();
                    qi += qiGain;
                    meditationTicks = 0;
                }
            }
        }
    
        private int calculateQiGain() {
            double baseGain = Config.baseQiGain.get();
            double pathMultiplier = switch (currentPath) {
                case "Righteous" -> 1.0;
                case "Demonic" -> 5.0;
                case "Beast" -> 3.0;
                case "Neutral" -> 1.0; // Added case for Neutral
                default -> 1.0; // Default case
            };
            return (int)(baseGain * pathMultiplier * Config.meditationMultiplier.get());
        }
        
        @Override
        public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
            return CAPABILITY_QI_MANAGER.orEmpty(cap, holder);
        }

        /**
         * Serializes capability data to NBT
         * @return CompoundTag containing all capability data
         */
        @Override
        public CompoundTag serialize() {
            CompoundTag tag = new CompoundTag();
            tag.putInt("qi", qi);
            tag.putBoolean("meditating", isMeditating);
            tag.putInt("realmLevel", realmLevel);
            tag.putString("path", currentPath);
            return tag;
        }

        /**
         * Deserializes capability data from NBT
         * @param tag CompoundTag containing capability data
         */
        @Override
        public void deserialize(CompoundTag tag) {
            qi = tag.getInt("qi");
            isMeditating = tag.getBoolean("meditating");
            realmLevel = tag.getInt("realmLevel");
            currentPath = tag.getString("path");
        }

        /**
         * Serializes capability data with provider
         * @param provider The HolderLookup.Provider instance
         * @return CompoundTag containing capability data
         */
        @Override
        public CompoundTag serializeNBT(HolderLookup.Provider provider) {
            return serialize();
        }

        /**
         * Deserializes capability data with provider
         * @param provider The HolderLookup.Provider instance
         * @param tag CompoundTag containing capability data
         */
        @Override
        public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
            deserialize(tag);
        }

        /**
         * Increases player's Qi amount
         * @param amount Amount of Qi to add
         */
        @Override
        public void gainQi(int amount) {
            this.qi += amount;
        }

        // IQiStorage implementation
        @Override
        public int getQiAmount() { return qi; }
        
        @Override
        public void setQiAmount(int amount) { this.qi = amount; }
        
        @Override
        public boolean isMeditating() { return isMeditating; }
        
        @Override
        public void setMeditating(boolean meditating) { this.isMeditating = meditating; }
        
        @Override
        public int getRealmLevel() { return realmLevel; }
        
        @Override
        public void setRealmLevel(int level) { this.realmLevel = level; }
        
        @Override
        public String getCurrentPath() { return currentPath; }
        
        @Override
        public void setCurrentPath(String path) { this.currentPath = path; }

        /**
         * Invalidates the capability holder
         */
        public void invalidateCapability() {
            holder.invalidate();
        }
    }
}
