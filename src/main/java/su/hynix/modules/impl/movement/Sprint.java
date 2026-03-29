package su.hynix.modules.impl.movement;

import com.darkmagician6.eventapi.EventTarget;
import lombok.Getter;
import net.minecraft.block.Blocks;
import net.minecraft.potion.Effects;
import su.hynix.events.EventUpdate;
import su.hynix.hynix;
import su.hynix.modules.Category;
import su.hynix.modules.Module;
import su.hynix.modules.api.constructors.impl.ModeSetting;
import su.hynix.modules.impl.combat.AttackAura;
import su.hynix.utils.player.MoveUtil;


public class Sprint extends Module {


    @Getter
    private final ModeSetting mode = new ModeSetting("Мод", "Обычный", "Обычный", "Пакетный");


    public Sprint() {
        super("Sprint", "Автоматически активирует режим бега, когда игрок начинает двигаться", Category.Movement);
        this.addSettings(mode);

    }
    /*@Subscribe
    public void onUpdate(EventUpdate eventUpdate) {
        if (Xrushka.getInst().getModuleManager().getAura().isState() && Aura.getTarget() != null && mc.player.getDistanceEyePos(Aura.getTarget()) < 0.5f && mc.player.isOnGround() && !mc.player.isElytraFlying() && !mc.player.isInWater() && !mc.player.isSwimming()) {
            sprint = false;
        } else {
            sprint = true;
        }
        if (!sprint) {
            mc.player.setSprinting(false);
        }
    } */

    @EventTarget
    public void onUpdateganodn(EventUpdate eventUpdate) {
        if (mode.is("Обычный")) {
            AttackAura aura = hynix.getInstance().getModuleManager().attackAura;


            boolean reset = aura.isEnabled() && aura.getOnlycrit().get();

            if (reset) {
                if (sprint()) {
                    mc.gameSettings.keyBindSprint.setPressed(true);
                } else {
                    mc.gameSettings.keyBindSprint.setPressed(false);
                    mc.player.setSprinting(false);
                }
            } else {
                mc.gameSettings.keyBindSprint.setPressed(true);
            }
        }


        if (mode.is("Пакетный")) {
            if (RAGE()) {
                mc.player.setSprinting(MoveUtil.isMoving());
            }
        }
    }


    public boolean sprint() {
        return canSprint();
    }

    public boolean RAGE() {
        return !mc.player.isSneaking() && !mc.player.collidedHorizontally && mc.player.movementInput.moveForward > 0.0F && !mc.player.isCrouching() && !mc.player.isPotionActive(Effects.SLOWNESS) && !mc.player.isPotionActive(Effects.BLINDNESS) && !mc.player.isVisuallySwimming() && !mc.player.isHandActive();
    }

    public boolean canSprint() {
        boolean flag = mc.player.getBlockState().isIn(Blocks.COBWEB) || mc.player.abilities.isFlying || MoveUtil.isInLiquid() || mc.player.isRidingHorse() || (mc.player.fallDistance <= 0.0F && mc.player.isOnGround());
        return flag;
    }

    @Override
    public void onDisable() {
        mc.gameSettings.keyBindSprint.setPressed(false);
    }
}
