package su.hynix.events;

import com.darkmagician6.eventapi.events.Event;
import com.darkmagician6.eventapi.events.callables.EventCancellable;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EventMotion extends EventCancellable implements Event {
    private double x, y, z;
    private float yaw, pitch;
    private boolean onGround;
    private boolean isSneaking;
    private boolean isSprinting;
}