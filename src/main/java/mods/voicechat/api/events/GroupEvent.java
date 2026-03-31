package mods.voicechat.api.events;

import mods.voicechat.api.Group;
import mods.voicechat.api.VoicechatConnection;

import javax.annotation.Nullable;

public interface GroupEvent extends ServerEvent {

    /**
     * @return the group - <code>null</code> if there is no group
     */
    @Nullable
    Group getGroup();

    /**
     * @return the connection of the player
     */
    @Nullable
    VoicechatConnection getConnection();

}
