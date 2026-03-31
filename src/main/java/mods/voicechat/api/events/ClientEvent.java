package mods.voicechat.api.events;

import mods.voicechat.api.VoicechatClientApi;

public interface ClientEvent extends Event {

    /**
     * @return the voice chat client API
     */
    VoicechatClientApi getVoicechat();

}
