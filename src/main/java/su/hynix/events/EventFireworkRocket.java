package su.hynix.events;

import com.darkmagician6.eventapi.events.Event;
import com.darkmagician6.eventapi.events.callables.EventCancellable;
import lombok.Data;
import net.minecraft.util.math.vector.Vector3d;

@Data
public class EventFireworkRocket extends EventCancellable implements Event {
    private float boostMultiplier;
    private float baseBoost;
    private float smoothingFactor;
    private float ySpeed;
    private Vector3d vector3d;

    public EventFireworkRocket(float boostMultiplier, float baseBoost, float smoothingFactor, Vector3d vec) {
        this.boostMultiplier = boostMultiplier;
        this.baseBoost = baseBoost;
        this.smoothingFactor = smoothingFactor;
        this.ySpeed = 1.0f;
        this.vector3d = vec;
    }
}