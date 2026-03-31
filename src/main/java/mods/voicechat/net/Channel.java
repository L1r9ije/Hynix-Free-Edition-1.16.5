package mods.voicechat.net;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.play.ServerPlayNetHandler;
import net.minecraft.server.MinecraftServer;

import javax.annotation.Nullable;

public class Channel<T extends Packet<T>> {

    @Nullable
    private NetManager.ServerReceiver<T> serverListener;

    public Channel() {

    }

    public void setServerListener(NetManager.ServerReceiver<T> packetReceiver) {
        serverListener = packetReceiver;
    }

    public void onServerPacket(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetHandler handler, T packet) {
        server.execute(() -> {
            if (serverListener != null) {
                serverListener.onPacket(server, player, handler, packet);
            }
        });
    }

}
