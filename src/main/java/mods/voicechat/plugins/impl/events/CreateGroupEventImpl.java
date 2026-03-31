package mods.voicechat.plugins.impl.events;

import mods.voicechat.api.Group;
import mods.voicechat.api.VoicechatConnection;
import mods.voicechat.api.events.CreateGroupEvent;

import javax.annotation.Nullable;

public class CreateGroupEventImpl extends GroupEventImpl implements CreateGroupEvent {

    public CreateGroupEventImpl(Group group, @Nullable VoicechatConnection connection) {
        super(group, connection);
    }
}
