package su.hynix.events;

import com.darkmagician6.eventapi.events.Event;
import com.darkmagician6.eventapi.events.callables.EventCancellable;
import lombok.Data;

@Data
public class EventJump extends EventCancellable implements Event {
}
