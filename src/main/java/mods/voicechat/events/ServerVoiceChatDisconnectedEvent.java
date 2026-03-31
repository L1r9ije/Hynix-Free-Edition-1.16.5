package mods.voicechat.events;

import com.darkmagician6.eventapi.events.Event;
import com.darkmagician6.eventapi.events.callables.EventCancellable;

import java.util.UUID;

public class ServerVoiceChatDisconnectedEvent extends EventCancellable implements Event {

    private final UUID playerID;

    public ServerVoiceChatDisconnectedEvent(UUID playerID) {
        this.playerID = playerID;
    }

    public UUID getPlayerID() {
        return playerID;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

}
