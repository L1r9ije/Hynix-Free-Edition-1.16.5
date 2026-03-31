package mods.voicechat.api.events;

import mods.voicechat.api.VoicechatServerApi;

public interface ServerEvent extends Event {

    /**
     * @return the voice chat server API
     */
    VoicechatServerApi getVoicechat();

}
