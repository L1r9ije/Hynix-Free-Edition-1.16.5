package mods.voicechat.plugins.impl.audiochannel;

import mods.voicechat.api.Position;
import mods.voicechat.api.audiochannel.ClientLocationalAudioChannel;
import mods.voicechat.voice.client.ClientUtils;
import mods.voicechat.voice.common.LocationSoundPacket;
import mods.voicechat.voice.common.SoundPacket;
import net.minecraft.util.math.vector.Vector3d;

import java.util.UUID;

public class ClientLocationalAudioChannelImpl extends ClientAudioChannelImpl implements ClientLocationalAudioChannel {

    private Position position;
    private float distance;

    public ClientLocationalAudioChannelImpl(UUID id, Position position) {
        super(id);
        this.position = position;
        this.distance = ClientUtils.getDefaultDistanceClient();
    }

    @Override
    protected SoundPacket<?> createSoundPacket(short[] rawAudio) {
        return new LocationSoundPacket(id, id, rawAudio, new Vector3d(position.getX(), position.getY(), position.getZ()), distance, category);
    }

    @Override
    public Position getLocation() {
        return position;
    }

    @Override
    public void setLocation(Position position) {
        this.position = position;
    }

    @Override
    public float getDistance() {
        return distance;
    }

    @Override
    public void setDistance(float distance) {
        this.distance = distance;
    }

}
