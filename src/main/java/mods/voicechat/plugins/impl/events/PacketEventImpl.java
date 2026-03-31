package mods.voicechat.plugins.impl.events;

import mods.voicechat.api.VoicechatConnection;
import mods.voicechat.api.events.PacketEvent;
import mods.voicechat.api.packets.Packet;

import javax.annotation.Nullable;

public class PacketEventImpl<T extends Packet> extends ServerEventImpl implements PacketEvent<T> {

    private final T packet;
    @Nullable
    private final VoicechatConnection receiverConnection;
    @Nullable
    private final VoicechatConnection senderConnection;

    public PacketEventImpl(T packet, @Nullable VoicechatConnection senderConnection, @Nullable VoicechatConnection receiverConnection) {
        this.packet = packet;
        this.senderConnection = senderConnection;
        this.receiverConnection = receiverConnection;
    }

    @Override
    public T getPacket() {
        return packet;
    }

    @Nullable
    @Override
    public VoicechatConnection getReceiverConnection() {
        return receiverConnection;
    }

    @Nullable
    @Override
    public VoicechatConnection getSenderConnection() {
        return senderConnection;
    }
}
