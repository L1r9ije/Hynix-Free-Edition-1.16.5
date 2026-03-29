package su.hynix.events;

import com.darkmagician6.eventapi.events.Event;
import com.darkmagician6.eventapi.events.callables.EventCancellable;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.minecraft.util.Hand;

@Data
@AllArgsConstructor
public class EventSwingSpeed extends EventCancellable implements Event {
    private int swipeSpeed;
    private Hand hand;
}
