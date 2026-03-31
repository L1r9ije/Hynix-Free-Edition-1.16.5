package mods.voicechat.plugins.impl.events;

import mods.voicechat.api.events.VoicechatServerStoppedEvent;

public class VoicechatServerStoppedEventImpl extends ServerEventImpl implements VoicechatServerStoppedEvent {

    @Override
    public boolean isCancellable() {
        return false;
    }
}
