package su.hynix.events;

import com.darkmagician6.eventapi.events.Event;
import com.darkmagician6.eventapi.events.callables.EventCancellable;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.minecraft.entity.Entity;

@Data
@AllArgsConstructor
public class EventInteract extends EventCancellable implements Event {
    private Entity entity;
}