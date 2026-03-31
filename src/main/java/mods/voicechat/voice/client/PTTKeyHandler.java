package mods.voicechat.voice.client;

import mods.voicechat.intercompatibility.ClientCompatibilityManager;
import net.minecraft.client.util.InputMappings;

import static net.minecraft.client.GameSettings.KEY_PTT;
import static net.minecraft.client.GameSettings.KEY_WHISPER;

public class PTTKeyHandler {

    private boolean pttKeyDown;
    private boolean whisperKeyDown;

    public PTTKeyHandler() {
        ClientCompatibilityManager.INSTANCE.onKeyboardEvent(this::onKeyboardEvent);
        ClientCompatibilityManager.INSTANCE.onMouseEvent(this::onMouseEvent);
    }

    public void onKeyboardEvent(long window, int key, int scancode) {
        InputMappings.Input pttKey = ClientCompatibilityManager.INSTANCE.getBoundKeyOf(KEY_PTT);
        if (pttKey.getKeyCode() != -1 && !pttKey.getType().equals(InputMappings.Type.MOUSE)) {
            pttKeyDown = InputMappings.isKeyDown(window, pttKey.getKeyCode());
        }

        InputMappings.Input whisperKey = ClientCompatibilityManager.INSTANCE.getBoundKeyOf(KEY_WHISPER);
        if (whisperKey.getKeyCode() != -1 && !whisperKey.getType().equals(InputMappings.Type.MOUSE)) {
            whisperKeyDown = InputMappings.isKeyDown(window, whisperKey.getKeyCode());
        }
    }

    public void onMouseEvent(long window, int button, int action, int mods) {
        InputMappings.Input pttKey = ClientCompatibilityManager.INSTANCE.getBoundKeyOf(KEY_PTT);
        if (pttKey.getKeyCode() != -1 && pttKey.getType().equals(InputMappings.Type.MOUSE) && pttKey.getKeyCode() == button) {
            pttKeyDown = action != 0;
        }

        InputMappings.Input whisperKey = ClientCompatibilityManager.INSTANCE.getBoundKeyOf(KEY_WHISPER);
        if (whisperKey.getKeyCode() != -1 && whisperKey.getType().equals(InputMappings.Type.MOUSE) && whisperKey.getKeyCode() == button) {
            whisperKeyDown = action != 0;
        }
    }

    public boolean isPTTDown() {
        return pttKeyDown;
    }

    public boolean isWhisperDown() {
        return whisperKeyDown;
    }

    public boolean isAnyDown() {
        return pttKeyDown || whisperKeyDown;
    }

}
