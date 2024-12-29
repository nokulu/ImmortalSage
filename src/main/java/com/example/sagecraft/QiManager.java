package com.example.sagecraft;
import net.minecraftforge.fml.common.Mod;

/**
 * Manages a player's Qi cultivation and realm progression.
 * Realms progress from Mortal to Immortal Sage as qi accumulates.
 * Each realm requires more qi to break through to the next level.
 */
@Mod.EventBusSubscriber
public class QiManager {
    private int qi;
    private int realmLevel;
    private int currentRealmIndex = 0;
    private boolean isMeditating;
    private String currentPath;
    private IQiStorage qiStorage;

    /** Realm names in order of progression */
    private static final String[] REALMS = {
        "Mortal", "Qi Sensing", "Qi Refining", "Body Strengthening", 
        "Foundation Building", "Golden Core", "Nascent Soul", 
        "Void Severing", "Immortal", "Immortal Sage"
    };
    
    /** Qi amount required to break through to next realm */
    private static final int[] BREAKTHROUGH_REQUIREMENTS = {
        1000, 4000, 16000, 64000, 256000, 1024000, 4096000, 16384000, 65536000
    };

    public static String getRealmName(int level) {
        String[] REALMS = {"Mortal", "Qi Sensing", "Foundation", "Core Formation", 
                          "Golden Core", "Nascent Soul", "Spirit Severing", "Dao Seeking"};
        return REALMS[Math.min(level, REALMS.length - 1)];
    }

    public QiManager(IQiStorage qiStorage) {
        this.qiStorage = qiStorage;
        this.qi = 0;
        this.realmLevel = 0;
        this.currentPath = "Neutral";
    }

    /**
     * Increases qi and checks for realm breakthrough
     * @param amount Amount of qi to gain
     */
    public void gainQi(int amount) {
        this.qi += amount;
        checkBreakthrough();
        qiStorage.setQiAmount(this.qi);
    }

    /**
     * Checks if current qi meets breakthrough requirements
     * Updates realm level if requirements are met
     */
    private void checkBreakthrough() {
        while (currentRealmIndex < BREAKTHROUGH_REQUIREMENTS.length 
               && qi >= BREAKTHROUGH_REQUIREMENTS[currentRealmIndex]) {
            currentRealmIndex++;
            realmLevel = currentRealmIndex;
            qiStorage.setRealmLevel(realmLevel);
        }
    }

    public int getQi() { return qi; }
    public void setQi(int amount) { 
        this.qi = amount;
        checkBreakthrough();
    }

    public String getCurrentRealmName() {
        return REALMS[currentRealmIndex];
    }

    public int getRealmLevel() { return realmLevel; }
    public void setRealmLevel(int level) { 
        this.realmLevel = level;
        this.currentRealmIndex = level;
    }


    public boolean isMeditating() { return isMeditating; }
    public void setMeditating(boolean meditating) { 
        this.isMeditating = meditating;
    }

    public String getCurrentPath() { return currentPath; }
    public void setCurrentPath(String path) { 
        this.currentPath = path;
    }

    /**
     * Gets the qi requirement for next realm breakthrough
     * @return Required qi for next realm, or -1 if at max realm
     */
    public int getNextBreakthroughRequirement() {
        return currentRealmIndex < BREAKTHROUGH_REQUIREMENTS.length ? 
            BREAKTHROUGH_REQUIREMENTS[currentRealmIndex] : -1;
    }

    /**
     * Gets the progress percentage towards next breakthrough
     * @return Progress percentage or 100 if at max realm
     */
    public float getBreakthroughProgress() {
        if (currentRealmIndex >= BREAKTHROUGH_REQUIREMENTS.length) return 100f;
        return ((float)qi / BREAKTHROUGH_REQUIREMENTS[currentRealmIndex]) * 100f;
    }
}