package mods.voicechat;

import com.darkmagician6.eventapi.EventManager;
import mods.voicechat.config.ConfigMigrator;
import mods.voicechat.intercompatibility.ClientCompatibilityManager;

public class ForgeVoicechatClientMod extends VoicechatClient {

    public ForgeVoicechatClientMod() {
        initializeClient();
        EventManager.register(ClientCompatibilityManager.INSTANCE);
    }

    @Override
    public void initializeConfigs() {
        super.initializeConfigs();
        ConfigMigrator.migrateClientConfig();
    }

}
