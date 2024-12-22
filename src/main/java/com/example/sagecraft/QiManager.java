package main.java.com.example.sagecraft;

import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Timer;
import java.util.TimerTask;

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

    public QiManager() {
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
                player.sendMessage(new StringTextComponent("You are meditating. Current Qi: " + qi));
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
            // Assuming player has methods to increase armor and damage
            player.increaseArmor(3); // Gain 3 armor
            player.increaseDamage(5); // Gain 5 damage
            
            // Trigger lightning strikes
            int lightningStrikes = (int) Math.pow(4, currentRealmIndex); // Quadruples each time
            for (int i = 0; i < lightningStrikes; i++) {
                // Logic to trigger lightning strike on player
                player.sendMessage(new StringTextComponent("You are struck by lightning!"));
            }

            // Move to the next realm
            currentRealmIndex++;
            player.sendMessage(new StringTextComponent("You have broken through to the realm of " + REALMS[currentRealmIndex - 1] + "!"));
        } else {
            player.sendMessage(new StringTextComponent("You do not have enough Qi to breakthrough."));
        }
    }

    @SubscribeEvent
    public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getPlayer();
        player.sendMessage(new StringTextComponent("You have " + qi + " Qi."));
    }
}
</create_file>
