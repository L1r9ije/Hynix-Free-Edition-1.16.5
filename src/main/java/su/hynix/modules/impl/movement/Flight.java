package su.hynix.modules.impl.movement;

import com.darkmagician6.eventapi.EventTarget;
import su.hynix.events.EventMotion;
import su.hynix.modules.Category;
import su.hynix.modules.Module;
import su.hynix.modules.api.constructors.impl.ModeSetting;
import su.hynix.modules.api.constructors.impl.SliderSetting;
import su.hynix.utils.player.MoveUtil;

public class Flight extends Module {
    private final ModeSetting modeSetting = new ModeSetting("Тип", "Скольжение", "Скольжение", "Прыжки", "Движение", "Обычный", "ReallyWorld Dragon");
    private final SliderSetting speed = new SliderSetting("Скорость", 1.5F, 0.1F, 10F, 0.1F, () -> !(modeSetting.is("Прыжки") || modeSetting.is("ReallyWorld Dragon")));
    private final SliderSetting speedY = new SliderSetting("Скорость по Y", 1.5F, 0.1F, 10F, 0.1F, () -> !(modeSetting.is("Прыжки") || modeSetting.is("ReallyWorld Dragon")));

    public Flight() {
        super("Flight", "Позволяет летать, облегчая перемещение", Category.Movement);
        addSettings(modeSetting, speed, speedY);
    }


    @EventTarget
    public void onEvent(EventMotion event) {
        boolean isSneaking = mc.gameSettings.keyBindSneak.isKeyDown();
        boolean isJumping = mc.gameSettings.keyBindJump.isKeyDown();
        float motionSpeed = speedY.get();
        switch (modeSetting.get()) {
            case "Скольжение":
                mc.player.setMotion(0, -0.005F, 0);
                if (isSneaking) {
                    mc.player.motion.y = -motionSpeed;
                } else if (isJumping) {
                    mc.player.motion.y = motionSpeed;
                }
                if (mc.player.isOnGround()) mc.player.jump();
                MoveUtil.setMotion(speed.get());
                break;

            case "Обычный":
                mc.player.setMotion(0, 0, 0);
                if (isSneaking) {
                    mc.player.motion.y = -motionSpeed;
                } else if (isJumping) {
                    mc.player.motion.y = motionSpeed;
                }
                if (mc.player.isOnGround()) mc.player.jump();
                MoveUtil.setMotion(speed.get());
                break;
            case "Движение":
                if (isSneaking) {
                    mc.player.motion.y = -motionSpeed;
                } else if (isJumping) {
                    mc.player.motion.y = motionSpeed;
                }
                MoveUtil.setMotion(speed.get());
                break;
            case "Прыжки":
                if (isJumping) mc.player.jump();
                break;
            case "ReallyWorld Dragon":
                if (mc.player.abilities.isFlying) {
                    mc.player.setMotion(0, 0, 0);
                    boolean moving = MoveUtil.isMoving();
                    boolean vertical = isSneaking || isJumping;
                    if (moving) {
                        MoveUtil.setMotion(vertical ? 0.7 : 1.09);
                    }
                    if (isSneaking && !moving) {
                        mc.player.motion.y = -1;
                    } else if (isJumping && !moving) {
                        mc.player.motion.y = 1;
                    }
                    if (isSneaking) {
                        mc.player.motion.y = -0.74;
                    } else if (isJumping) {
                        mc.player.motion.y = 0.74;
                    }
                }
                break;
            default:
                break;
        }
    }
}
