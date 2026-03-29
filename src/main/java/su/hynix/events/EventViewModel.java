package su.hynix.events;

import com.darkmagician6.eventapi.events.Event;
import com.darkmagician6.eventapi.events.callables.EventCancellable;
import com.mojang.blaze3d.matrix.MatrixStack;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.minecraft.util.HandSide;

@Data
@AllArgsConstructor
public class EventViewModel extends EventCancellable implements Event {
    private MatrixStack matrixStack;
    private HandSide handside;
}
