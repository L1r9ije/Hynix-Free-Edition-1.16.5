package mods.voicechat.api.events;

import mods.voicechat.api.Group;
import mods.voicechat.api.VoicechatConnection;

import javax.annotation.Nullable;

/**
 * This event is only cancellable if the group is persistent
 */
public interface RemoveGroupEvent extends GroupEvent {

    /**
     * @return the group that was removed
     */
    Group getGroup();

    /**
     * @return <code>null</code>
     */
    @Nullable
    @Deprecated
    VoicechatConnection getConnection();

}
