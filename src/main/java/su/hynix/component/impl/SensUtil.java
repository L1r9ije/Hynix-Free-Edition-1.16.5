package su.hynix.component.impl;

import lombok.experimental.UtilityClass;
import su.hynix.utils.Wrapper;
import su.hynix.utils.misc.ServerUtil;

@UtilityClass
public class SensUtil implements Wrapper {

    public float getSens(float rotation) {
        return getDeltaMouse(rotation) * getGCDValue();
    }

    public float getGCDValue() {
        double multiplier = ServerUtil.isConnectedToServer("reallyworld") ? 0.15D : 0.015D;
        return (float) (getGCD() * multiplier);
    }


    public static float getGCD() {
        double mouseSensitivity = mc.gameSettings.mouseSensitivity;
        return (float) (Math.pow(mouseSensitivity * 0.6F + 0.2F, 3.0D) * 8F);
    }

    public float getDeltaMouse(float delta) {
        return Math.round(delta / getGCDValue());
    }

}