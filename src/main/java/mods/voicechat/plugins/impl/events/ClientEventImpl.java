package mods.voicechat.plugins.impl.events;

import mods.voicechat.api.VoicechatClientApi;
import mods.voicechat.api.events.ClientEvent;
import mods.voicechat.plugins.impl.VoicechatClientApiImpl;

public class ClientEventImpl extends EventImpl implements ClientEvent {

    @Override
    public VoicechatClientApi getVoicechat() {
        return VoicechatClientApiImpl.instance();
    }
}
