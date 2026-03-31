package mods.voicechat.plugins.impl.audiochannel;

import mods.voicechat.api.audiochannel.ClientAudioChannel;
import mods.voicechat.voice.client.ClientManager;
import mods.voicechat.voice.client.ClientVoicechat;
import mods.voicechat.voice.common.SoundPacket;

import javax.annotation.Nullable;
import java.util.UUID;

public abstract class ClientAudioChannelImpl implements ClientAudioChannel {

    protected UUID id;
    @Nullable
    protected String category;

    public ClientAudioChannelImpl(UUID id) {
        this.id = id;
    }

    @Override
    public UUID getId() {
        return id;
    }

    protected abstract SoundPacket<?> createSoundPacket(short[] rawAudio);

    @Override
    public void play(short[] rawAudio) {
        ClientVoicechat client = ClientManager.getClient();
        if (client != null) {
            client.processSoundPacket(createSoundPacket(rawAudio));
        }
    }

    @Nullable
    @Override
    public String getCategory() {
        return category;
    }

    @Override
    public void setCategory(@Nullable String category) {
        this.category = category;
    }
}
