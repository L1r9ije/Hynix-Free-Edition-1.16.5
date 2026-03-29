package su.hynix.modules.impl.combat;


import com.darkmagician6.eventapi.EventTarget;
import net.minecraft.network.play.server.SEntityVelocityPacket;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import su.hynix.events.EventDamage;
import su.hynix.events.EventInput;
import su.hynix.events.EventPacket;
import su.hynix.hynix;
import su.hynix.modules.Category;
import su.hynix.modules.Module;
import su.hynix.modules.api.constructors.impl.BooleanSetting;
import su.hynix.modules.api.constructors.impl.ModeSetting;
import su.hynix.modules.impl.movement.FreeCamera;
import su.hynix.utils.player.DamageUtil;


public class NoVelocity extends Module {

    private final ModeSetting modeSetting = new ModeSetting("Мод", "Обычный", "Обычный", "Легит");
    private final BooleanSetting jump = new BooleanSetting("Прыгать", true, () -> modeSetting.is("Легит"));
    private final DamageUtil damageUtil = new DamageUtil();
    private Vector3d knockbackVelocity = Vector3d.ZERO;

    public NoVelocity() {
        super("Velocity", "Не позволяет разным условиям в мире откидывать вас", Category.Combat);
        addSettings(modeSetting, jump);
    }

    @EventTarget
    public void onPacket(EventPacket e) {
        if (modeSetting.is("Обычный")) {
            if (mc.player != null && e.getPacket() instanceof SEntityVelocityPacket p) {
                if (p.getEntityID() == mc.player.getEntityId()) {
                    e.setCancelled(true);
                }
            }
        }

        if (modeSetting.is("Легит")) {
            damageUtil.time(700L);
            if (mc.player != null && damageUtil.isNormalDamage() && e.getPacket() instanceof SEntityVelocityPacket p) {
                if (p.getEntityID() == mc.player.getEntityId()) {
                    knockbackVelocity = new Vector3d(p.getMotionX(), p.getMotionY(), p.getMotionZ());
                }
            }
            damageUtil.onPacketEvent(e);
        }

    }

    @EventTarget
    private void onDamage(EventDamage e) {
        if (modeSetting.is("Легит")) {
            damageUtil.processDamage(e);
        }
    }

    @EventTarget
    public void onInput(EventInput e) {
        if (!modeSetting.is("Легит")) return;
        if (hynix.getInstance().getModuleManager().getModule(FreeCamera.class).isEnabled()) {
            knockbackVelocity = Vector3d.ZERO;
            return;
        }
        if (mc.player.hurtTime > 0 && knockbackVelocity.lengthSquared() > 0.0F) {
            double relativeAngle = getRelativeAngle();
            float forward = 0.0F;
            float strafe = 0.0F;
            if (relativeAngle > -45.0F && relativeAngle < 45.0F) {
                forward = 1.0F;
            } else if (!(relativeAngle > 135.0F) && !(relativeAngle < -135.0F)) {
                if (relativeAngle >= 45.0F && relativeAngle <= 135.0F) {
                    strafe = -1.0F;
                } else if (relativeAngle <= -45.0F && relativeAngle >= -135.0F) {
                    strafe = 1.0F;
                }
            } else {
                forward = -1.0F;
            }

            e.setForward(forward);
            e.setStrafe(strafe);
            e.setJump(jump.get() && mc.player.isOnGround());
        } else {
            knockbackVelocity = Vector3d.ZERO;
        }
    }

    private double getRelativeAngle() {
        double yaw = mc.player.rotationYaw;
        double dx = -knockbackVelocity.x;
        double dz = -knockbackVelocity.z;
        double attackerYaw = Math.toDegrees(Math.atan2(dz, dx)) - 90.0F;
        return MathHelper.wrapDegrees(attackerYaw - yaw);
    }
}
