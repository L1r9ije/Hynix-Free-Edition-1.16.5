package mods.voicechat.voice.common;

import mods.voicechat.api.Group;
import mods.voicechat.plugins.impl.GroupImpl;
import net.minecraft.network.PacketBuffer;

import java.util.Objects;
import java.util.UUID;

public record ClientGroup(UUID id, String name, boolean hasPassword, boolean persistent, boolean hidden,
                          Group.Type type) {

    public static ClientGroup fromBytes(PacketBuffer buf) {
        return new ClientGroup(buf.readUniqueId(), buf.readString(512), buf.readBoolean(), buf.readBoolean(), buf.readBoolean(), GroupImpl.TypeImpl.fromInt(buf.readShort()));
    }

    public void toBytes(PacketBuffer buf) {
        buf.writeUniqueId(id);
        buf.writeString(name, 512);
        buf.writeBoolean(hasPassword);
        buf.writeBoolean(persistent);
        buf.writeBoolean(hidden);
        buf.writeShort(GroupImpl.TypeImpl.toInt(type));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ClientGroup group = (ClientGroup) o;

        return Objects.equals(id, group.id);
    }
}
