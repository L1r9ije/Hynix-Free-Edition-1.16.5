package mods.voicechat.plugins.impl.events;

import mods.voicechat.api.Group;
import mods.voicechat.api.VoicechatConnection;
import mods.voicechat.api.events.LeaveGroupEvent;

import javax.annotation.Nullable;

public class LeaveGroupEventImpl extends GroupEventImpl implements LeaveGroupEvent {

    public LeaveGroupEventImpl(@Nullable Group group, VoicechatConnection connection) {
        super(group, connection);
    }
}
