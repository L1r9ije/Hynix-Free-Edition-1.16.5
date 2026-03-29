package su.hynix.modules.impl.combat;

import com.darkmagician6.eventapi.EventTarget;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.util.math.vector.Vector2f;
import su.hynix.events.EventNoRotate;
import su.hynix.events.EventPacket;
import su.hynix.modules.Category;
import su.hynix.modules.Module;

public class NoServerDesync extends Module {

    private boolean isPacketSent;
    private Vector2f rotate;
    public NoServerDesync() {
        super("NoServerDesync", "Не позволяет серверу десинкнуть вас", Category.Combat);
    }

    @EventTarget
    public void onPacket(EventPacket event) {
        if (isPacketSent) {
            if (event.getPacket() instanceof CPlayerPacket packet) {
                packet.setYaw(rotate.x);
                packet.setPitch(rotate.y);
                isPacketSent = false;
            }
        }
    }

    @EventTarget
    public void onEvent(EventNoRotate eventNoRotate) {
        rotate = new Vector2f(eventNoRotate.getYaw(), eventNoRotate.getPitch());
        isPacketSent = true;
        eventNoRotate.setCancelled(true);
    }
}
