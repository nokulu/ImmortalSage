package main.java.com.example.sagecraft;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public class RealmDisplayManager {
    public void updatePlayerNameTag(Player player, String realm, String path) {
        String nameTag = "Realm: " + realm + " | Path: " + path;
        player.setCustomName(Component.literal(nameTag));
    }
}
