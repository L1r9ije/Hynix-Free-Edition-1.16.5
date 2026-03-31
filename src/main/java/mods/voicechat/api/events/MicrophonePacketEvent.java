package mods.voicechat.api.events;

import mods.voicechat.api.packets.MicrophonePacket;

/**
 * This event is emitted when a microphone packet arrives at the server.
 */
public interface MicrophonePacketEvent extends PacketEvent<MicrophonePacket> {

}
