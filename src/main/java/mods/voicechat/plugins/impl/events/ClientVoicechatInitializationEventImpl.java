package mods.voicechat.plugins.impl.events;

import mods.voicechat.api.ClientVoicechatSocket;
import mods.voicechat.api.events.ClientVoicechatInitializationEvent;

import javax.annotation.Nullable;

public class ClientVoicechatInitializationEventImpl extends ClientEventImpl implements ClientVoicechatInitializationEvent {

    @Nullable
    private ClientVoicechatSocket socketImplementation;

    @Nullable
    @Override
    public ClientVoicechatSocket getSocketImplementation() {
        return socketImplementation;
    }

    @Override
    public void setSocketImplementation(ClientVoicechatSocket socket) {
        this.socketImplementation = socket;
    }

    @Override
    public boolean isCancellable() {
        return false;
    }

}
