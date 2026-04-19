package mods.voicechat.api;

import java.net.SocketAddress;

public interface RawUdpPacket {

    byte[] data();

    long timestamp();

    SocketAddress socketAddress();

}
