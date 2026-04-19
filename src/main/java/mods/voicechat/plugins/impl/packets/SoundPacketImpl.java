package mods.voicechat.plugins.impl.packets;

import mods.voicechat.api.Position;
import mods.voicechat.api.packets.EntitySoundPacket;
import mods.voicechat.api.packets.LocationalSoundPacket;
import mods.voicechat.api.packets.SoundPacket;
import mods.voicechat.api.packets.StaticSoundPacket;
import mods.voicechat.plugins.impl.PositionImpl;
import mods.voicechat.voice.common.GroupSoundPacket;
import mods.voicechat.voice.common.LocationSoundPacket;
import mods.voicechat.voice.common.PlayerSoundPacket;
import mods.voicechat.voice.common.Utils;

import javax.annotation.Nullable;
import java.util.UUID;

public class SoundPacketImpl implements SoundPacket {

    private final mods.voicechat.voice.common.SoundPacket<?> packet;

    public SoundPacketImpl(mods.voicechat.voice.common.SoundPacket<?> packet) {
        this.packet = packet;
    }

    @Override
    public UUID getChannelId() {
        return packet.getChannelId();
    }

    @Override
    public UUID getSender() {
        return packet.getSender();
    }

    @Override
    public byte[] getOpusEncodedData() {
        return packet.getData();
    }

    @Override
    public long getSequenceNumber() {
        return packet.getSequenceNumber();
    }

    @Nullable
    @Override
    public String getCategory() {
        return packet.getCategory();
    }

    public mods.voicechat.voice.common.SoundPacket<?> getPacket() {
        return packet;
    }

    @Override
    public EntitySoundPacket.Builder<?> entitySoundPacketBuilder() {
        return new EntitySoundPacketImpl.BuilderImpl(this);
    }

    @Override
    public LocationalSoundPacket.Builder<?> locationalSoundPacketBuilder() {
        return new LocationalSoundPacketImpl.BuilderImpl(this);
    }

    @Override
    public StaticSoundPacket.Builder<?> staticSoundPacketBuilder() {
        return new StaticSoundPacketImpl.BuilderImpl(this);
    }

    @Override
    public EntitySoundPacket toEntitySoundPacket(UUID entityUuid, boolean whispering) {
        return new EntitySoundPacketImpl(new PlayerSoundPacket(packet.getChannelId(), packet.getSender(), packet.getData(), packet.getSequenceNumber(), whispering, getDistance(), null));
    }

    @Override
    public LocationalSoundPacket toLocationalSoundPacket(Position position) {
        if (position instanceof PositionImpl p) {
            return new LocationalSoundPacketImpl(new LocationSoundPacket(packet.getChannelId(), packet.getSender(), p.position(), packet.getData(), packet.getSequenceNumber(), getDistance(), null));
        } else {
            throw new IllegalArgumentException("position is not an instance of PositionImpl");
        }
    }

    private float getDistance() {
        if (this instanceof EntitySoundPacket p) {
            return p.getDistance();
        } else if (this instanceof LocationalSoundPacket p) {
            return p.getDistance();
        }
        return Utils.getDefaultDistanceServer();
    }

    @Override
    public StaticSoundPacket toStaticSoundPacket() {
        return new StaticSoundPacketImpl(new GroupSoundPacket(packet.getChannelId(), packet.getSender(), packet.getData(), packet.getSequenceNumber(), null));
    }

    public abstract static class BuilderImpl<T extends BuilderImpl<T, P>, P extends SoundPacket> implements Builder<T, P> {

        protected UUID channelId;
        protected UUID sender;
        protected byte[] opusEncodedData;
        protected long sequenceNumber;
        @Nullable
        protected String category;

        public BuilderImpl(SoundPacketImpl soundPacket) {
            this.channelId = soundPacket.getChannelId();
            this.sender = soundPacket.getSender();
            this.opusEncodedData = soundPacket.getOpusEncodedData();
            this.sequenceNumber = soundPacket.getSequenceNumber();
            this.category = soundPacket.getCategory();
        }

        public BuilderImpl(UUID channelId, UUID sender, byte[] opusEncodedData, long sequenceNumber, @Nullable String category) {
            this.channelId = channelId;
            this.sender = sender;
            this.opusEncodedData = opusEncodedData;
            this.sequenceNumber = sequenceNumber;
            this.category = category;
        }

        @Override
        public T channelId(UUID channelId) {
            if (channelId == null) {
                throw new IllegalArgumentException("channelId can't be null");
            }
            this.channelId = channelId;
            return (T) this;
        }

        @Override
        public T opusEncodedData(byte[] data) {
            this.opusEncodedData = data;
            return (T) this;
        }

        @Override
        public T category(@Nullable String category) {
            this.category = category;
            return (T) this;
        }
    }

}
