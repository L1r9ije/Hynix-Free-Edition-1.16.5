package su.hynix.modules.api.elements.impl;

import com.mojang.blaze3d.matrix.MatrixStack;
import org.lwjgl.glfw.GLFW;
import su.hynix.modules.api.constructors.impl.ButtonSetting;
import su.hynix.modules.api.elements.Element;
import su.hynix.ui.gui.themes.ThemeEditor;
import su.hynix.ui.gui.themes.ThemeSettings;
import su.hynix.utils.animation.AnimationUtil;
import su.hynix.utils.animation.Easings;
import su.hynix.utils.math.MathUtil;
import su.hynix.utils.render.ColorUtil;
import su.hynix.utils.render.RenderUtil;
import su.hynix.utils.render.font.Fonts;

public class ButtonElement extends Element {
    private final ButtonSetting setting;
    private final String textOn;
    private final String textOff;
    private final AnimationUtil hoverAnimation = new AnimationUtil(0.0f, 10, Easings.LINEAR);
    private float width, height;

    public ButtonElement(ButtonSetting setting, String textOff, String textOn) {
        this.setting = setting;
        this.textOn = textOn;
        this.textOff = textOff;
    }

    @Override
    public void render(MatrixStack stack, float mouseX, float mouseY, float alpha) {
        super.render(stack, mouseX, mouseY, alpha);

        width = 94;
        height = 13;
        float toggleX = getX() + 4;
        float toggleY = getY() - 2;

        boolean isHovered = MathUtil.isHovered(mouseX, mouseY, toggleX, toggleY, width, height);
        hoverAnimation.update(isHovered ? 1.0f : 0.0f);

        int interpolatedBg = ColorUtil.interpolate(ThemeEditor.getColor(ThemeSettings.BUTTON), ThemeEditor.getColor(ThemeSettings.BUTTON_INACTIVE), hoverAnimation.getValue());
        float bgAlpha = (isHovered ? ThemeEditor.getAlpha(ThemeSettings.BUTTON) : ThemeEditor.getAlpha(ThemeSettings.BUTTON_INACTIVE)) / 255F * alpha;
        int finalBg = ColorUtil.applyOpacity(interpolatedBg, bgAlpha);

        RenderUtil.drawRoundedRectangle(toggleX, toggleY, width, height, 1.5f, finalBg);
        RenderUtil.drawOutlineRectangle(toggleX, toggleY, width, height, 2, ThemeEditor.getColor(ThemeSettings.OUTLINE), ThemeEditor.getAlpha(ThemeSettings.OUTLINE) * alpha);

        String displayText = setting.get() ? textOn : textOff;

        float textWidth = Fonts.sf_regular[12].getWidth(displayText);
        float rectCenterX = toggleX + width / 2;

        int textX = (int) (rectCenterX - textWidth / 2) + 1;
        float textY = getY() + (getHeight() - 15.5f);

        int interpolatedText = ColorUtil.interpolate(ThemeEditor.getColor(ThemeSettings.TEXT), ThemeEditor.getColor(ThemeSettings.TEXT_INACTIVE), hoverAnimation.getValue());
        float txtAlpha = (isHovered ? ThemeEditor.getAlpha(ThemeSettings.TEXT) : ThemeEditor.getAlpha(ThemeSettings.TEXT_INACTIVE)) / 255F * alpha;
        int finalText = ColorUtil.applyOpacity(interpolatedText, txtAlpha);

        Fonts.sf_regular[12].drawString(stack, displayText, textX, textY, finalText);

        setHeight(19);
    }

    @Override
    public void mouseClicked(float mouseX, float mouseY, int button) {
        if (MathUtil.isHovered(mouseX, mouseY, getX() + 3, getY() - 4, width + 1, height + 2)) {
            if (button == GLFW.GLFW_MOUSE_BUTTON_1) {
                setting.set(!setting.get());
            } else if (button == GLFW.GLFW_MOUSE_BUTTON_3) {
                setting.set(setting.defaultVal);
            }
        }
        super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean isVisible() {
        return setting.isVisible();
    }
}