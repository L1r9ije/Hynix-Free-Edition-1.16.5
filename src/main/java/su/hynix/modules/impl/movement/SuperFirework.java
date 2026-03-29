package su.hynix.modules.impl.movement;

import com.darkmagician6.eventapi.EventTarget;
import su.hynix.events.EventFireworkRocket;
import su.hynix.modules.Category;
import su.hynix.modules.Module;
import su.hynix.modules.api.constructors.impl.ClickSetting;
import su.hynix.modules.api.constructors.impl.ModeSetting;
import su.hynix.modules.api.constructors.impl.SliderSetting;
import su.hynix.modules.api.constructors.impl.TextSetting;


public class SuperFirework extends Module {
    private final ModeSetting mode = new ModeSetting("Режим", "Обычный", "Кастомный", "Обычный");
    private final TextSetting speedxz = new TextSetting("Скорость по XZ", () -> mode.is("Кастомный"));
    private final TextSetting speedy = new TextSetting("Скорость по Y", () -> mode.is("Кастомный"));
    private final SliderSetting xz40_45 = new SliderSetting("Скорость 40-45", 1.6f, 1.5f, 3.0f, 0.01f, () -> mode.is("Кастомный"));
    private final SliderSetting xz35_40 = new SliderSetting("Скорость 35-40", 1.6f, 1.5f, 3.0f, 0.01f, () -> mode.is("Кастомный"));
    private final SliderSetting xz30_35 = new SliderSetting("Скорость 30-35", 1.6f, 1.5f, 3.0f, 0.01f, () -> mode.is("Кастомный"));
    private final SliderSetting xz25_30 = new SliderSetting("Скорость 25-30", 1.6f, 1.5f, 3.0f, 0.01f, () -> mode.is("Кастомный"));
    private final SliderSetting xz20_25 = new SliderSetting("Скорость 20-25", 1.6f, 1.5f, 3.0f, 0.01f, () -> mode.is("Кастомный"));
    private final SliderSetting xz15_20 = new SliderSetting("Скорость 15-20", 1.6f, 1.5f, 3.0f, 0.01f, () -> mode.is("Кастомный"));
    private final SliderSetting xz10_15 = new SliderSetting("Скорость 10-15", 1.6f, 1.5f, 3.0f, 0.01f, () -> mode.is("Кастомный"));
    private final SliderSetting xz5_10 = new SliderSetting("Скорость 5-10", 1.6f, 1.5f, 3.0f, 0.01f, () -> mode.is("Кастомный"));
    private final SliderSetting xz0_5 = new SliderSetting("Скорость 0-5", 1.6f, 1.5f, 3.0f, 0.01f, () -> mode.is("Кастомный"));
    private final SliderSetting y40_45 = new SliderSetting("Скорость 40-45", 1.6f, 1.5f, 3.0f, 0.01f, () -> mode.is("Кастомный"));
    private final SliderSetting y35_40 = new SliderSetting("Скорость 35-40", 1.6f, 1.5f, 3.0f, 0.01f, () -> mode.is("Кастомный"));
    private final SliderSetting y30_35 = new SliderSetting("Скорость 30-35", 1.6f, 1.5f, 3.0f, 0.01f, () -> mode.is("Кастомный"));
    private final SliderSetting y25_30 = new SliderSetting("Скорость 25-30", 1.6f, 1.5f, 3.0f, 0.01f, () -> mode.is("Кастомный"));
    private final SliderSetting y20_25 = new SliderSetting("Скорость 20-25", 1.6f, 1.5f, 3.0f, 0.01f, () -> mode.is("Кастомный"));
    private final SliderSetting y15_20 = new SliderSetting("Скорость 15-20", 1.6f, 1.5f, 3.0f, 0.01f, () -> mode.is("Кастомный"));
    private final SliderSetting y10_15 = new SliderSetting("Скорость 10-15", 1.6f, 1.5f, 3.0f, 0.01f, () -> mode.is("Кастомный"));
    private final SliderSetting y5_10 = new SliderSetting("Скорость 5-10", 1.6f, 1.5f, 3.0f, 0.01f, () -> mode.is("Кастомный"));
    private final SliderSetting y0_5 = new SliderSetting("Скорость 0-5", 1.6f, 1.5f, 3.0f, 0.01f, () -> mode.is("Кастомный"));
    private final ClickSetting bravohvh = new ClickSetting("BravoHvH", () -> {
        xz0_5.set(1.64f);
        xz5_10.set(1.65f);
        xz10_15.set(1.66f);
        xz15_20.set(1.67f);
        xz20_25.set(1.68f);
        xz25_30.set(1.69f);
        xz30_35.set(1.93f);
        xz35_40.set(1.96f);
        xz40_45.set(1.96f);


        y0_5.set(1.55f);
        y5_10.set(1.56f);
        y10_15.set(1.57f);
        y15_20.set(1.61f);
        y20_25.set(1.62f);
        y25_30.set(1.63f);
        y30_35.set(1.93f);
        y35_40.set(1.96f);
        y40_45.set(1.96f);
    }, () -> mode.is("Кастомный"));
    private final ClickSetting rw = new ClickSetting("ReallyWorld", () -> {
        xz0_5.set(1.5f);
        xz5_10.set(1.5f);
        xz10_15.set(1.5f);
        xz15_20.set(1.5f);
        xz20_25.set(1.5f);
        xz25_30.set(1.5f);
        xz30_35.set(1.5f);
        xz35_40.set(1.5f);
        xz40_45.set(1.5f);


        y0_5.set(1.5f);
        y5_10.set(1.5f);
        y10_15.set(1.5f);
        y15_20.set(1.5f);
        y20_25.set(1.5f);
        y25_30.set(1.5f);
        y30_35.set(1.5f);
        y35_40.set(1.5f);
        y40_45.set(1.5f);
    }, () -> mode.is("Кастомный"));
    private final TextSetting presets = new TextSetting("Пресеты", () -> mode.is("Кастомный"));
    private final SliderSetting xzSpeed = new SliderSetting("Скорость XZ", 1.6f, 1.5f, 3.0f, 0.01f, () -> mode.is("Обычный"));
    private final SliderSetting ySpeed = new SliderSetting("Скорость Y", 1.6f, 1.5f, 3.0f, 0.01f, () -> mode.is("Обычный"));

