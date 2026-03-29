package su.hynix.modules.impl.movement;

import com.darkmagician6.eventapi.EventTarget;
import su.hynix.events.EventUpdate;
import su.hynix.modules.Category;
import su.hynix.modules.Module;
import su.hynix.utils.player.MoveUtil;

public class NoWeb extends Module {

    public NoWeb() {
        super("No Web", "Позволяет быстро перемещаться в паутине", Category.Movement);
    }

    @EventTarget
    public void onEvent(EventUpdate event) {
        if (mc.player.isInWeb) {

            mc.player.motion.y = 0.0;

            if (mc.gameSettings.keyBindJump.isKeyDown()) {
                mc.player.motion.y = 0.95;
            }

            if (mc.gameSettings.keyBindSneak.isKeyDown()) {
                mc.player.motion.y = -0.95;
            }

            MoveUtil.setMotion(0.22);
        }
    }
}