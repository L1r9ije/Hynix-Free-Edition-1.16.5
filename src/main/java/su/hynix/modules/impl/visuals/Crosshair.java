package su.hynix.modules.impl.visuals;

import com.darkmagician6.eventapi.EventTarget;
import com.mojang.blaze3d.matrix.MatrixStack;
import su.hynix.events.EventCrosshair;
import su.hynix.hynix;
import su.hynix.modules.Category;
import su.hynix.modules.Module;
import su.hynix.modules.api.constructors.impl.BooleanSetting;
import su.hynix.modules.api.constructors.impl.SliderSetting;
import su.hynix.modules.impl.movement.FreeCamera;
import su.hynix.utils.render.ColorUtil;
import su.hynix.utils.render.RenderUtil;

public class Crosshair extends Module {

    private final SliderSetting gap = new SliderSetting("Зазор", 2.0f, 0.0f, 6, 0.5F);
    private final SliderSetting length = new SliderSetting("Длина", 3.0f, 2, 5, 0.5F);
    private final SliderSetting thickness = new SliderSetting("Толщина", 2.0f, 1, 6, 1);

    private final BooleanSetting changeColorOnHover = new BooleanSetting("Менять цвет при наведении", false);
    private final BooleanSetting dynamicCrosshair = new BooleanSetting("Динамический разрыв", false);
    private final BooleanSetting showOutline = new BooleanSetting("Обводка", true);
    private final BooleanSetting showDot = new BooleanSetting("Точка в центре", true);

    public Crosshair() {
        super("Crosshair", "Позволяет кастомизировать прицел", Category.Visuals);
        addSettings(gap, length, thickness, changeColorOnHover, dynamicCrosshair, showOutline, showDot);
    }

    @EventTarget
    public void onRender2D(EventCrosshair e) {
        if (mc.gameSettings.getPointOfView().firstPerson() || hynix.getInstance().getModuleManager().getModule(FreeCamera.class).isEnabled()) {
            float cx = mc.getMainWindow().getScaledWidth() / 2.0f;
            float cy = mc.getMainWindow().getScaledHeight() / 2.0f;
            float cooldown = 1.0f - mc.player.getCooledAttackStrength(e.getPartialTicks());
            drawCustomCrosshair(e.getStack(), cx, cy, cooldown);
            e.setCancelled(true);
        }
    }

    private void drawCustomCrosshair(MatrixStack stack, float centerX, float centerY, float cooldown) {
        float gapValue = gap.get();
        float thicknessValue = thickness.get();
        float lengthValue = length.get();

        float actualGap = dynamicCrosshair.get() ? gapValue + (8.0f * cooldown) : gapValue;
        float outline = 1.0f;

        int color = (changeColorOnHover.get() && mc.pointedEntity != null) ? ColorUtil.getColor(255, 64, 64) : -1;

        if (showOutline.get()) {
            RenderUtil.drawMinecraftRectangle(stack, centerX + actualGap - outline / 2.0f, centerY - thicknessValue / 2.0f - outline / 2.0f, lengthValue + outline, thicknessValue + outline, ColorUtil.getColor(0, 0, 0));
            RenderUtil.drawMinecraftRectangle(stack, centerX - actualGap - lengthValue - outline / 2.0f, centerY - thicknessValue / 2.0f - outline / 2.0f, lengthValue + outline, thicknessValue + outline, ColorUtil.getColor(0, 0, 0));
            RenderUtil.drawMinecraftRectangle(stack, centerX - thicknessValue / 2.0f - outline / 2.0f, centerY - actualGap - lengthValue - outline / 2.0f, thicknessValue + outline, lengthValue + outline, ColorUtil.getColor(0, 0, 0));
            RenderUtil.drawMinecraftRectangle(stack, centerX - thicknessValue / 2.0f - outline / 2.0f, centerY + actualGap - outline / 2.0f, thicknessValue + outline, lengthValue + outline, ColorUtil.getColor(0, 0, 0));


            RenderUtil.drawMinecraftRectangle(stack, centerX + actualGap, centerY - thicknessValue / 2.0f, lengthValue, thicknessValue, color);
            RenderUtil.drawMinecraftRectangle(stack, centerX - actualGap - lengthValue, centerY - thicknessValue / 2.0f, lengthValue, thicknessValue, color);
            RenderUtil.drawMinecraftRectangle(stack, centerX - thicknessValue / 2.0f, centerY - actualGap - lengthValue, thicknessValue, lengthValue, color);
            RenderUtil.drawMinecraftRectangle(stack, centerX - thicknessValue / 2.0f, centerY + actualGap, thicknessValue, lengthValue, color);
        } else {
            RenderUtil.drawMinecraftRectangle(stack, centerX + actualGap, centerY - thicknessValue / 2.0f, lengthValue, thicknessValue, color);
            RenderUtil.drawMinecraftRectangle(stack, centerX - actualGap - lengthValue, centerY - thicknessValue / 2.0f, lengthValue, thicknessValue, color);
            RenderUtil.drawMinecraftRectangle(stack, centerX - thicknessValue / 2.0f, centerY - actualGap - lengthValue, thicknessValue, lengthValue, color);
            RenderUtil.drawMinecraftRectangle(stack, centerX - thicknessValue / 2.0f, centerY + actualGap, thicknessValue, lengthValue, color);
        }

        if (showDot.get() && actualGap > 0.0f) {
            float dotSize = thicknessValue;
            float dx = centerX - dotSize / 2.0f;
            float dy = centerY - dotSize / 2.0f;
            if (showOutline.get()) {
                RenderUtil.drawMinecraftRectangle(stack, dx - outline / 2.0f, dy - outline / 2.0f, dotSize + outline, dotSize + outline, ColorUtil.getColor(0, 0, 0));
                RenderUtil.drawMinecraftRectangle(stack, dx, dy, dotSize, dotSize, color);
            } else {
                RenderUtil.drawMinecraftRectangle(stack, dx, dy, dotSize, dotSize, color);
            }
        }
    }
}
