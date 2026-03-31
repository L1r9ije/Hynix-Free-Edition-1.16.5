package mods.voicechat.plugins.impl.audiochannel;

import mods.voicechat.api.audiochannel.StaticAudioChannel;
import mods.voicechat.api.packets.MicrophonePacket;
import mods.voicechat.plugins.impl.VoicechatConnectionImpl;
import mods.voicechat.plugins.impl.VoicechatServerApiImpl;
import mods.voicechat.voice.common.GroupSoundPacket;
import mods.voicechat.voice.server.Server;

import java.util.UUID;

public class StaticAudioChannelImpl extends AudioChannelImpl implements StaticAudioChannel {

    protected VoicechatConnectionImpl connection;

    public StaticAudioChannelImpl(UUID channelId, Server server, VoicechatConnectionImpl connection) {
        super(channelId, server);
        this.connection = connection;
    }

    @Override
    public void send(byte[] opusData) {
        broadcast(new GroupSoundPacket(channelId, channelId, opusData, sequenceNumber.getAndIncrement(), category));
    }

    @Override
    public void send(MicrophonePacket packet) {
        send(packet.getOpusEncodedData());
    }

    @Override
    public void flush() {
        GroupSoundPacket packet = new GroupSoundPacket(channelId, channelId, new byte[0], sequenceNumber.getAndIncrement(), category);
        broadcast(packet);
    }

    private void broadcast(GroupSoundPacket packet) {
        VoicechatServerApiImpl.sendPacket(connection, packet);
    }

}
