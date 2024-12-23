package com.example.sagecraft;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.TickEvent; // Import for TickEvent
import net.minecraft.core.Direction;

@Mod.EventBusSubscriber
public class QiCapability {
    public static final Capability<QiManager> CAPABILITY_QI_MANAGER = CapabilityManager.get(new CapabilityToken<>() {});

    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<Player> event) {
        if (event.getObject() instanceof Player) {
            event.addCapability(new ResourceLocation("sagecraft", "qi_manager"), new QiStorage());
        }
    }

    @SubscribeEvent
    public static void setup(FMLCommonSetupEvent event) {
        CapabilityManager.INSTANCE.register(QiManager.class, new QiStorage(), QiManager::new);
    }

    public static class QiStorage implements ICapabilitySerializable<CompoundTag> {
        private QiManager instance = new QiManager();

        @Override
        public CompoundTag serializeNBT() {
            return instance.serializeNBT();
        }

        @Override
        public void deserializeNBT(CompoundTag nbt) {
            instance.deserializeNBT(nbt);
        }

        @Override
        public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
            return (cap == CAPABILITY_QI_MANAGER) ? LazyOptional.of(() -> instance) : LazyOptional.empty();
        }
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        for (Player player : event.getServer().getPlayerList().getPlayers()) {
            QiManager qiManager = player.getCapability(CAPABILITY_QI_MANAGER).orElse(null);
            if (qiManager != null && qiManager.isMeditating()) {
                qiManager.gainQi(1); // Gain 1 Qi every tick while meditating
            }
        }
    }
}
