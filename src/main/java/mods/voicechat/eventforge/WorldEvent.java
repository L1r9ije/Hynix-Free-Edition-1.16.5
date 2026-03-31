package mods.voicechat.eventforge;

import com.darkmagician6.eventapi.events.Event;
import com.darkmagician6.eventapi.events.callables.EventCancellable;
import net.minecraft.world.IWorld;

public class WorldEvent extends EventCancellable implements Event {
    private final IWorld world;

    public WorldEvent(IWorld world) {
        this.world = world;
    }

    public IWorld getWorld() {
        return this.world;
    }

    public static class Load extends WorldEvent {
        public Load(IWorld world) {
            super(world);
        }
    }

    public static class Unload extends WorldEvent {
        public Unload(IWorld world) {
            super(world);
        }
    }
}