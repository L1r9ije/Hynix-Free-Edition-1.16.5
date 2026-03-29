package su.hynix.modules.impl.movement;


import com.darkmagician6.eventapi.EventTarget;
import su.hynix.events.EventUpdate;
import su.hynix.modules.Category;
import su.hynix.modules.Module;

public class NoJumpDelay extends Module {

    public NoJumpDelay() {
        super("NoJumpDelay", "Убирает задержку между прыжками, позволяя прыгать быстрее", Category.Movement);
    }

    @EventTarget
    public void onUpdate(EventUpdate e) {
        mc.player.jumpTicks = 0;
    }
}