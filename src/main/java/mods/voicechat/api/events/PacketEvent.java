package mods.voicechat.api.events;

import mods.voicechat.api.VoicechatConnection;
import mods.voicechat.api.packets.Packet;

import javax.annotation.Nullable;

public interface PacketEvent<T extends Packet> extends ServerEvent {

    /**
     * @return the packet
     */
    T getPacket();

    /**
     * @return the connection of the player that should receive this packet
     */
    @Nullable
    VoicechatConnection getReceiverConnection();

    /**
     * @return the connection of the player that sent this packet or <code>null</code> if it wasn't sent by a player
     */
    @Nullable
    VoicechatConnection getSenderConnection();

}