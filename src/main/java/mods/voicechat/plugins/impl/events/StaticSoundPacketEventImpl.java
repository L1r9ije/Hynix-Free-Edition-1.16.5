package mods.voicechat.plugins.impl.events;

import mods.voicechat.api.VoicechatConnection;
import mods.voicechat.api.events.StaticSoundPacketEvent;
import mods.voicechat.api.packets.StaticSoundPacket;

import javax.annotation.Nullable;

public class StaticSoundPacketEventImpl extends SoundPacketEventImpl<StaticSoundPacket> implements StaticSoundPacketEvent {

    public StaticSoundPacketEventImpl(StaticSoundPacket packet, @Nullable VoicechatConnection senderConnection, VoicechatConnection receiverConnection, String source) {
        super(packet, senderConnection, receiverConnection, source);
    }
}
