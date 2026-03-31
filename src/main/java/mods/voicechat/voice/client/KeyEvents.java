package mods.voicechat.voice.client;

import mods.voicechat.Voicechat;
import mods.voicechat.VoicechatClient;
import mods.voicechat.gui.VoiceChatScreen;
import mods.voicechat.gui.VoiceChatSettingsScreen;
import mods.voicechat.gui.group.GroupScreen;
import mods.voicechat.gui.group.JoinGroupScreen;
import mods.voicechat.gui.onboarding.OnboardingManager;
import mods.voicechat.gui.volume.AdjustVolumesScreen;
import mods.voicechat.intercompatibility.ClientCompatibilityManager;
import mods.voicechat.voice.common.ClientGroup;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.text.TranslationTextComponent;

import static net.minecraft.client.GameSettings.*;

public class KeyEvents {

    public static KeyBinding[] ALL_KEYS;
    private final Minecraft minecraft;

    public KeyEvents() {
        minecraft = Minecraft.getInstance();
        ClientCompatibilityManager.INSTANCE.onHandleKeyBinds(this::handleKeybinds);

        ALL_KEYS = new KeyBinding[]{
                KEY_PTT, KEY_WHISPER, KEY_MUTE, KEY_DISABLE, KEY_HIDE_ICONS, KEY_VOICE_CHAT, KEY_VOICE_CHAT_SETTINGS, KEY_GROUP, KEY_TOGGLE_RECORDING, KEY_ADJUST_VOLUMES
        };
    }

    private void handleKeybinds() {
        ClientPlayerEntity player = minecraft.player;
        if (player == null) {
            return;
        }
        if (!mods.voicechat.VoicechatGate.isEnabled()) {
            return;
        }
        if (OnboardingManager.isOnboarding()) {
            for (KeyBinding allKey : ALL_KEYS) {
                if (allKey.isPressed()) {
                    OnboardingManager.startOnboarding(null);
                    return;
                }
            }
            return;
        }

        ClientVoicechat client = ClientManager.getClient();
        ClientPlayerStateManager playerStateManager = ClientManager.getPlayerStateManager();
        if (KEY_VOICE_CHAT.isPressed()) {
            if (Screen.hasAltDown()) {
                if (Screen.hasControlDown()) {
                    VoicechatClient.CLIENT_CONFIG.onboardingFinished.set(false).save();
                    player.sendStatusMessage(new TranslationTextComponent("message.voicechat.onboarding.reset"), true);
                } else {
                    ClientManager.getDebugOverlay().toggle();
                }
            } else {
                minecraft.displayGuiScreen(new VoiceChatScreen());
            }
        }

        if (KEY_GROUP.isKeyDown()) {
            if (client != null && client.getConnection() != null && client.getConnection().getData().groupsEnabled()) {
                ClientGroup group = playerStateManager.getGroup();
                if (group != null) {
                    minecraft.displayGuiScreen(new GroupScreen(group));
                } else {
                    minecraft.displayGuiScreen(new JoinGroupScreen());
                }
            } else {
                player.sendStatusMessage(new TranslationTextComponent("message.voicechat.groups_disabled"), true);
            }
        }

        if (KEY_VOICE_CHAT_SETTINGS.isPressed()) {
            minecraft.displayGuiScreen(new VoiceChatSettingsScreen());
        }

        if (KEY_ADJUST_VOLUMES.isPressed()) {
            minecraft.displayGuiScreen(new AdjustVolumesScreen());
        }

        if (KEY_PTT.isPressed()) {
            checkConnected();
        }

        if (KEY_WHISPER.isPressed()) {
            checkConnected();
        }

        if (KEY_MUTE.isPressed()) {
            playerStateManager.setMuted(!playerStateManager.isMuted());
        }

        if (KEY_DISABLE.isPressed()) {
            playerStateManager.setDisabled(!playerStateManager.isDisabled());
        }

        if (KEY_TOGGLE_RECORDING.isPressed() && client != null) {
            ClientManager.getClient().toggleRecording();
        }

        if (KEY_HIDE_ICONS.isPressed()) {
            boolean hidden = !VoicechatClient.CLIENT_CONFIG.hideIcons.get();
            VoicechatClient.CLIENT_CONFIG.hideIcons.set(hidden).save();

            if (hidden) {
                player.sendStatusMessage(new TranslationTextComponent("message.voicechat.icons_hidden"), true);
            } else {
                player.sendStatusMessage(new TranslationTextComponent("message.voicechat.icons_visible"), true);
            }
        }
    }

    private boolean checkConnected() {
        if (ClientManager.getClient() == null || ClientManager.getClient().getConnection() == null || !ClientManager.getClient().getConnection().isInitialized()) {
            sendNotConnectedMessage();
            return false;
        }
        return true;
    }

    private void sendNotConnectedMessage() {
        ClientPlayerEntity player = minecraft.player;
        if (player == null) {
            Voicechat.LOGGER.warn("Voice chat not connected");
            return;
        }
        player.sendStatusMessage(new TranslationTextComponent("message.voicechat.voice_chat_not_connected"), true);
    }

}
