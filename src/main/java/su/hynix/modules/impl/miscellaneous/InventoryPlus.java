package su.hynix.modules.impl.miscellaneous;

import com.darkmagician6.eventapi.EventTarget;
import net.minecraft.network.play.client.CCloseWindowPacket;
import su.hynix.events.EventPacket;
import su.hynix.modules.Category;
import su.hynix.modules.Module;

public class InventoryPlus extends Module {

    public InventoryPlus() {
        super("Xcarry", "Добовляет дополнительные 4 слота в инвентаре, увеличивая его вместимость", Category.Miscellaneous);
    }

    @EventTarget
    public void onEvent(EventPacket event) {
        if (mc.player == null) return;

        if (event.getPacket() instanceof CCloseWindowPacket) {
            event.setCancelled(true);
        }
    }
}
