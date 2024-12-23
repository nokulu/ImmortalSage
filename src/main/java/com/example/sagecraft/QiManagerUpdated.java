package com.example.sagecraft;

import java.util.Timer;
import java.util.TimerTask; // Import for Component

import net.minecraft.client.Minecraft; // Import for OutgoingChatMessage
import net.minecraft.nbt.CompoundTag; // Import for PlayerChatMessage
import net.minecraft.network.chat.ChatType; // Import for ChatType
import net.minecraft.network.chat.OutgoingChatMessage; // Existing import
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent; // Import for Minecraft instance
import net.minecraftforge.eventbus.api.SubscribeEvent; // Import for CompoundTag
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
    
    private Timer meditationTimer;

    public QiManagerUpdated() {
        this.qi = 0; // Initialize Qi to 0
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
        if (meditationTimer != null) {
            return; // Already meditating
        }

        meditationTimer = new Timer();
        meditationTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                gainQi(1); // Gain 1 Qi every second
                player.createCommandSourceStack().sendChatMessage(new OutgoingChatMessage.Player(PlayerChatMessage.unsigned(player.getUUID(), "You are meditating. Current Qi: " + qi)), false, ChatType.bind(ChatType.CHAT, player));
            }
        }, 0, 1000); // Schedule to run every second
    }

    public void stopMeditation() {
        if (meditationTimer != null) {
            meditationTimer.cancel();
            meditationTimer = null;
        }
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
            //add more stats here, attack damage, armor ammount... etc
            
            // Trigger lightning strikes
            int lightningStrikes = (int) Math.pow(4, currentRealmIndex); // Quadruples each time
            for (int i = 0; i < lightningStrikes; i++) {
                player.createCommandSourceStack().sendChatMessage(new OutgoingChatMessage.Player(PlayerChatMessage.unsigned(player.getUUID(), "You are struck by lightning!")), false, ChatType.bind(ChatType.CHAT, player));
            }

            // Move to the next realm and update player name tag
            RealmDisplayManager realmDisplayManager = new RealmDisplayManager();
            realmDisplayManager.updatePlayerNameTag(player, REALMS[currentRealmIndex], "Current Path"); // Replace "Current Path" with the actual path variable if available
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
