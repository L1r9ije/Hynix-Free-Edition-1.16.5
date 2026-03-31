package mods.voicechat.plugins.impl.events;

import mods.voicechat.api.VoicechatConnection;
import mods.voicechat.api.events.EntitySoundPacketEvent;
import mods.voicechat.api.packets.EntitySoundPacket;

import javax.annotation.Nullable;

public class EntitySoundPacketEventImpl extends SoundPacketEventImpl<EntitySoundPacket> implements EntitySoundPacketEvent {

    public EntitySoundPacketEventImpl(EntitySoundPacket packet, @Nullable VoicechatConnection senderConnection, VoicechatConnection receiverConnection, String source) {
        super(packet, senderConnection, receiverConnection, source);
    }
}
