package su.hynix.modules.api.elements.impl;

import com.mojang.blaze3d.matrix.MatrixStack;
import org.lwjgl.glfw.GLFW;
import su.hynix.modules.api.constructors.impl.ModeSetting;
import su.hynix.modules.api.elements.Element;
import su.hynix.ui.gui.themes.ThemeEditor;
import su.hynix.ui.gui.themes.ThemeSettings;
import su.hynix.utils.animation.AnimationUtil;
import su.hynix.utils.animation.Easings;
import su.hynix.utils.math.MathUtil;
import su.hynix.utils.render.ColorUtil;
import su.hynix.utils.render.RenderUtil;
import su.hynix.utils.render.font.Fonts;

import java.util.HashMap;
import java.util.Map;

public class ModeElement extends Element {
    private final ModeSetting setting;
    private final Map<String, AnimationUtil> colorAnimations;

    public ModeElement(ModeSetting setting) {
        this.setting = setting;
        this.colorAnimations = new HashMap<>();
        for (String mode : setting.strings) {
            colorAnimations.put(mode, new AnimationUtil(mode.equals(setting.get()) ? 1f : 0f, 10f, Easings.LINEAR));
        }
    }

    @Override
    public void render(MatrixStack stack, float mouseX, float mouseY, float alpha) {
        super.render(stack, mouseX, mouseY, alpha);

        float offsetX = 0, offsetY = 0, totalHeight = 0;

        Fonts.sf_medium[14].drawString(stack, setting.getName(), getX() + 6, getY() + 2, ColorUtil.applyOpacity(ThemeEditor.getColor(ThemeSettings.TEXT), (ThemeEditor.getAlpha(ThemeSettings.TEXT) / 255F) * alpha));

        for (String text : setting.strings) {
            float boxWidth = Fonts.sf_medium[12].getWidth(text) + 5;
            float boxHeight = Fonts.sf_medium[12].getHeight() + 6;

            if (offsetX + boxWidth >= getWidth() - 8) {
                offsetX = 0;
                offsetY += boxHeight + 2;
            }

            AnimationUtil animation = colorAnimations.get(text);
            animation.update(text.equals(setting.get()) ? 1f : 0f);

            int currentBg = ColorUtil.interpolate(ThemeEditor.getColor(ThemeSettings.BUTTON), ThemeEditor.getColor(ThemeSettings.BUTTON_INACTIVE), animation.getValue());
            int currentText = ColorUtil.interpolate(ThemeEditor.getColor(ThemeSettings.TEXT), ThemeEditor.getColor(ThemeSettings.TEXT_INACTIVE), animation.getValue());

            float bgAlpha = (text.equals(setting.get()) ? ThemeEditor.getAlpha(ThemeSettings.BUTTON) : ThemeEditor.getAlpha(ThemeSettings.BUTTON_INACTIVE)) / 255F * alpha;
            int finalBg = ColorUtil.applyOpacity(currentBg, bgAlpha);
            RenderUtil.drawRoundedRectangle(getX() + 5.5f + offsetX, getY() + 10 + offsetY - 0.5f, boxWidth + 2, boxHeight + 1.0f, 1.5f, finalBg);
            RenderUtil.drawOutlineRectangle(getX() + 5.5f + offsetX, getY() + 10 + offsetY - 0.5f, boxWidth + 2, boxHeight + 1.0f, 1.5f, ThemeEditor.getColor(ThemeSettings.OUTLINE), ThemeEditor.getAlpha(ThemeSettings.OUTLINE) * alpha);

            float rectCenterX = getX() + 5 + offsetX + boxWidth / 2;
            float rectCenterY = getY() + 10 + offsetY + boxHeight / 2;

            float textX = rectCenterX - Fonts.sf_medium[12].getWidth(text) / 2;
            float textY = rectCenterY - Fonts.sf_medium[12].getHeight() / 2 + 1;

            float txtAlpha = (text.equals(setting.get()) ? ThemeEditor.getAlpha(ThemeSettings.TEXT) : ThemeEditor.getAlpha(ThemeSettings.TEXT_INACTIVE)) / 255F * alpha;
            int finalText = ColorUtil.applyOpacity(currentText, txtAlpha);
            Fonts.sf_medium[12].drawString(stack, text, textX + 1.5f, textY, finalText);

            offsetX += boxWidth + 3;
            totalHeight = Math.max(totalHeight, offsetY + boxHeight);
        }

        setHeight(9.5f + totalHeight + Fonts.sf_medium[14].getHeight() + 2.5f);
    }

    @Override
    public void mouseClicked(float mouseX, float mouseY, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_1) {
            float offsetX = 0, offsetY = 0;

            for (String text : setting.strings) {
                float boxWidth = Fonts.sf_medium[12].getWidth(text) + 5;
                float boxHeight = Fonts.sf_medium[12].getHeight() + 6;

                if (offsetX + boxWidth >= getWidth() - 8) {
                    offsetX = 0;
                    offsetY += boxHeight + 2;
                }

                if (MathUtil.isHovered(mouseX, mouseY, getX() + 5.5f + offsetX, getY() + 10 + offsetY - 0.5f, boxWidth + 1, boxHeight + 0.5f)) {
                    boolean anyAlive = false;
                    for (AnimationUtil anim : colorAnimations.values()) {
                        if (anim.isAlive() && anim.getValue() != anim.getTarget()) {
                            anyAlive = true;
                            break;
                        }
                    }
                    if (anyAlive) {
                        break;
                    }
                    setting.set(text);
                    for (String mode : setting.strings) {
                        colorAnimations.get(mode).update(mode.equals(setting.get()) ? 1f : 0f);
                    }
                    break;
                }

                offsetX += boxWidth + 3;
            }
        } else if (button == GLFW.GLFW_MOUSE_BUTTON_3 && MathUtil.isHovered(mouseX, mouseY, getX(), getY(), getWidth(), getHeight())) {
            boolean anyAlive = false;
            for (AnimationUtil anim : colorAnimations.values()) {
                if (anim.isAlive() && anim.getValue() != anim.getTarget()) {
                    anyAlive = true;
                    break;
                }
            }
            if (!anyAlive) {
                setting.set(setting.defaultVal);
                for (String mode : setting.strings) {
                    colorAnimations.get(mode).update(mode.equals(setting.get()) ? 1f : 0f);
                }
            }
        }
        super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean isVisible() {
        return setting.isVisible();
    }
}