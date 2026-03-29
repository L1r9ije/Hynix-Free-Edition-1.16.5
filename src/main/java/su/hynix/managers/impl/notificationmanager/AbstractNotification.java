package su.hynix.managers.impl.notificationmanager;

import com.mojang.blaze3d.matrix.MatrixStack;
import lombok.Getter;
import net.minecraft.util.text.ITextComponent;
import su.hynix.utils.animation.AnimationUtil;
import su.hynix.utils.math.TimeUtil;

@Getter
public abstract class AbstractNotification {
    protected final ITextComponent message;
    protected final long duration = 1500;
    protected final TimeUtil timer = new TimeUtil();
    protected final AnimationUtil yAnimation = new AnimationUtil(0.0f, 20);
    protected final AnimationUtil alphaAnimation = new AnimationUtil(0.0f, 10);
    protected boolean forceExpire = false;
    protected boolean initializedY = false;

    public AbstractNotification(ITextComponent message) {
        this.message = message;
    }

    public abstract void render(float x, float y, MatrixStack matrixStack);

    public boolean isExpired() {
        return forceExpire || timer.hasTimeElapsed(duration);
    }

    public void expireNow() {
        this.forceExpire = true;
    }

    protected void initYIfNeeded(float y) {
        if (!initializedY) {
            yAnimation.setValue(y);
            initializedY = true;
        }
    }
}