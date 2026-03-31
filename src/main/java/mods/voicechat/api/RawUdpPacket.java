package mods.voicechat.api;

import java.net.SocketAddress;

public interface RawUdpPacket {

    byte[] getData();

    long getTimestamp();

    SocketAddress getSocketAddress();

}
