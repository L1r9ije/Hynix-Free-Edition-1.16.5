package mods.voicechat.api.events;

import mods.voicechat.api.Group;
import mods.voicechat.api.VoicechatConnection;

import javax.annotation.Nullable;

public interface CreateGroupEvent extends GroupEvent {

    /**
     * @return the group that was created
     */
    Group getGroup();

    /**
     * @return the connection of the player that created the group or <code>null</code> if the group was not created by a player
     */
    @Nullable
    VoicechatConnection getConnection();

}
