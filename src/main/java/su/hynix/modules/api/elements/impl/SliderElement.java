package su.hynix.modules.api.elements.impl;

import com.mojang.blaze3d.matrix.MatrixStack;
import lombok.Setter;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;
import su.hynix.modules.api.constructors.impl.SliderSetting;
import su.hynix.modules.api.elements.Element;
import su.hynix.ui.gui.themes.ThemeEditor;
import su.hynix.ui.gui.themes.ThemeSettings;
import su.hynix.utils.math.MathUtil;
import su.hynix.utils.render.ColorUtil;
import su.hynix.utils.render.RenderUtil;
import su.hynix.utils.render.font.Fonts;

@Setter
public class SliderElement extends Element {
    private final SliderSetting setting;
    private boolean drag;
    private float anim;

    public SliderElement(SliderSetting setting) {
        this.setting = setting;
    }

    @Override
    public void render(MatrixStack stack, float mouseX, float mouseY, float alpha) {
        super.render(stack, mouseX, mouseY, alpha);
        setHeight(24);

        float valueWidth = Fonts.sf_regular[13].getWidth(String.valueOf(setting.get()));
        float valueX = getX() + getWidth() - valueWidth - 6;

        float textWidth = getWidth() - valueWidth - 15.5f;

        Fonts.sf_regular[13].drawScrolledString(stack, setting.getName(), getX() + 6, getY() + 2, textWidth, ColorUtil.applyOpacity(ThemeEditor.getColor(ThemeSettings.TEXT), (ThemeEditor.getAlpha(ThemeSettings.TEXT) / 255F) * alpha), MathUtil.isHovered(mouseX, mouseY, getX() + 6, getY(), textWidth, Fonts.sf_regular[14].getHeight() + 2), getScrollState());

        float targetWidth = (getWidth() - 15) * (setting.get() - setting.min) / (setting.max - setting.min);
        anim = MathUtil.fast(anim, targetWidth, 20);

        //RenderUtil.drawRoundedRectangle(valueX - 3, getY() - 1, valueWidth + 3.5f, 10, 2, ColorUtil.applyOpacity(ThemeEditor.getColor(ThemeSettings.SLIDER_WINDOW), (ThemeEditor.getAlpha(ThemeSettings.SLIDER_WINDOW) / 255F) * alpha));
        //RenderUtil.drawOutlineRectangle(valueX - 3, getY() - 1, valueWidth + 3.5f, 10, 2, ThemeEditor.getColor(ThemeSettings.OUTLINE), ThemeEditor.getAlpha(ThemeSettings.OUTLINE) * alpha);

        float rectCenterX = valueX + valueWidth / 2.0f - 1;
        float rectCenterY = getY() + 4.5f;

        float textX = rectCenterX - Fonts.sf_regular[13].getWidth(String.valueOf(setting.get())) / 2.0f;
        float textY = rectCenterY - Fonts.sf_regular[13].getHeight() / 2.0f;

        Fonts.sf_regular[13].drawString(stack, String.valueOf(setting.get()), textX, getY() + 2.5f, ColorUtil.applyOpacity(ThemeEditor.getColor(ThemeSettings.TEXT), (ThemeEditor.getAlpha(ThemeSettings.TEXT) / 255F) * alpha));

        RenderUtil.drawRoundedRectangle(getX() + 5, getY() + 12, getWidth() - 10.5f, 4, 1, ColorUtil.applyOpacity(ThemeEditor.getColor(ThemeSettings.OUTLINE), (ThemeEditor.getAlpha(ThemeSettings.OUTLINE) / 255F) * alpha));
        RenderUtil.drawRoundedRectangle(getX() + 5, getY() + 12, anim + 2, 4, 0.6F, ColorUtil.applyOpacity(ThemeEditor.getColor(ThemeSettings.SLIDER), (ThemeEditor.getAlpha(ThemeSettings.SLIDER) / 255F) * alpha));
        RenderUtil.drawRoundedRectangle(getX() + 4.5f + anim, getY() + 11.25f, 5.5f, 5.5f, 1.66f, ColorUtil.applyOpacity(ThemeEditor.getColor(ThemeSettings.SLIDER_CIRCLE), (ThemeEditor.getAlpha(ThemeSettings.SLIDER_CIRCLE) / 255F) * alpha));
    }


    private void applyMouseX(float mouseX) {
        float width = getWidth() - 17;
        float startCenter = getX() + 8.5f;
        float ratio = MathHelper.clamp((mouseX - startCenter) / width, 0.0f, 1.0f);
        float value = setting.min + ratio * (setting.max - setting.min);
        setting.set(MathHelper.clamp(MathUtil.round(value, setting.increment), setting.min, setting.max));
    }

    @Override
    public void mouseClicked(float mouseX, float mouseY, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_1 && MathUtil.isHovered(mouseX, mouseY, getX() + 5, getY() + 10, getWidth() - 10.5f, 6)) {
            drag = true;
            applyMouseX(mouseX);
        } else if (button == GLFW.GLFW_MOUSE_BUTTON_3 && MathUtil.isHovered(mouseX, mouseY, getX(), getY(), getWidth(), getHeight())) {
            setting.set(setting.defaultVal);
        }
    }

    @Override
    public void keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            drag = false;
        }
    }

    @Override
    public void mouseReleased(float mouseX, float mouseY, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_1) {
            drag = false;
        }
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_1 && drag) {
            applyMouseX((float) mouseX);
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (Screen.hasControlDown() && isHovered((float) mouseX, (float) mouseY)) {
            float step = setting.increment;
            float rawValue = setting.get() + ((float) delta > 0 ? step : -step);
            float roundedValue = MathUtil.round(rawValue, step);
            float clampedValue = MathHelper.clamp(roundedValue, setting.min, setting.max);
            setting.set(clampedValue);
            return true;
        }
        return false;
    }

    @Override
    public boolean isVisible() {
        return setting.isVisible();
    }
}