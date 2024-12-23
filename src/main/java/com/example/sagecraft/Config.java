package com.example.sagecraft;

import net.minecraftforge.common.ForgeConfigSpec;

public class Config {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.IntValue exampleValue;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        
        // Define configuration options
        exampleValue = builder
            .comment("An example configuration value")
            .defineInRange("exampleValue", 10, 0, 100); // Default value, min, max
        
        SPEC = builder.build();
    }
}
