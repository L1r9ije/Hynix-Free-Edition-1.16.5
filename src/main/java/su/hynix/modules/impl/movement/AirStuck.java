package su.hynix.modules.impl.movement;

import com.darkmagician6.eventapi.EventTarget;
import net.minecraft.network.play.client.CPlayerPacket;
import su.hynix.events.EventMotion;
import su.hynix.events.EventPacket;
import su.hynix.events.EventSwapWorld;
import su.hynix.events.EventTravel;
import su.hynix.modules.Category;
import su.hynix.modules.Module;

public class AirStuck extends Module {

    public AirStuck() {
        super("Air Stuck", "Позволяет зависнуть в воздухе", Category.Movement);
    }

    @EventTarget
    public void onPacket(EventPacket e) {
        if (mc.player == null || mc.player.connection == null) return;
        if (e.getPacket() instanceof CPlayerPacket) {
            e.setCancelled(true);
        }
    }

    @EventTarget
    public void onEvent(EventSwapWorld eventSwapWorld) {
        toggle();
    }

    @EventTarget
    public void onEvent(EventMotion eventMotion) {
        eventMotion.setY(0.0);
        eventMotion.setCancelled(true);
    }

    @EventTarget
    public void onTravel(EventTravel e) {
        if (e.getEntity() == mc.player) {
            e.setCancelled(true);
        }
    }
}
