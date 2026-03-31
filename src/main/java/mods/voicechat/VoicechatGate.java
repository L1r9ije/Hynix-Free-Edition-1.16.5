package mods.voicechat;

import su.hynix.hynix;
import su.hynix.modules.ModuleManager;
import su.hynix.modules.impl.miscellaneous.VoiceChat;

public final class VoicechatGate {
    public static boolean isEnabled() {
        ModuleManager moduleManager = hynix.getInstance().getModuleManager();
        VoiceChat voiceMode = (VoiceChat) moduleManager.getModule(VoiceChat.class);
        return voiceMode != null && voiceMode.isEnabled();
    }
}


