package su.hynix.component.impl;

import com.darkmagician6.eventapi.EventTarget;
import lombok.Getter;
import lombok.Setter;
import su.hynix.component.Component;
import su.hynix.events.EventInput;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class MoveComponent extends Component {

    @Getter
    @Setter
    public static int stopTicks = 0;

    @Getter
    @Setter
    public static boolean stop = false;

    static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    @EventTarget
    public void onEvent(EventInput event) {
        if (stopTicks > 0) {
            if (stop) {
                event.setStrafe(0);
                event.setForward(0);
            }
            mc.gameSettings.keyBindSprint.setPressed(false);
            mc.player.setSprinting(false);
            stopTicks--;
        } else if (stop) {
            stop = false;
        }
    }


}
