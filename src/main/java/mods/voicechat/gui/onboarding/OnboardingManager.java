package mods.voicechat.gui.onboarding;

import mods.voicechat.VoicechatClient;
import mods.voicechat.voice.client.ChatUtils;
import mods.voicechat.voice.client.ClientManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

import javax.annotation.Nullable;

import static net.minecraft.client.GameSettings.KEY_VOICE_CHAT;

public class OnboardingManager {

    private static final Minecraft MC = Minecraft.getInstance();

    public static boolean isOnboarding() {
        return !VoicechatClient.CLIENT_CONFIG.onboardingFinished.get();
    }

    public static void startOnboarding(@Nullable Screen parent) {
        MC.displayGuiScreen(getOnboardingScreen(parent));
    }

    public static Screen getOnboardingScreen(@Nullable Screen parent) {
        return new IntroductionOnboardingScreen(parent);
    }

    public static void finishOnboarding() {
        VoicechatClient.CLIENT_CONFIG.muted.set(true).save();
        VoicechatClient.CLIENT_CONFIG.disabled.set(false).save();
        VoicechatClient.CLIENT_CONFIG.onboardingFinished.set(true).save();
        ClientManager.getPlayerStateManager().onFinishOnboarding();
        MC.displayGuiScreen(null);
    }

    public static void onConnecting() {
        if (!isOnboarding()) {
            return;
        }
        ChatUtils.sendModMessage(new TranslationTextComponent("message.voicechat.set_up",
                KEY_VOICE_CHAT.getTranslatedKeyMessage().deepCopy().mergeStyle(TextFormatting.BOLD, TextFormatting.UNDERLINE)
        ));
    }
}
