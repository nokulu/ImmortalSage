package com.example.sagecraft;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

/**
 * Configuration management for Sagecraft mod.
 * Handles cultivation system settings and calculations.
 */
public class Config {
    public static final ForgeConfigSpec SPEC;
    
    // Cultivation Settings
    public static final ForgeConfigSpec.IntValue baseQiGain;
    public static final ForgeConfigSpec.DoubleValue meditationMultiplier;
    public static final ForgeConfigSpec.IntValue realmAdvancementCost;
    public static final ForgeConfigSpec.DoubleValue pathBonusMultiplier;
    
    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        
        builder.comment("Cultivation System Settings");
        builder.push("cultivation");
        
        baseQiGain = builder
            .comment("Base Qi gained per tick while cultivating")
            .translation("config.sagecraft.base_qi_gain")
            .defineInRange("baseQiGain", 1, 0, 100);
            
        meditationMultiplier = builder
            .comment("Multiplier for Qi gain while meditating")
            .translation("config.sagecraft.meditation_multiplier")
            .defineInRange("meditationMultiplier", 2.0, 1.0, 10.0);
            
        realmAdvancementCost = builder
            .comment("Base Qi required to advance to next realm")
            .translation("config.sagecraft.realm_advancement_cost")
            .defineInRange("realmAdvancementCost", 1000, 100, 1000000);
            
        pathBonusMultiplier = builder
            .comment("Multiplier for path-specific cultivation bonuses")
            .translation("config.sagecraft.path_bonus_multiplier")
            .defineInRange("pathBonusMultiplier", 1.5, 1.0, 5.0);
        
        builder.pop();
        SPEC = builder.build();
    }
    
    /**
     * Registers configuration with Forge mod system
     */
    public static void register() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, SPEC);
    }
    
    /**
     * Calculates qi gain based on meditation state and path
     * @param isMeditating Whether player is meditating
     * @param path Current cultivation path
     * @return Amount of qi to gain
     */
    public static int getQiGain(boolean isMeditating, String path) {
        double base = baseQiGain.get();
        if (isMeditating) {
            base *= meditationMultiplier.get();
        }
        if (!"Neutral".equals(path)) {
            base *= pathBonusMultiplier.get();
        }
        return (int)base;
    }
    
    /**
     * Calculates qi cost for realm advancement
     * @param currentRealm Current cultivation realm
     * @return Required qi for next realm
     */
    public static int getRealmCost(int currentRealm) {
        return realmAdvancementCost.get() * (currentRealm + 1);
    }
}