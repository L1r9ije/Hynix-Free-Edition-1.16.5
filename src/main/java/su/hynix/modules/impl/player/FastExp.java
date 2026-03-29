package su.hynix.modules.impl.player;

import com.darkmagician6.eventapi.EventTarget;
import net.minecraft.item.Items;
import su.hynix.events.EventUpdate;
import su.hynix.modules.Category;
import su.hynix.modules.Module;

public class FastExp extends Module {

    public FastExp() {
        super("FastExp", "Позволяет быстро бросать пузырьки опыта", Category.Player);
    }

    @EventTarget
    public void onUpdate(EventUpdate event) {
        if (mc.player.getHeldItemMainhand().getItem() == Items.EXPERIENCE_BOTTLE) {
            mc.rightClickDelayTimer = 0;
        }
    }
}
