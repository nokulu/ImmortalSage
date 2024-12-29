package com.example.sagecraft;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

public class ScreenManager {
    private static final Map<Class<? extends Screen>, Screen> screens = new HashMap<>();

    public static <T extends Screen> void register(Class<T> screenClass, T screenInstance) {
        screens.put(screenClass, screenInstance);
    }

    public static void display(Class<? extends Screen> screenClass) {
        Screen screen = screens.get(screenClass);
        if (screen != null) {
            Minecraft.getInstance().setScreen(screen);
        } else {
            System.err.println("Screen not registered: " + screenClass.getSimpleName());
        }
    }
}
