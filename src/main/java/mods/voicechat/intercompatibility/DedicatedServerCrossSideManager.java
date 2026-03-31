package mods.voicechat.intercompatibility;

import com.sun.jna.Platform;
import mods.voicechat.Voicechat;
import mods.voicechat.macos.VersionCheck;
import net.minecraft.server.MinecraftServer;

public class DedicatedServerCrossSideManager extends CrossSideManager {

    @Override
    public int getMtuSize() {
        return Voicechat.SERVER_CONFIG.voiceChatMtuSize.get();
    }

    @Override
    public boolean useNatives() {
        if (Platform.isMac()) {
            if (!VersionCheck.isMacOSNativeCompatible()) {
                return false;
            }
        }
        return Voicechat.SERVER_CONFIG.useNatives.get();
    }

    @Override
    public boolean shouldRunVoiceChatServer(MinecraftServer server) {
        return true;
    }
}
