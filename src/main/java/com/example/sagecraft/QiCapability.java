package com.example.sagecraft;

import com.example.sagecraft.QiManager;
import com.example.sagecraft.RealmDisplayManager;
import com.example.sagecraft.QiData;

import net.minecraft.core.*;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Documentation:
 * This class manages the Qi capabilities for players in the Sagecraft mod.
 * It allows players to have a QiManager instance that tracks their Qi levels
 * and provides methods for serialization and deserialization of Qi data.
 * 
 * Key functionalities:
 * - Registers the QiManager capability for players.
 * - Attaches the QiManager capability to players when they are created.
 * - Handles the serialization and deserialization of QiManager data.
 * - Updates Qi levels for players who are meditating during server ticks.
 * 
 * Events:
 * - AttachCapabilitiesEvent: Attaches the QiManager capability to players.
 * - FMLCommonSetupEvent: Registers the QiManager capability with the capability manager.
 * - ServerTickEvent: Updates Qi levels for players who are meditating.
 * 
 * References:
 * - Capability System: https://forge.gemwire.uk/wiki/Capabilities
 * - How to fix capability from 1.20.x -> 1.21.1: https://forums.minecraftforge.net/topic/149807-forge-121-5101-configjava-uses-resourcelocation-but-it-is-set-to-private/
 * - idk: https://forge.gemwire.uk/wiki/Capabilities
 */
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class QiCapability {
    public static final Capability<QiManager> CAPABILITY_QI_MANAGER = CapabilityManager.get(new CapabilityToken<QiManager>() {});

    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<Player> event) {
        if (event.getObject() instanceof Player) {
            event.addCapability(ResourceLocation.fromNamespaceAndPath("sagecraft", "qi_manager"), new QiStorage());
        }
    }

    public static class QiStorage implements ICapabilityProvider, INBTSerializable<CompoundTag> {
        private final QiManager instance = new QiManager();
        private final LazyOptional<QiManager> lazyOptional = LazyOptional.of(() -> instance);

        @NotNull
        @Override
        public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
            return CAPABILITY_QI_MANAGER.orEmpty(cap, lazyOptional);
        }

        @Override
        public CompoundTag serializeNBT() {
            CompoundTag compound = new CompoundTag();
            compound.putInt("QiAmount", instance.getQiAmount());
            compound.putString("CurrentPath", instance.getCurrentPath());
            compound.putInt("RealmLevel", instance.getRealmLevel());
            return compound;
        }

        @Override
        public void deserializeNBT(CompoundTag nbt) {
            if (nbt.contains("QiAmount")) {
                instance.setQiAmount(nbt.getInt("QiAmount"));
            }
            if (nbt.contains("CurrentPath")) {
                instance.setCurrentPath(nbt.getString("CurrentPath"));
            }
            if (nbt.contains("RealmLevel")) {
                instance.setRealmLevel(nbt.getInt("RealmLevel"));
            }
        }
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        
        for (Player player : event.getServer().getPlayerList().getPlayers()) {
            QiManager qiManager = player.getCapability(CAPABILITY_QI_MANAGER).orElse(null);
            if (qiManager != null && qiManager.isMeditating()) {
                qiManager.gainQi(1); // Gain 1 Qi every tick while meditating
            }
        }
    }
}
