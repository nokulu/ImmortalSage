package com.example.sagecraft;

import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.Event;

@Event.HasResult
public class RealmLevelUpEvent extends PlayerEvent {
    private final int newRealmLevel;
    private final int previousRealmLevel;

    public RealmLevelUpEvent(Player player, int previousRealmLevel, int newRealmLevel) {
        super(player);
        this.previousRealmLevel = previousRealmLevel;
        this.newRealmLevel = newRealmLevel;
    }

    public RealmLevelUpEvent(Player player, int newRealmLevel) {
        this(player, 
            player.getCapability(QiCapability.CAPABILITY_QI_MANAGER)
                .map(IQiStorage::getRealmLevel)
                .orElse(0),
            newRealmLevel);
    }

    public int getNewRealmLevel() {
        return newRealmLevel;
    }

    public int getPreviousRealmLevel() {
        return previousRealmLevel;
    }

    public double getRequiredQi() {
return Config.realmAdvancementCost.get() * Math.pow(2, newRealmLevel);
    }
}
