package mods.voicechat.plugins.impl.events;

import mods.voicechat.api.VoicechatConnection;
import mods.voicechat.api.events.PlayerConnectedEvent;
import mods.voicechat.plugins.impl.VoicechatConnectionImpl;

public class PlayerConnectedEventImpl extends ServerEventImpl implements PlayerConnectedEvent {

    protected VoicechatConnectionImpl connection;

    public PlayerConnectedEventImpl(VoicechatConnectionImpl connection) {
        this.connection = connection;
    }

    @Override
    public VoicechatConnection getConnection() {
        return connection;
    }
}
