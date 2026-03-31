package mods.voicechat.plugins.impl.audiochannel;

import mods.voicechat.api.audiochannel.ClientStaticAudioChannel;
import mods.voicechat.voice.common.GroupSoundPacket;
import mods.voicechat.voice.common.SoundPacket;

import java.util.UUID;

public class ClientStaticAudioChannelImpl extends ClientAudioChannelImpl implements ClientStaticAudioChannel {

    public ClientStaticAudioChannelImpl(UUID id) {
        super(id);
    }

    @Override
    protected SoundPacket<?> createSoundPacket(short[] rawAudio) {
        return new GroupSoundPacket(id, id, rawAudio, category);
    }

}
