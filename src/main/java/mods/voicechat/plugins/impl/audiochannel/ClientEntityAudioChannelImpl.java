package mods.voicechat.plugins.impl.audiochannel;

import mods.voicechat.api.audiochannel.ClientEntityAudioChannel;
import mods.voicechat.voice.client.ClientUtils;
import mods.voicechat.voice.common.PlayerSoundPacket;
import mods.voicechat.voice.common.SoundPacket;

import java.util.UUID;

public class ClientEntityAudioChannelImpl extends ClientAudioChannelImpl implements ClientEntityAudioChannel {

    private boolean whispering;
    private float distance;

    public ClientEntityAudioChannelImpl(UUID id) {
        super(id);
        this.whispering = false;
        this.distance = ClientUtils.getDefaultDistanceClient();
    }

    @Override
    protected SoundPacket<?> createSoundPacket(short[] rawAudio) {
        return new PlayerSoundPacket(id, id, rawAudio, whispering, distance, category);
    }

    @Override
    public boolean isWhispering() {
        return whispering;
    }

    @Override
    public void setWhispering(boolean whispering) {
        this.whispering = whispering;
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
