package mods.voicechat.events;

import com.darkmagician6.eventapi.events.Event;
import com.darkmagician6.eventapi.events.callables.EventCancellable;
import mods.voicechat.voice.client.ClientVoicechatConnection;

public class ClientVoiceChatConnectedEvent extends EventCancellable implements Event {

    private final ClientVoicechatConnection client;

    public ClientVoiceChatConnectedEvent(ClientVoicechatConnection client) {
        this.client = client;
    }

    public ClientVoicechatConnection getClient() {
        return client;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }
}
