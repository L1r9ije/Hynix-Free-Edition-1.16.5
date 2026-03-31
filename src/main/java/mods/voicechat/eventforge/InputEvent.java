package mods.voicechat.eventforge;

import com.darkmagician6.eventapi.events.Event;
import com.darkmagician6.eventapi.events.callables.EventCancellable;

public class InputEvent extends EventCancellable implements Event {


    public static class KeyInputEvent
            extends InputEvent {
        private final int key;
        private final int scanCode;
        private final int action;
        private final int modifiers;

        public KeyInputEvent(int key, int scanCode, int action, int modifiers) {
            this.key = key;
            this.scanCode = scanCode;
            this.action = action;
            this.modifiers = modifiers;
        }

        public int getKey() {
            return this.key;
        }

        public int getScanCode() {
            return this.scanCode;
        }

        public int getAction() {
            return this.action;
        }

        public int getModifiers() {
            return this.modifiers;
        }
    }

    public static class RawMouseEvent
            extends InputEvent {
        private final int button;
        private final int action;
        private final int mods;

        public RawMouseEvent(int button, int action, int mods) {
            this.button = button;
            this.action = action;
            this.mods = mods;
        }

        public int getButton() {
            return this.button;
        }

        public int getAction() {
            return this.action;
        }

        public int getMods() {
            return this.mods;
        }
    }
}
