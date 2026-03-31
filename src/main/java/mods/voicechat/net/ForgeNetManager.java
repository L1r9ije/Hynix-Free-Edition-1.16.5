package mods.voicechat.net;

import mods.voicechat.Voicechat;
import mods.voicechat.eventforge.ForgeNetworkEvents;
import net.minecraft.client.Minecraft;


public class ForgeNetManager extends NetManager {

    @Override
    public <T extends Packet<T>> Channel<T> registerReceiver(Class<T> packetType, boolean toClient, boolean toServer) {
        ClientServerChannel<T> c = new ClientServerChannel<>();
        try {
            T dummyPacket = packetType.getDeclaredConstructor().newInstance();

            if (toServer) {
                ForgeNetworkEvents.registerServerPacket(dummyPacket.getIdentifier(), (packet, player) -> {
                    try {
                        if (!Voicechat.SERVER.isCompatible(player) && !packetType.equals(RequestSecretPacket.class)) {
                            return;
                        }
                        T vcPacket = packetType.getDeclaredConstructor().newInstance();
                        vcPacket.fromBytes(packet.getBufferData());
                        c.onServerPacket(player.server, player, player.connection, vcPacket);
                    } catch (Exception e) {
                        Voicechat.LOGGER.error("Failed to process packet", e);
                    }
                });
            }


            if (toClient) {
                ForgeNetworkEvents.registerClientPacket(dummyPacket.getIdentifier(), payload -> {
                    try {
                        T packet = packetType.getDeclaredConstructor().newInstance();
                        packet.fromBytes(payload.getBufferData());
                        onClientPacket(c, packet);
                    } catch (Exception e) {
                        Voicechat.LOGGER.error("Failed to process packet", e);
                    }
                });
            }
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
        return c;
    }


    private <T extends Packet<T>> void onClientPacket(ClientServerChannel<T> channel, T packet) {
        channel.onClientPacket(Minecraft.getInstance(), Minecraft.getInstance().getConnection(), packet);
    }

}
