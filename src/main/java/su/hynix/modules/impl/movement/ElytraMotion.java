package su.hynix.modules.impl.movement;

import com.darkmagician6.eventapi.EventTarget;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Items;
import su.hynix.events.EventTravel;
import su.hynix.events.EventUpdate;
import su.hynix.hynix;
import su.hynix.modules.Category;
import su.hynix.modules.Module;
import su.hynix.modules.api.constructors.impl.BooleanSetting;
import su.hynix.modules.api.constructors.impl.SliderSetting;
import su.hynix.modules.impl.combat.AttackAura;
import su.hynix.utils.math.TimeUtil;
import su.hynix.utils.player.InventoryUtil;


public class ElytraMotion extends Module {

    public final SliderSetting attackDistance = new SliderSetting("Дистанция работы", 3.0F, 0.1F, 5.0F, 0.1F);
    private final BooleanSetting autoFirework = new BooleanSetting("Автоматически использовать феерверк", false);
    private final TimeUtil timer = new TimeUtil();
    public boolean freeze;

    public ElytraMotion() {
        super("Elytra Motion", "Позволяет зависать в воздухе на элитре, упрощая таргетирование цели", Category.Movement);
        addSettings(attackDistance, autoFirework);
    }

    @EventTarget
    public void eventUpdate(EventUpdate e) {
        if (!mc.player.isElytraFlying()) {
            freeze = false;
            return;
        }
        AttackAura killAura = hynix.getInstance().getModuleManager().getAttackAura();
        ElytraTarget elytraTarget = hynix.getInstance().getModuleManager().getElytraTarget();

        if (check(killAura, elytraTarget)) {
            mc.gameSettings.keyBindForward.setPressed(false);
            freeze = true;
        } else {
            mc.gameSettings.keyBindForward.setPressed(true);
            freeze = false;
        }

        if (autoFirework.get()) {
            if (AttackAura.getTarget() != null && timer.hasTimeElapsed(500)) {
                InventoryUtil.inventorySwapClick(Items.FIREWORK_ROCKET);
                timer.reset();
            }
        }
    }

    @EventTarget
    public void eventMotion(EventTravel e) {
        if (e.getEntity() == mc.player && freeze) {
            e.setCancelled(true);
        }
    }

    public boolean check(AttackAura killAura, ElytraTarget elytraTarget) {
        LivingEntity target = AttackAura.getTarget();
        if (target == null) return false;
        boolean canTarget = shouldTarget(target, true);
        return !canTarget && target.getDistance(mc.player) < attackDistance.get();
    }

    public boolean shouldTarget(LivingEntity livingEntity, boolean checkResolve) {
        if (!isEnabled() || livingEntity == null) return false;
        return (checkResolve ? mc.player.isElytraFlying() && livingEntity.isElytraFlying() : mc.player.isElytraFlying());
    }

    @Override
    public void onDisable() {
        freeze = false;
        super.onDisable();
    }
}