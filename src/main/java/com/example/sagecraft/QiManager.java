package com.example.sagecraft;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.OutgoingChatMessage;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class QiManager {
    private int qi;
    private int currentRealmIndex = 0; // Start at Mortal
    private static final String[] REALMS = {
        "Mortal", "Qi Sensing", "Qi Refining", "Body Strengthening", 
        "Foundation Building", "Golden Core", "Nascent Soul", 
        "Void Severing", "Immortal", "Immortal Sage"
    };
    
    private static final int[] BREAKTHROUGH_REQUIREMENTS = {
        1000, 4000, 16000, 64000, 256000, 1024000, 4096000, 16384000, 65536000
    };
    
    private boolean isMeditating; // Track meditation

    public QiManager() {
        this.qi = 0; // Initialize Qi to 0
        this.isMeditating = false; // Initialize meditation state
    }

    public void gainQi(int amount) {
        this.qi += amount;
    }

    public void onKeyPress(Player player) {
        gainQi(1); // Gain 1 Qi immediately on key press
        meditate(player); // Start meditation if key is held down
    }

    public void onKeyRelease() {
        stopMeditation(); // Stop gaining Qi when key is released
    }

    public void meditate(Player player) {
        isMeditating = true; // Set meditation state
        // Additional logic for meditation can be added here
    }

    public void stopMeditation() {
        isMeditating = false; // Reset meditation state
    }

    public boolean isMeditating() {
        return isMeditating; // Return current meditation state
    }

    public int getQi() {
        return qi;
    }

    public boolean canBreakthrough() {
        return currentRealmIndex < BREAKTHROUGH_REQUIREMENTS.length && qi >= BREAKTHROUGH_REQUIREMENTS[currentRealmIndex];
    }

    public void breakthrough(Player player) {
        if (canBreakthrough()) {
            // Increase player stats
            player.setHealth(player.getHealth() + 10); // Gain 10 HP
            // Trigger lightning strikes
            int lightningStrikes = (int) Math.pow(4, currentRealmIndex); // Quadruples each time
            for (int i = 0; i < lightningStrikes; i++) {
                player.createCommandSourceStack().sendChatMessage(new OutgoingChatMessage.Player(PlayerChatMessage.unsigned(player.getUUID(), "You are struck by lightning!")), false, ChatType.bind(ChatType.CHAT, player));
            }

            // Move to the next realm and update player name tag
            RealmDisplayManager realmDisplayManager = new RealmDisplayManager();
            realmDisplayManager.updatePlayerNameTag(player, REALMS[currentRealmIndex], "Current Path");
            currentRealmIndex++;
            player.createCommandSourceStack().sendChatMessage(new OutgoingChatMessage.Player(PlayerChatMessage.unsigned(player.getUUID(), "You have broken through to the realm of " + REALMS[currentRealmIndex - 1] + "!")), false, ChatType.bind(ChatType.CHAT, player));
        } else {
            player.createCommandSourceStack().sendChatMessage(new OutgoingChatMessage.Player(PlayerChatMessage.unsigned(player.getUUID(), "You do not have enough Qi to breakthrough.")), false, ChatType.bind(ChatType.CHAT, player));
        }
    }

    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        nbt.putInt("qi", this.qi);
        return nbt;
    }

    public void deserializeNBT(CompoundTag nbt) {
        this.qi = nbt.getInt("qi");
    }

    @SubscribeEvent
    public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = Minecraft.getInstance().player; // Retrieve the player instance from the event
        if (player != null) {
            player.createCommandSourceStack().sendChatMessage(new OutgoingChatMessage.Player(PlayerChatMessage.unsigned(player.getUUID(), "You have " + qi + " Qi.")), false, ChatType.bind(ChatType.CHAT, player));
        }
    }
}
</create_file>
