package mods.voicechat.api.events;

import mods.voicechat.api.Group;
import mods.voicechat.api.VoicechatConnection;

public interface JoinGroupEvent extends GroupEvent {

    /**
     * @return the group that was joined
     */
    Group getGroup();

    /**
     * @return the connection of the player
     */
    VoicechatConnection getConnection();

}
