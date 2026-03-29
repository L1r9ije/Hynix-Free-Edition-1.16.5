package su.hynix.modules.api.elements.impl;

import com.mojang.blaze3d.matrix.MatrixStack;
import lombok.Getter;
import org.lwjgl.glfw.GLFW;
import su.hynix.modules.api.constructors.impl.ColorSetting;
import su.hynix.modules.api.elements.Element;
import su.hynix.ui.gui.themes.ThemeEditor;
import su.hynix.ui.gui.themes.ThemeSettings;
import su.hynix.utils.math.MathUtil;
import su.hynix.utils.render.ColorUtil;
import su.hynix.utils.render.RenderUtil;
import su.hynix.utils.render.font.Fonts;

@Getter
public class ColorElement extends Element {
    private final ColorSetting setting;
    private final ColorPickerElement colorPicker;

    public ColorElement(ColorSetting setting) {
        this.setting = setting;
        this.colorPicker = new ColorPickerElement(setting);
    }

    @Override
    public void render(MatrixStack stack, float mouseX, float mouseY, float alpha) {
        super.render(stack, mouseX, mouseY, alpha);


        Fonts.sf_medium[13].drawScrolledString(stack, setting.getName(), getX() + 6, getY() + 2, 80, ColorUtil.applyOpacity(ThemeEditor.getColor(ThemeSettings.TEXT), (ThemeEditor.getAlpha(ThemeSettings.TEXT) / 255F) * alpha), MathUtil.isHovered(mouseX, mouseY, getX() + 6, getY(), 80, Fonts.sf_regular[14].getHeight() + 2), getScrollState());
        float selectedCircleRadius = 2.5f;
        RenderUtil.drawRoundedRectangle(getX() + getWidth() - 16 + selectedCircleRadius + 0.5f, getY() + 1.5f, selectedCircleRadius * 2, selectedCircleRadius * 2, selectedCircleRadius - 1, ColorUtil.applyOpacity(setting.get(), setting.getAlpha() * alpha / 255));
        RenderUtil.drawOutlineRectangleBold(
                getX() + getWidth() - 16 + selectedCircleRadius - 1f,
                getY(),
                (selectedCircleRadius * 2) + 3,
                (selectedCircleRadius * 2) + 3,
                3.25f,
                setting.get(), setting.getAlpha() * alpha
        );

        setHeight(15f);
    }

    @Override
    public boolean isVisible() {
        return setting.isVisible();
    }

    @Override
    public void mouseClicked(float mouseX, float mouseY, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_1 && MathUtil.isHovered(mouseX, mouseY, getX(), getY() - 1, getWidth(), getHeight() - 7)) {
            colorPicker.setColorPickMode(!colorPicker.isColorPickMode());
        } else if (button == GLFW.GLFW_MOUSE_BUTTON_3 && MathUtil.isHovered(mouseX, mouseY, getX(), getY(), getWidth(), getHeight())) {
            setting.set(setting.defaultVal);
            colorPicker.updateHSBFromColor();
        } else if (colorPicker.isColorPickMode()) {
            colorPicker.mouseClicked(mouseX, mouseY);
        }
    }

    @Override
    public void mouseReleased(float mouseX, float mouseY, int button) {
        if (colorPicker.isColorPickMode()) {
            colorPicker.mouseReleased();
        }
    }
}