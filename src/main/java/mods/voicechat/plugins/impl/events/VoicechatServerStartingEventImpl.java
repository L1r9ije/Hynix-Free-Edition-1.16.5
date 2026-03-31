package mods.voicechat.plugins.impl.events;

import mods.voicechat.api.VoicechatSocket;
import mods.voicechat.api.events.VoicechatServerStartingEvent;

import javax.annotation.Nullable;

public class VoicechatServerStartingEventImpl extends ServerEventImpl implements VoicechatServerStartingEvent {

    @Nullable
    private VoicechatSocket socketImplementation;

    @Nullable
    @Override
    public VoicechatSocket getSocketImplementation() {
        return socketImplementation;
    }

    @Override
    public void setSocketImplementation(VoicechatSocket socket) {
        this.socketImplementation = socket;
    }

    @Override
    public boolean isCancellable() {
        return false;
    }
}
