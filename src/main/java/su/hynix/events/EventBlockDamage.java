package su.hynix.events;

import com.darkmagician6.eventapi.events.Event;
import com.darkmagician6.eventapi.events.callables.EventCancellable;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

@Data
@AllArgsConstructor
public class EventBlockDamage extends EventCancellable implements Event {
    private BlockState blockState;
    private BlockPos pos;
    private State state;

    public enum State {
        START,
        STOP
    }
}
