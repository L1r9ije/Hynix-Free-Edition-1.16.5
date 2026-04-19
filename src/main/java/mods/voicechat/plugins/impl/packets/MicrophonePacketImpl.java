package mods.voicechat.plugins.impl.packets;

import mods.voicechat.api.Position;
import mods.voicechat.api.packets.EntitySoundPacket;
import mods.voicechat.api.packets.LocationalSoundPacket;
import mods.voicechat.api.packets.MicrophonePacket;
import mods.voicechat.api.packets.StaticSoundPacket;
import mods.voicechat.plugins.impl.PositionImpl;
import mods.voicechat.voice.common.*;

import java.util.Objects;
import java.util.UUID;

public class MicrophonePacketImpl implements MicrophonePacket {

    private final MicPacket packet;
    private final UUID sender;

    public MicrophonePacketImpl(MicPacket packet, UUID sender) {
        this.packet = packet;
        this.sender = sender;
    }

    @Override
    public boolean isWhispering() {
        return packet.isWhispering();
    }

    @Override
    public byte[] getOpusEncodedData() {
        return packet.getData();
    }

    @Override
    public void setOpusEncodedData(byte[] data) {
        packet.setData(Objects.requireNonNull(data));
    }

    @Override
    public EntitySoundPacket.Builder<?> entitySoundPacketBuilder() {
        return new EntitySoundPacketImpl.BuilderImpl(sender, sender, packet.getData(), packet.getSequenceNumber(), null);
    }

    @Override
    public LocationalSoundPacket.Builder<?> locationalSoundPacketBuilder() {
        return new LocationalSoundPacketImpl.BuilderImpl(sender, sender, packet.getData(), packet.getSequenceNumber(), null);
    }

    @Override
    public StaticSoundPacket.Builder<?> staticSoundPacketBuilder() {
        return new StaticSoundPacketImpl.BuilderImpl(sender, sender, packet.getData(), packet.getSequenceNumber(), null);
    }

    @Override
    @Deprecated
    public EntitySoundPacket toEntitySoundPacket(UUID entityUuid, boolean whispering) {
        return new EntitySoundPacketImpl(new PlayerSoundPacket(sender, sender, packet.getData(), packet.getSequenceNumber(), whispering, Utils.getDefaultDistanceServer(), null));
    }

    @Override
    @Deprecated
    public LocationalSoundPacket toLocationalSoundPacket(Position position) {
        if (position instanceof PositionImpl p) {
            return new LocationalSoundPacketImpl(new LocationSoundPacket(sender, sender, p.position(), packet.getData(), packet.getSequenceNumber(), Utils.getDefaultDistanceServer(), null));
        } else {
            throw new IllegalArgumentException("position is not an instance of PositionImpl");
        }
    }

    @Override
    @Deprecated
    public StaticSoundPacket toStaticSoundPacket() {
        return new StaticSoundPacketImpl(new GroupSoundPacket(sender, sender, packet.getData(), packet.getSequenceNumber(), null));
    }

}
