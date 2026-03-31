package mods.voicechat.plugins.impl.events;

import mods.voicechat.api.events.PlayerDisconnectedEvent;

import java.util.UUID;

public class PlayerDisconnectedEventImpl extends ServerEventImpl implements PlayerDisconnectedEvent {

    protected UUID player;

    public PlayerDisconnectedEventImpl(UUID player) {
        this.player = player;
    }

    @Override
    public UUID getPlayerUuid() {
        return player;
    }
}
