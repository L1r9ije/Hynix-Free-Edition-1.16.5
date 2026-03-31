package mods.voicechat.eventforge;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.play.client.CCustomPayloadPacket;
import net.minecraft.network.play.server.SCustomPayloadPlayPacket;
import net.minecraft.util.ResourceLocation;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ForgeNetworkEvents {

    private static final Map<ResourceLocation, ServerCustomPayloadEvent> serverPackets = new ConcurrentHashMap<>();
    private static final Map<ResourceLocation, ClientCustomPayloadEvent> clientPackets = new ConcurrentHashMap<>();

    public static void registerServerPacket(ResourceLocation channel, ServerCustomPayloadEvent event) {
        serverPackets.put(channel, event);
    }

    public static void registerClientPacket(ResourceLocation channel, ClientCustomPayloadEvent event) {
        clientPackets.put(channel, event);
    }

    public static boolean onCustomPayloadServer(CCustomPayloadPacket packet, ServerPlayerEntity player) {
        System.out.println("Received packet on channel: " + packet.getChannelName());
        ServerCustomPayloadEvent event = serverPackets.get(packet.getChannelName());
        if (event != null) {
            event.onCustomPayload(packet, player);
            return true;
        }
        return false;
    }

    public static boolean onCustomPayloadClient(SCustomPayloadPlayPacket packet) {
        System.out.println("Received packet on channel: " + packet.getChannelName());
        ClientCustomPayloadEvent event = clientPackets.get(packet.getChannelName());
        if (event != null) {
            event.onCustomPayload(packet);
            return true;
        }
        return false;
    }


    public interface ServerCustomPayloadEvent {
        void onCustomPayload(CCustomPayloadPacket packet, ServerPlayerEntity player);
    }

    public interface ClientCustomPayloadEvent {
        void onCustomPayload(SCustomPayloadPlayPacket packet);
    }

}
