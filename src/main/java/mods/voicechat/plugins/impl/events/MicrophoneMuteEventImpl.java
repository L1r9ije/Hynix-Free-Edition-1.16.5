package mods.voicechat.plugins.impl.events;

import mods.voicechat.api.events.MicrophoneMuteEvent;

public class MicrophoneMuteEventImpl extends ClientEventImpl implements MicrophoneMuteEvent {

    private final boolean muted;

    public MicrophoneMuteEventImpl(boolean muted) {
        super();
        this.muted = muted;
    }

    @Override
    public boolean isDisabled() {
        return muted;
    }
}
