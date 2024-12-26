package com.example.sagecraft;

import java.util.Timer;
import java.util.TimerTask; // Import for Component

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.OutgoingChatMessage; // Import for PlayerChatMessage
import net.minecraft.network.chat.PlayerChatMessage; // Import for ChatType
import net.minecraft.world.entity.player.Player; // Existing import
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class QiManager {
    private int qi; // Moved from PlayerPathManager
    private int realmLevel; // New variable for realm level
    private int currentRealmIndex = 0; // Start at Mortal
    private static final String[] REALMS = {
        "Mortal", "Qi Sensing", "Qi Refining", "Body Strengthening", 
        "Foundation Building", "Golden Core", "Nascent Soul", 
        "Void Severing", "Immortal", "Immortal Sage"
    };
    
    private static final int[] BREAKTHROUGH_REQUIREMENTS = {
        1000, 4000, 16000, 64000, 256000, 1024000, 4096000, 16384000, 65536000
    };
    
    private Timer meditationTimer;
    private boolean isMeditating; // Track meditation state
    private String currentPath; // Variable to store the current path

    public QiManager() {
        this.qi = 0; // Initialize Qi to 0
        this.realmLevel = 0; // Initialize realm level to 0
    }

    public void gainQi(int amount) {
        this.qi += amount;
    }

    public int getRealmLevel() {
        return realmLevel;
    }

    public void setRealmLevel(int realmLevel) {
        this.realmLevel = realmLevel;
        // Update QiData with the new realm level
        QiData qiData = new QiData(); // Assuming you have a way to get the current QiData instance
        qiData.setRealmLevel(realmLevel);
    }

    public void onKeyPress(Player player) {
        gainQi(1); // Gain 1 Qi immediately on key press
        meditate(player); // Start meditation if key is held down
    }

    public void onKeyRelease(Player player) {
        stopMeditation(player); // Stop gaining Qi when key is released
    }

    public void meditate(Player player) {
        if (meditationTimer != null) {
            return; // Already meditating
        }

        isMeditating = true; // Set meditation state to true
        player.createCommandSourceStack().sendChatMessage(new OutgoingChatMessage.Player(PlayerChatMessage.unsigned(player.getUUID(), "You have started meditating.")), false, ChatType.bind(ChatType.CHAT, player));
        meditationTimer = new Timer();
        meditationTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                gainQi(1); // Gain 1 Qi every second
                player.createCommandSourceStack().sendChatMessage(new OutgoingChatMessage.Player(PlayerChatMessage.unsigned(player.getUUID(), "You are meditating. Current Qi: " + qi)), false, ChatType.bind(ChatType.CHAT, player));
            }
        }, 0, 1000); // Schedule to run every second
    }

    public void stopMeditation(Player player) {
        if (meditationTimer != null) {
            meditationTimer.cancel();
            meditationTimer = null;
        }
        isMeditating = false; // Set meditation state to false
        player.createCommandSourceStack().sendChatMessage(new OutgoingChatMessage.Player(PlayerChatMessage.unsigned(player.getUUID(), "You have stopped meditating.")), false, ChatType.bind(ChatType.CHAT, player));
    }

    public int getQi() {
        return qi;
    }

    public void setQiAmount(int amount) {
        this.qi = amount; // Set the Qi amount
    }

    public boolean isMeditating() {
        return isMeditating; // Return the meditation state
    }

    public boolean canBreakthrough() {
        return currentRealmIndex < BREAKTHROUGH_REQUIREMENTS.length && qi >= BREAKTHROUGH_REQUIREMENTS[currentRealmIndex];
    }

    public void breakthrough(Player player) {
        if (canBreakthrough()) {
            // Increase player stats
            player.setHealth(player.getHealth() + 10); // Gain 10 HP
            //add more stats here, attack damage, armor amount... etc
            
            // Trigger lightning strikes
            int lightningStrikes = (int) Math.pow(4, currentRealmIndex); // Quadruples each time
            for (int i = 0; i < lightningStrikes; i++) {
                player.createCommandSourceStack().sendChatMessage(new OutgoingChatMessage.Player(PlayerChatMessage.unsigned(player.getUUID(), "You are struck by lightning!")), false, ChatType.bind(ChatType.CHAT, player));
            }

            // Move to the next realm and update player name tag
            RealmDisplayManager realmDisplayManager = new RealmDisplayManager();
            realmDisplayManager.updatePlayerNameTag(player, REALMS[currentRealmIndex], currentPath); // Replace "Current Path" with the actual path variable if available
            currentRealmIndex++;
            setRealmLevel(currentRealmIndex); // Update realm level
            player.createCommandSourceStack().sendChatMessage(new OutgoingChatMessage.Player(PlayerChatMessage.unsigned(player.getUUID(), "You have broken through to the realm of " + REALMS[currentRealmIndex - 1] + "!")), false, ChatType.bind(ChatType.CHAT, player));
        } else {
            player.createCommandSourceStack().sendChatMessage(new OutgoingChatMessage.Player(PlayerChatMessage.unsigned(player.getUUID(), "You do not have enough Qi to breakthrough.")), false, ChatType.bind(ChatType.CHAT, player));
        }
    }

    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        nbt.putInt("qi", this.qi);
        nbt.putInt("realmLevel", this.realmLevel); // Serialize realm level
        return nbt;
    }

    public void deserializeNBT(CompoundTag nbt) {
        this.qi = nbt.getInt("qi");
        this.realmLevel = nbt.getInt("realmLevel"); // Deserialize realm level
    }

    public void setCurrentPath(String currentPath) {
        this.currentPath = currentPath; // Set the current path in QiManager
    }

    public String getCurrentPath() {
        return currentPath; // Return the current path
    }
}
