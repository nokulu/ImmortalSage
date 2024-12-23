package net.minecraft.network.protocol.game;

import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.protocol.common.ServerCommonPacketListener;
import net.minecraft.network.protocol.ping.ServerPingPacketListener;

/**
 * PacketListener for the server side of the PLAY protocol.
 */
public interface ServerGamePacketListener extends ServerPingPacketListener, ServerCommonPacketListener {
    @Override
    default ConnectionProtocol protocol() {
        return ConnectionProtocol.PLAY;
    }

    void handleAnimate(ServerboundSwingPacket pPacket);

    void handleChat(ServerboundChatPacket pPacket);

    void handleChatCommand(ServerboundChatCommandPacket pPacket);

    void handleSignedChatCommand(ServerboundChatCommandSignedPacket pPacket);

    void handleChatAck(ServerboundChatAckPacket pPacket);

    void handleClientCommand(ServerboundClientCommandPacket pPacket);

    void handleContainerButtonClick(ServerboundContainerButtonClickPacket pPacket);

    void handleContainerClick(ServerboundContainerClickPacket pPacket);

    void handlePlaceRecipe(ServerboundPlaceRecipePacket pPacket);

    void handleContainerClose(ServerboundContainerClosePacket pPacket);

    void handleInteract(ServerboundInteractPacket pPacket);

    void handleMovePlayer(ServerboundMovePlayerPacket pPacket);

    void handlePlayerAbilities(ServerboundPlayerAbilitiesPacket pPacket);

    void handlePlayerAction(ServerboundPlayerActionPacket pPacket);

    void handlePlayerCommand(ServerboundPlayerCommandPacket pPacket);

    void handlePlayerInput(ServerboundPlayerInputPacket pPacket);

    void handleSetCarriedItem(ServerboundSetCarriedItemPacket pPacket);

    void handleSetCreativeModeSlot(ServerboundSetCreativeModeSlotPacket pPacket);

    void handleSignUpdate(ServerboundSignUpdatePacket pPacket);

    void handleUseItemOn(ServerboundUseItemOnPacket pPacket);

    void handleUseItem(ServerboundUseItemPacket pPacket);

    void handleTeleportToEntityPacket(ServerboundTeleportToEntityPacket pPacket);

    void handlePaddleBoat(ServerboundPaddleBoatPacket pPacket);

    void handleMoveVehicle(ServerboundMoveVehiclePacket pPacket);

    void handleAcceptTeleportPacket(ServerboundAcceptTeleportationPacket pPacket);

    void handleRecipeBookSeenRecipePacket(ServerboundRecipeBookSeenRecipePacket pPacket);

    void handleRecipeBookChangeSettingsPacket(ServerboundRecipeBookChangeSettingsPacket pPacket);

    void handleSeenAdvancements(ServerboundSeenAdvancementsPacket pPacket);

    void handleCustomCommandSuggestions(ServerboundCommandSuggestionPacket pPacket);

    void handleSetCommandBlock(ServerboundSetCommandBlockPacket pPacket);

    void handleSetCommandMinecart(ServerboundSetCommandMinecartPacket pPacket);

    void handlePickItem(ServerboundPickItemPacket pPacket);

    void handleRenameItem(ServerboundRenameItemPacket pPacket);

    void handleSetBeaconPacket(ServerboundSetBeaconPacket pPacket);

    void handleSetStructureBlock(ServerboundSetStructureBlockPacket pPacket);

    void handleSelectTrade(ServerboundSelectTradePacket pPacket);

    void handleEditBook(ServerboundEditBookPacket pPacket);

    void handleEntityTagQuery(ServerboundEntityTagQueryPacket pPacket);

    void handleContainerSlotStateChanged(ServerboundContainerSlotStateChangedPacket pPacket);

    void handleBlockEntityTagQuery(ServerboundBlockEntityTagQueryPacket pPacket);

    void handleSetJigsawBlock(ServerboundSetJigsawBlockPacket pPacket);

    void handleJigsawGenerate(ServerboundJigsawGeneratePacket pPacket);

    void handleChangeDifficulty(ServerboundChangeDifficultyPacket pPacket);

    void handleLockDifficulty(ServerboundLockDifficultyPacket pPacket);

    void handleChatSessionUpdate(ServerboundChatSessionUpdatePacket pPacket);

    void handleConfigurationAcknowledged(ServerboundConfigurationAcknowledgedPacket pPacket);

    void handleChunkBatchReceived(ServerboundChunkBatchReceivedPacket pPacket);

    void handleDebugSampleSubscription(ServerboundDebugSampleSubscriptionPacket pPacket);
}