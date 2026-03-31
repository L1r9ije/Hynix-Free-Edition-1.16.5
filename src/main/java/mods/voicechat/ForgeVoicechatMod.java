package mods.voicechat;

import com.darkmagician6.eventapi.EventManager;
import mods.voicechat.config.ConfigMigrator;
import mods.voicechat.intercompatibility.CommonCompatibilityManager;

public class ForgeVoicechatMod extends Voicechat {

    public ForgeVoicechatMod() {
        initialize();
        EventManager.register(new ConfigMigrator());
        EventManager.register(CommonCompatibilityManager.INSTANCE);
    }
}