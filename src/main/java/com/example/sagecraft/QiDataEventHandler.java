package com.example.sagecraft;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.Level;
import com.example.sagecraft.network.PathUpdatePacket;
import net.minecraft.server.level.ServerPlayer;
import com.example.sagecraft.QiDataChangeEvent; // Import statement added

@Mod.EventBusSubscriber(modid = SagecraftMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class QiDataEventHandler {

    @SubscribeEvent
    public static void onQiDataChange(QiDataChangeEvent event) {
        try {
            // Log the event for debugging
            SagecraftMod.LOGGER.info("Processing QiDataChangeEvent: {}", event);
            
            // Handle different types of Qi data changes
            switch (event.getChangeType()) {
                case QI_AMOUNT:
                    handleQiAmountChange(event);
                    break;
                case PATH:
                    handlePathChange(event);
                    break;
                case REALM_LEVEL:
                    handleRealmLevelChange(event);
                    break;
                default:
                    SagecraftMod.LOGGER.warn("Unknown QiDataChangeEvent type: {}", event.getChangeType());
                    break;
            }
        } catch (Exception e) {
            SagecraftMod.LOGGER.error("Error handling QiDataChangeEvent", e);
        }
    }

    private static void handleQiAmountChange(QiDataChangeEvent event) {
        int oldQi = (Integer) event.getOldValue();
        int newQi = (Integer) event.getNewValue();
        
        // Update HUD display
        HudOverlay.updateQiDisplay(newQi);
        
        // Send update to client if needed
        PacketHandler.sendToPlayer(new PathUpdatePacket(QiDataChangeEvent.ChangeType.QI_AMOUNT, newQi), (ServerPlayer) event.getEntity());
        
        SagecraftMod.LOGGER.info("Qi amount changed from {} to {} for player {}", 
            oldQi, newQi, event.getEntity().getName().getString());
    }

    private static void handlePathChange(QiDataChangeEvent event) {
        String oldPath = (String) event.getOldValue();
        String newPath = (String) event.getNewValue();
        
        // Update player attributes
        PlayerPathManager playerPathManager = new PlayerPathManager(event.getEntity());
        playerPathManager.setPath(newPath);
        
        // Send update to client if needed
        PacketHandler.sendToPlayer(new PathUpdatePacket(QiDataChangeEvent.ChangeType.PATH, newPath), (ServerPlayer) event.getEntity());
        
        SagecraftMod.LOGGER.info("Path changed from {} to {} for player {}", 
            oldPath, newPath, event.getEntity().getName().getString());
    }

    private static void handleRealmLevelChange(QiDataChangeEvent event) {
        int oldLevel = (Integer) event.getOldValue();
        int newLevel = (Integer) event.getNewValue();
        
        // Update realm display
        RealmDisplayManager.updateRealmDisplay(newLevel);
        
        // Send update to client if needed
        PacketHandler.sendToPlayer(new PathUpdatePacket(QiDataChangeEvent.ChangeType.REALM_LEVEL, newLevel), (ServerPlayer) event.getEntity());
        
        SagecraftMod.LOGGER.info("Realm level changed from {} to {} for player {}", 
            oldLevel, newLevel, event.getEntity().getName().getString());
    }
}
