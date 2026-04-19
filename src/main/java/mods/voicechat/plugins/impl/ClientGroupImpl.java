package mods.voicechat.plugins.impl;

import mods.voicechat.api.Group;
import mods.voicechat.voice.common.ClientGroup;

import java.util.Objects;
import java.util.UUID;

public record ClientGroupImpl(ClientGroup group) implements Group {

    @Override
    public String getName() {
        return group.name();
    }

    @Override
    public boolean hasPassword() {
        return group.hasPassword();
    }

    @Override
    public UUID getId() {
        return group.id();
    }

    @Override
    public boolean isPersistent() {
        return group.persistent();
    }

    @Override
    public boolean isHidden() {
        return group.hidden();
    }

    @Override
    public Type getType() {
        return group.type();
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        ClientGroupImpl that = (ClientGroupImpl) object;
        return Objects.equals(group.id(), that.group.id());
    }

}
