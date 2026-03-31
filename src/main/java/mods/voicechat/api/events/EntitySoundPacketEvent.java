package mods.voicechat.api.events;

import mods.voicechat.api.packets.EntitySoundPacket;

/**
 * This event is emitted when an entity sound packet is about to get sent to a client.
 */
public interface EntitySoundPacketEvent extends SoundPacketEvent<EntitySoundPacket> {

}
