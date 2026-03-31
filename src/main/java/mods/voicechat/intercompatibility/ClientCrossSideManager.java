package mods.voicechat.intercompatibility;

import com.sun.jna.Platform;
import mods.voicechat.Voicechat;
import mods.voicechat.VoicechatClient;
import mods.voicechat.macos.VersionCheck;
import mods.voicechat.voice.client.ClientManager;
import mods.voicechat.voice.client.ClientVoicechat;
import mods.voicechat.voice.client.ClientVoicechatConnection;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;

public class ClientCrossSideManager extends CrossSideManager {

    public ClientCrossSideManager() {

    }

    @Override
    public int getMtuSize() {
        ClientVoicechat client = ClientManager.getClient();
        if (client != null) {
            ClientVoicechatConnection connection = client.getConnection();
            if (connection != null) {
                return connection.getData().getMtuSize();
            }
        }
        return Voicechat.SERVER_CONFIG.voiceChatMtuSize.get();
    }

    @Override
    public boolean useNatives() {
        if (Platform.isMac()) {
            if (!VersionCheck.isMacOSNativeCompatible()) {
                return false;
            }
        }
        if (VoicechatClient.CLIENT_CONFIG == null) {
            return Voicechat.SERVER_CONFIG.useNatives.get();
        }
        return VoicechatClient.CLIENT_CONFIG.useNatives.get();
    }

    @Override
    public boolean shouldRunVoiceChatServer(MinecraftServer server) {
        return server instanceof DedicatedServer || VoicechatClient.CLIENT_CONFIG == null || VoicechatClient.CLIENT_CONFIG.runLocalServer.get();
    }

}
