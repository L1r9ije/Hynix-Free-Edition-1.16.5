package su.hynix.events;

import com.darkmagician6.eventapi.events.Event;
import com.darkmagician6.eventapi.events.callables.EventCancellable;
import lombok.Data;
import net.minecraft.entity.Entity;


@Data
public class EventEntityRayTrace extends EventCancellable implements Event {
    private final Entity entity;
}