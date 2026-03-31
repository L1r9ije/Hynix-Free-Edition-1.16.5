package mods.voicechat.plugins.impl.events;

import mods.voicechat.api.Group;
import mods.voicechat.api.events.RemoveGroupEvent;

public class RemoveGroupEventImpl extends GroupEventImpl implements RemoveGroupEvent {

    public RemoveGroupEventImpl(Group group) {
        super(group, null);
    }

    @Override
    public boolean isCancellable() {
        return super.isCancellable() && group.isPersistent();
    }
}
