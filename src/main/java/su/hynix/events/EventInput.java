package su.hynix.events;

import com.darkmagician6.eventapi.events.Event;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EventInput implements Event {
    private float forward, strafe;
    private boolean forwardKeyDown, backKeyDown, leftKeyDown, rightKeyDown;
    private boolean jump, sneak;
    private double sneakSlowDownMultiplier;
}