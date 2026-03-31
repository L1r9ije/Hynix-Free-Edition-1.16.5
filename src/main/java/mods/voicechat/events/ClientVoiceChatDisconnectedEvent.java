package mods.voicechat.events;

import com.darkmagician6.eventapi.events.Event;
import com.darkmagician6.eventapi.events.callables.EventCancellable;

public class ClientVoiceChatDisconnectedEvent extends EventCancellable implements Event {

    @Override
    public boolean isCancelled() {
        return false;
    }

}
