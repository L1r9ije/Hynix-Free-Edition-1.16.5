package su.hynix.managers.impl.dragmanager.elements.impl;

import com.mojang.blaze3d.matrix.MatrixStack;
import lombok.Getter;
import org.lwjgl.glfw.GLFW;
import su.hynix.modules.api.constructors.impl.BooleanSetting;
import su.hynix.modules.api.elements.Element;
import su.hynix.ui.gui.themes.ThemeEditor;
import su.hynix.ui.gui.themes.ThemeSettings;
import su.hynix.utils.animation.AnimationUtil;
import su.hynix.utils.animation.Easings;
import su.hynix.utils.render.ColorUtil;
import su.hynix.utils.render.RenderUtil;
import su.hynix.utils.render.font.Fonts;

public class DraggingBooleanElement extends Element {
    @Getter
    private final BooleanSetting setting;
    private final AnimationUtil animation;

    public DraggingBooleanElement(BooleanSetting setting) {
        this.setting = setting;
        this.animation = new AnimationUtil(setting.get() ? 1f : 0f, 10f, Easings.LINEAR);
    }

    @Override
    public void render(MatrixStack stack, float mouseX, float mouseY, float alpha) {
        animation.update(setting.get() ? 1f : 0f);
        super.render(stack, mouseX, mouseY, alpha);

        Fonts.sf_medium[12].drawString(stack, setting.getName(), getX() + 3, getY() + 4.5f, ThemeEditor.getColor(ThemeSettings.TEXT));

        float progress = animation.getValue();

        int boxColor = ColorUtil.interpolate(ThemeEditor.getColor(ThemeSettings.BUTTON), ThemeEditor.getColor(ThemeSettings.BUTTON_INACTIVE), progress);
        int textColor = ColorUtil.interpolate(-1, ColorUtil.getColor(255, 255, 255, 175), progress);

        RenderUtil.drawRoundedRectangle(getX() + getWidth() - 18, getY() + 2, 15, 8, 3, boxColor);
        float offsetX = progress * 7f;
        RenderUtil.drawRoundedRectangle(getX() + getWidth() - 17 + offsetX, getY() + 3, 6, 6, 2, textColor);

        setHeight(Math.max(10, Fonts.sf_medium[12].getHeight()));
    }

    @Override
    public void mouseClicked(float mouseX, float mouseY, int button) {
        boolean inBounds = mouseX >= getX() + getWidth() - 18 && mouseX <= getX() + getWidth() - 4
                && mouseY >= getY() + 1.5f && mouseY <= getY() + 9.5f;

        if (inBounds) {
            if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                setting.set(!setting.get());
                animation.update(setting.get() ? 1f : 0f);
            } else if (button == GLFW.GLFW_MOUSE_BUTTON_MIDDLE) {
                setting.set(setting.defaultVal);
                animation.update(setting.defaultVal ? 1f : 0f);
            }
        }
        super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean isVisible() {
        return setting.isVisible();
    }
}