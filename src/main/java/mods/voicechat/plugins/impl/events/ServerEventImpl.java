package mods.voicechat.plugins.impl.events;

import mods.voicechat.api.VoicechatServerApi;
import mods.voicechat.api.events.ServerEvent;
import mods.voicechat.plugins.impl.VoicechatServerApiImpl;

public class ServerEventImpl extends EventImpl implements ServerEvent {

    @Override
    public VoicechatServerApi getVoicechat() {
        return VoicechatServerApiImpl.instance();
    }

}
