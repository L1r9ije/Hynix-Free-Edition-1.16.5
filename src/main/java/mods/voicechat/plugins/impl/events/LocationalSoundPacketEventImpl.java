package mods.voicechat.plugins.impl.events;

import mods.voicechat.api.VoicechatConnection;
import mods.voicechat.api.events.LocationalSoundPacketEvent;
import mods.voicechat.api.packets.LocationalSoundPacket;

import javax.annotation.Nullable;

public class LocationalSoundPacketEventImpl extends SoundPacketEventImpl<LocationalSoundPacket> implements LocationalSoundPacketEvent {

    public LocationalSoundPacketEventImpl(LocationalSoundPacket packet, @Nullable VoicechatConnection senderConnection, VoicechatConnection receiverConnection, String source) {
        super(packet, senderConnection, receiverConnection, source);
    }
}
