package com.example.sagecraft;

import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.Event;

/**
 * Event triggered when Qi-related data changes
 */
@Event.HasResult
public class QiDataChangeEvent extends PlayerEvent {
    private final ChangeType changeType;
    private final Object oldValue;
    private final Object newValue;

    public QiDataChangeEvent(Player player, ChangeType changeType, Object oldValue, Object newValue) {
        super(player);
        this.changeType = changeType;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    public ChangeType getChangeType() {
        return changeType;
    }

    public Object getOldValue() {
        return oldValue;
    }

    public Object getNewValue() {
        return newValue;
    }

    public enum ChangeType {
        QI_AMOUNT,
        PATH,
        REALM_LEVEL
    }
}
