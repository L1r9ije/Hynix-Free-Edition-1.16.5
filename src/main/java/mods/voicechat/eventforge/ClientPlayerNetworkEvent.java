package mods.voicechat.eventforge;

import com.darkmagician6.eventapi.events.Event;
import com.darkmagician6.eventapi.events.callables.EventCancellable;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.multiplayer.PlayerController;
import net.minecraft.network.NetworkManager;

import javax.annotation.Nullable;

public class ClientPlayerNetworkEvent extends EventCancellable implements Event {
    private final PlayerController controller;
    private final ClientPlayerEntity player;
    private final NetworkManager networkManager;

    ClientPlayerNetworkEvent(PlayerController controller, ClientPlayerEntity player, NetworkManager networkManager) {
        this.controller = controller;
        this.player = player;
        this.networkManager = networkManager;
    }

    @Nullable
    public PlayerController getController() {
        return this.controller;
    }

    @Nullable
    public ClientPlayerEntity getPlayer() {
        return this.player;
    }

    @Nullable
    public NetworkManager getNetworkManager() {
        return this.networkManager;
    }

    public static class LoggedInEvent extends ClientPlayerNetworkEvent {
        public LoggedInEvent(PlayerController controller, ClientPlayerEntity player, NetworkManager networkManager) {
            super(controller, player, networkManager);
        }
    }

    public static class LoggedOutEvent extends ClientPlayerNetworkEvent {
        public LoggedOutEvent(PlayerController controller, ClientPlayerEntity player, NetworkManager networkManager) {
            super(controller, player, networkManager);
        }
    }
}