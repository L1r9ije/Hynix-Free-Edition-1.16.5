package mods.voicechat.events;

import com.darkmagician6.eventapi.events.Event;
import com.darkmagician6.eventapi.events.callables.EventCancellable;
import net.minecraft.entity.player.ServerPlayerEntity;

public class ServerVoiceChatConnectedEvent extends EventCancellable implements Event {

    private final ServerPlayerEntity player;

    public ServerVoiceChatConnectedEvent(ServerPlayerEntity player) {
        this.player = player;
    }

    public ServerPlayerEntity getPlayer() {
        return player;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }
}
