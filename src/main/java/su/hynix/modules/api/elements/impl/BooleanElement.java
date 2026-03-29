package su.hynix.modules.api.elements.impl;

import com.mojang.blaze3d.matrix.MatrixStack;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.glfw.GLFW;
import su.hynix.hynix;
import su.hynix.modules.api.constructors.impl.BooleanSetting;
import su.hynix.modules.api.elements.Element;
import su.hynix.ui.gui.themes.ThemeEditor;
import su.hynix.ui.gui.themes.ThemeSettings;
import su.hynix.utils.animation.AnimationUtil;
import su.hynix.utils.animation.Easings;
import su.hynix.utils.math.MathUtil;
import su.hynix.utils.render.ColorUtil;
import su.hynix.utils.render.RenderUtil;
import su.hynix.utils.render.font.Fonts;

@Getter
@Setter
public class BooleanElement extends Element {
    private final BooleanSetting setting;
    private final AnimationUtil stateAnimation = new AnimationUtil(0f, 8f, Easings.QUAD_IN_OUT);
    private boolean bind;

    public BooleanElement(BooleanSetting setting) {
        this.setting = setting;
        stateAnimation.setValue(setting.get() ? 1f : 0f);
    }

    @Override
    public void render(MatrixStack stack, float mouseX, float mouseY, float alpha) {
        super.render(stack, mouseX, mouseY, alpha);

        Fonts.sf_medium[13].drawScrolledString(stack, setting.getName(), getX() + 6, getY() + 2, 80, ColorUtil.applyOpacity(ThemeEditor.getColor(ThemeSettings.TEXT), (ThemeEditor.getAlpha(ThemeSettings.TEXT) / 255F) * alpha), MathUtil.isHovered(mouseX, mouseY, getX() + 6, getY(), 80, Fonts.sf_medium[14].getHeight() + 2), getScrollState());

        setHeight(15);

        float toggleX = getX() + getWidth() - 16;
        float toggleY = getY() + getHeight() - 10;

        stateAnimation.update(setting.get() ? 1f : 0f);
        int currentBg = ColorUtil.interpolate(ThemeEditor.getColor(ThemeSettings.BUTTON), ThemeEditor.getColor(ThemeSettings.BUTTON_INACTIVE), alpha);
        int currentText = ColorUtil.interpolate(ThemeEditor.getColor(ThemeSettings.TEXT), ThemeEditor.getColor(ThemeSettings.TEXT_INACTIVE), alpha);

        float bgAlpha = (setting.get() ? ThemeEditor.getAlpha(ThemeSettings.BUTTON) : ThemeEditor.getAlpha(ThemeSettings.BUTTON_INACTIVE)) / 255F * alpha;
        int finalBg = ColorUtil.applyOpacity(currentBg, bgAlpha);

        RenderUtil.drawRoundedRectangle(toggleX + 0.5f, toggleY - 6, 10, 10, 2.5f, finalBg);

        float scale = stateAnimation.getValue() < 0.5f ? 1 - stateAnimation.getValue() * 2 : (stateAnimation.getValue() - 0.5f) * 2;
        float activeAlpha = ThemeEditor.getAlpha(ThemeSettings.INDICATOR) / 255F * alpha;
        float inactiveAlpha = ThemeEditor.getAlpha(ThemeSettings.INDICATOR_INACTIVE) / 255F * alpha;

        RenderUtil.scaleStart(toggleX + 7, toggleY - 1, scale);
        RenderUtil.drawImage2D(new ResourceLocation("hynix/icons/gui/check.png"), toggleX + 2.5f, toggleY - 4, 6, 6, ColorUtil.applyOpacity(ThemeEditor.getColor(ThemeSettings.INDICATOR), activeAlpha * stateAnimation.getValue()));
        RenderUtil.drawImage2D(new ResourceLocation("hynix/icons/gui/xmark.png"), toggleX + 3.5f, toggleY - 3, 4, 4, ColorUtil.applyOpacity(ThemeEditor.getColor(ThemeSettings.INDICATOR_INACTIVE), inactiveAlpha * (1 - stateAnimation.getValue())));
        RenderUtil.scaleEnd();
    }

    @Override
    public void mouseClicked(float mouseX, float mouseY, int button) {
        float toggleX = getX() + getWidth() - 16;
        float toggleY = getY() + (getHeight() - 10) - 7;

        if (MathUtil.isHovered(mouseX, mouseY, toggleX + 0.5f, toggleY + 0.5f, 10, 10)) {
            if (button == GLFW.GLFW_MOUSE_BUTTON_1) {
                if (stateAnimation.isAlive()) {
                    if (stateAnimation.getValue() != stateAnimation.getTarget()) {
                        return;
                    }
                }
                setting.set(!setting.get());
            } else if (button == GLFW.GLFW_MOUSE_BUTTON_2) {
                hynix.getInstance().getDropDown().getBindingPanelManager().openForBoolean(this);
            }
        } else if (button == GLFW.GLFW_MOUSE_BUTTON_3 && MathUtil.isHovered(mouseX, mouseY, getX(), getY(), getWidth() + 10, getHeight())) {
            if (stateAnimation.isAlive()) {
                if (stateAnimation.getValue() != stateAnimation.getTarget()) {
                    return;
                }
            }
            setting.set(setting.defaultVal);
        }

        super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void keyPressed(int keyCode, int scanCode, int modifiers) {
        super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean isVisible() {
        return setting.isVisible();
    }
}