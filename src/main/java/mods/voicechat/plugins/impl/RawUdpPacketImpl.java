package mods.voicechat.plugins.impl;

import mods.voicechat.api.RawUdpPacket;

import java.net.SocketAddress;

public record RawUdpPacketImpl(byte[] data, SocketAddress socketAddress, long timestamp) implements RawUdpPacket {

}
