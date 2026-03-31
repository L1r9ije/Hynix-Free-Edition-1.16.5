package mods.voicechat.plugins.impl.events;

import mods.voicechat.api.events.VoicechatServerStartedEvent;

public class VoicechatServerStartedEventImpl extends ServerEventImpl implements VoicechatServerStartedEvent {

    @Override
    public boolean isCancellable() {
        return false;
    }
}
