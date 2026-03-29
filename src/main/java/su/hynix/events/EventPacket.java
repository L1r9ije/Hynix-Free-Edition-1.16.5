package su.hynix.events;

import com.darkmagician6.eventapi.events.Event;
import com.darkmagician6.eventapi.events.callables.EventCancellable;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.minecraft.network.IPacket;


@Data
@AllArgsConstructor
public class EventPacket extends EventCancellable implements Event {
    private final IPacket<?> packet;
    private final PacketType packetType;

    public boolean isReceive() {
        return this.packetType == PacketType.RECEIVE;
    }

    public boolean isSend() {
        return this.packetType == PacketType.SEND;
    }

    public enum PacketType {
        RECEIVE, SEND
    }
}