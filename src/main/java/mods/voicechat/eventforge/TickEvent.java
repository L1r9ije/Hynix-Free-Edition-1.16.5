package mods.voicechat.eventforge;

import com.darkmagician6.eventapi.events.Event;
import com.darkmagician6.eventapi.events.callables.EventCancellable;

public class TickEvent extends EventCancellable implements Event {
    public final Type type;
    public final Phase phase;

    public TickEvent(Type type, Phase phase) {
        this.type = type;
        this.phase = phase;
    }

    public enum Phase {
        START,
        END
    }

    public enum Type {
        WORLD,
        PLAYER,
        CLIENT,
        SERVER,
        RENDER
    }

    public static class ServerTickEvent extends TickEvent {
        public ServerTickEvent(TickEvent.Phase phase) {
            super(Type.SERVER, phase);
        }
    }

    public static class ClientTickEvent extends TickEvent {
        public ClientTickEvent(TickEvent.Phase phase) {
            super(Type.CLIENT, phase);
        }
    }
}
