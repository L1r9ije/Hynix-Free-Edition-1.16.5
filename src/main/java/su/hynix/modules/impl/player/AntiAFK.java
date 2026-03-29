package su.hynix.modules.impl.player;

import com.darkmagician6.eventapi.EventTarget;
import net.minecraft.util.Hand;
import org.apache.commons.lang3.RandomStringUtils;
import su.hynix.events.EventUpdate;
import su.hynix.modules.Category;
import su.hynix.modules.Module;
import su.hynix.modules.api.constructors.impl.BooleanSetting;
import su.hynix.modules.api.constructors.impl.MultiBooleanSetting;
import su.hynix.utils.math.TimeUtil;
import su.hynix.utils.player.MoveUtil;


public class AntiAFK extends Module {
    private final MultiBooleanSetting actions = new MultiBooleanSetting("Действия", new BooleanSetting("Прыжок", false), new BooleanSetting("Команда", true), new BooleanSetting("Качание рукой", false));

    private final TimeUtil timer = new TimeUtil();

    public AntiAFK() {
        super("AntiAFK", "Предотвращает кик сервером за AFK", Category.Player);
        addSettings(actions);
    }

    @Override
    public void onDisable() {
        timer.reset();
        super.onDisable();
    }


    @EventTarget
    public void onUpdate(EventUpdate event) {
        if (mc.player == null || mc.world == null) return;

        if (MoveUtil.isMoving()) {
            timer.reset();
            return;
        }

        if (timer.hasReached(20000)) {
            if (actions.is("Прыжок") && mc.player.isOnGround()) {
                mc.player.jump();
            }
            if (actions.is("Команда")) {
                mc.player.sendChatMessage("/" + RandomStringUtils.randomAlphabetic(5));
            }
            if (actions.is("Качание рукой")) {
                mc.player.swingArm(Hand.MAIN_HAND);
            }
            timer.reset();
        }
    }
}