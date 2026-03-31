package mods.voicechat.api.events;

import mods.voicechat.api.Group;
import mods.voicechat.api.VoicechatConnection;

import javax.annotation.Nullable;

public interface LeaveGroupEvent extends GroupEvent {

    /**
     * @return the group that was left or <code>null</code> if the player was not in a group
     */
    @Nullable
    Group getGroup();

    /**
     * @return the connection of the player
     */
    VoicechatConnection getConnection();

}
