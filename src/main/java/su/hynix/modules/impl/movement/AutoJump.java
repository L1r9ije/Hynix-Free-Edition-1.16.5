package su.hynix.modules.impl.movement;

import com.darkmagician6.eventapi.EventTarget;
import net.minecraft.potion.Effects;
import su.hynix.events.EventUpdate;
import su.hynix.hynix;
import su.hynix.modules.Category;
import su.hynix.modules.Module;
import su.hynix.modules.api.constructors.impl.BooleanSetting;
import su.hynix.modules.api.constructors.impl.MultiBooleanSetting;
import su.hynix.modules.impl.combat.AttackAura;


public class AutoJump extends Module {
    public static MultiBooleanSetting jump = new MultiBooleanSetting("Прыгать если", new BooleanSetting("Активна Attack Aura", false), new BooleanSetting("Активно зелье замедления", true));

    public AutoJump() {
        super("Auto Jump", "Автоматически прыгает", Category.Movement);
        addSettings(jump);
    }


    @EventTarget
    public void onUpdate(EventUpdate event) {
        if (!mc.player.isOnGround()) {
            return;
        }

        boolean shouldJump = false;

        if (jump.is("Активна Attack Aura")) {
            AttackAura aura = hynix.getInstance().getModuleManager().attackAura;
            if (aura.isEnabled() && AttackAura.getTarget() != null) {
                shouldJump = true;
            }
        }

        if (jump.is("Активно зелье замедления")) {
            if (mc.player.isPotionActive(Effects.SLOWNESS)) {
                shouldJump = true;
            }
        }

        if (shouldJump) {
            mc.player.jump();
        }
    }
}
