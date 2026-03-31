package mods.voicechat.api.events;

import mods.voicechat.api.VoicechatConnection;

public interface PlayerConnectedEvent extends ServerEvent {

    /**
     * @return the connection of the player
     */
    VoicechatConnection getConnection();

}