    public SuperFirework() {
        super("Super Firework", "Увеличивает скорость и дальность полета при использовании фейерверков на элитре", Category.Movement);
        addSettings(mode, speedxz, xz0_5, xz5_10, xz10_15, xz15_20, xz20_25, xz25_30, xz30_35, xz35_40, xz40_45, speedy, y0_5, y5_10, y10_15, y15_20, y20_25, y25_30, y30_35, y35_40, y40_45, presets, bravohvh, rw, xzSpeed, ySpeed);
    }


    @EventTarget
    public void onFireworkRocket(EventFireworkRocket event) {
        float xzSpeedValue;
        float ySpeedValue;
        if (mode.is("Кастомный")) {
            float normalizedPitch = normalizeAngle(mc.player.rotationPitch);
            float absPitch = Math.abs(normalizedPitch);
            if (absPitch > 45.0f) {
                absPitch = 90.0f - absPitch;
            }
            float normalizedYaw = normalizeAngle(mc.player.rotationYaw);
            float absYaw = Math.abs(normalizedYaw);
            if (absYaw > 45.0f) {
                absYaw = 90.0f - absYaw;
            }
            if (absYaw <= 5f) {
                xzSpeedValue = xz0_5.get();
            } else if (absYaw <= 10f) {
                xzSpeedValue = xz5_10.get();
            } else if (absYaw <= 15f) {
                xzSpeedValue = xz10_15.get();
            } else if (absYaw <= 20f) {
                xzSpeedValue = xz15_20.get();
            } else if (absYaw <= 25f) {
                xzSpeedValue = xz20_25.get();
            } else if (absYaw <= 30f) {
                xzSpeedValue = xz25_30.get();
            } else if (absYaw <= 35f) {
                xzSpeedValue = xz30_35.get();
            } else if (absYaw <= 40f) {
                xzSpeedValue = xz35_40.get();
            } else {
                xzSpeedValue = xz40_45.get();
            }
            if (absPitch <= 5f) {
                ySpeedValue = y0_5.get();
            } else if (absPitch <= 10f) {
                ySpeedValue = y5_10.get();
            } else if (absPitch <= 15f) {
                ySpeedValue = y10_15.get();
            } else if (absPitch <= 20f) {
                ySpeedValue = y15_20.get();
            } else if (absPitch <= 25f) {
                ySpeedValue = y20_25.get();
            } else if (absPitch <= 30f) {
                ySpeedValue = y25_30.get();
            } else if (absPitch <= 35f) {
                ySpeedValue = y30_35.get();
            } else if (absPitch <= 40f) {
                ySpeedValue = y35_40.get();
            } else {
                ySpeedValue = y40_45.get();
            }
        } else {
            xzSpeedValue = xzSpeed.get();
            ySpeedValue = ySpeed.get();
        }

        event.setBoostMultiplier(xzSpeedValue);
        event.setYSpeed(ySpeedValue);
    }

    private float normalizeAngle(float angle) {
        float normalized = angle % 180;
        if (normalized > 90) {
            normalized -= 180;
        } else if (normalized < -90) {
            normalized += 180;
        }
        return normalized;
    }
}