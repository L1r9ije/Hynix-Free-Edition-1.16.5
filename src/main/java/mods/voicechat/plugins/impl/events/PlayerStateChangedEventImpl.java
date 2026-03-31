package mods.voicechat.plugins.impl.events;

import mods.voicechat.Voicechat;
import mods.voicechat.api.VoicechatConnection;
import mods.voicechat.api.events.PlayerStateChangedEvent;
import mods.voicechat.plugins.impl.VoicechatConnectionImpl;
import mods.voicechat.voice.common.PlayerState;
import mods.voicechat.voice.server.Server;
import net.minecraft.entity.player.ServerPlayerEntity;

import javax.annotation.Nullable;
import java.util.UUID;

public class PlayerStateChangedEventImpl extends ServerEventImpl implements PlayerStateChangedEvent {

    protected final PlayerState state;
    @Nullable
    protected VoicechatConnectionImpl connection;

    public PlayerStateChangedEventImpl(PlayerState state) {
        this.state = state;
    }

    @Override
    public boolean isDisabled() {
        return state.isDisabled();
    }

    @Override
    public boolean isDisconnected() {
        return state.isDisconnected();
    }

    @Override
    public UUID getPlayerUuid() {
        return state.getUuid();
    }

    @Override
    @Nullable
    public VoicechatConnection getConnection() {
        if (connection == null) {
            Server server = Voicechat.SERVER.getServer();
            if (server == null) {
                return null;
            }
            ServerPlayerEntity player = server.getServer().getPlayerList().getPlayerByUUID(state.getUuid());
            if (player == null) {
                return null;
            }
            connection = VoicechatConnectionImpl.fromPlayer(player);
        }
        return connection;
    }
}
