package su.hynix.component.impl;

import com.darkmagician6.eventapi.EventTarget;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.util.math.MathHelper;
import su.hynix.component.Component;
import su.hynix.component.Instance;
import su.hynix.events.EventUpdate;
import su.hynix.handlers.impl.LookHandler;
import su.hynix.handlers.impl.Rotation;


@Getter
@Setter
@Accessors(fluent = true)
public class RotationComponent extends Component {
    private RotationTask currentTask = RotationTask.IDLE;
    private float currentYawSpeed;
    private float currentPitchSpeed;
    private float currentYawReturnSpeed;
    private float currentPitchReturnSpeed;
    private int currentPriority;
    private int currentTimeout;
    private int idleTicks;
    private Rotation targetRotation;

    public static RotationComponent getInstance() {
        return Instance.getComponent(RotationComponent.class);
    }

    public static void update(Rotation rotation, float turnSpeed, int timeout, int priority) {
        update(rotation, turnSpeed, turnSpeed, timeout, priority);
    }

    public static void update(Rotation target, float yawSpeed, float pitchSpeed, float yawReturnSpeed, float pitchReturnSpeed, int timeout, int priority, boolean clientRotation) {
        final RotationComponent instance = RotationComponent.getInstance();
        if (instance.currentPriority() > priority) {
            return;
        }

        if (instance.currentTask().equals(RotationTask.IDLE) && !clientRotation) {
            LookHandler.setActive(true);
        }

        instance.currentYawSpeed(yawSpeed);
        instance.currentPitchSpeed(pitchSpeed);
        instance.currentYawReturnSpeed(yawReturnSpeed);
        instance.currentPitchReturnSpeed(pitchReturnSpeed);
        instance.currentTimeout(timeout);
        instance.currentPriority(priority);
        instance.currentTask(RotationTask.AIM);
        instance.targetRotation(target);

        instance.updateRotation(target, yawSpeed, pitchSpeed);
    }

    public static void update(Rotation targetRotation, float turnSpeed, float returnSpeed, int timeout, int priority) {
        update(targetRotation, turnSpeed, turnSpeed, returnSpeed, returnSpeed, timeout, priority, false);
    }

    private void resetRotation() {
        Rotation targetRotation = new Rotation(LookHandler.getFreeYaw(), LookHandler.getFreePitch());
        if (updateRotation(targetRotation, currentYawReturnSpeed(), currentPitchReturnSpeed())) {
            stopRotation();
        }
    }

    @EventTarget
    public void onEvent(EventUpdate event) {
        if (currentTask().equals(RotationTask.AIM) && idleTicks() > currentTimeout()) {
            currentTask(RotationTask.RESET);
        }

        if (currentTask().equals(RotationTask.RESET)) {
            resetRotation();
        }
        idleTicks++;
    }

    private boolean updateRotation(Rotation targetRotation, float yawSpeed, float pitchSpeed) {
        if (mc.player == null) return false;

        Rotation currentRotation = new Rotation(mc.player);
        float yawDelta = MathHelper.wrapDegrees(targetRotation.getYaw() - currentRotation.getYaw());
        float pitchDelta = targetRotation.getPitch() - currentRotation.getPitch();

        float clampedYaw = Math.min(Math.abs(yawDelta), yawSpeed);
        float clampedPitch = Math.min(Math.abs(pitchDelta), pitchSpeed);

        mc.player.rotationYaw += SensUtil.getSens(MathHelper.clamp(yawDelta, -clampedYaw, clampedYaw)) - SensUtil.getSens(MathHelper.clamp(yawDelta, -clampedYaw, clampedYaw)) % SensUtil.getGCDValue();
        mc.player.rotationPitch = MathHelper.clamp(mc.player.rotationPitch + SensUtil.getSens(MathHelper.clamp(pitchDelta, -clampedPitch, clampedPitch)), -90F, 90F) - MathHelper.clamp(mc.player.rotationPitch + SensUtil.getSens(MathHelper.clamp(pitchDelta, -clampedPitch, clampedPitch)), -90F, 90F) % SensUtil.getGCDValue();

        idleTicks(0);
        return new Rotation(mc.player).getDelta(targetRotation) < 1F;
    }

    public void stopRotation() {
        currentTask(RotationTask.IDLE);
        currentPriority(0);
        if (!SmoothRotationComponent.getInstance().isRotating()) {
            LookHandler.setActive(false);
        }
    }

    public boolean isRotating() {
        return !currentTask.equals(RotationTask.IDLE);
    }

    public enum RotationTask {
        AIM,
        RESET,
        IDLE
    }
}
