package mods.voicechat.plugins.impl.events;

import mods.voicechat.api.Group;
import mods.voicechat.api.VoicechatConnection;
import mods.voicechat.api.events.GroupEvent;

import javax.annotation.Nullable;

public class GroupEventImpl extends ServerEventImpl implements GroupEvent {

    @Nullable
    protected Group group;
    protected VoicechatConnection connection;

    public GroupEventImpl(@Nullable Group group, VoicechatConnection connection) {
        this.group = group;
        this.connection = connection;
    }

    @Nullable
    @Override
    public Group getGroup() {
        return group;
    }

    @Override
    public VoicechatConnection getConnection() {
        return connection;
    }
}
