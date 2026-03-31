package mods.voicechat.plugins.impl.events;

import mods.voicechat.api.Group;
import mods.voicechat.api.VoicechatConnection;
import mods.voicechat.api.events.JoinGroupEvent;

public class JoinGroupEventImpl extends GroupEventImpl implements JoinGroupEvent {

    public JoinGroupEventImpl(Group group, VoicechatConnection connection) {
        super(group, connection);
    }
}
