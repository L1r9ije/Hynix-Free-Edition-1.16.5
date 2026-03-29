package su.hynix.modules.api.elements.impl;

import com.mojang.blaze3d.matrix.MatrixStack;
import lombok.Getter;
import org.lwjgl.glfw.GLFW;
import su.hynix.modules.api.constructors.impl.StringSetting;
import su.hynix.modules.api.elements.Element;
import su.hynix.ui.gui.themes.ThemeEditor;
import su.hynix.ui.gui.themes.ThemeSettings;
import su.hynix.utils.animation.AnimationUtil;
import su.hynix.utils.animation.Easings;
import su.hynix.utils.render.ColorUtil;
import su.hynix.utils.render.RenderUtil;
import su.hynix.utils.render.font.Fonts;

@Getter
public class StringElement extends Element {
    private final StringSetting setting;
    private final AnimationUtil caretFadeAnimation = new AnimationUtil(0f, 12f, Easings.SINE_OUT);
    public boolean isFocused = false;
    private String inputText;
    private boolean isTextSelected = false;
    private long cursorAnimationStart = 0;

    public StringElement(StringSetting setting) {
        this.setting = setting;
        this.inputText = setting.get();
    }

    public void setInputText(String text) {
        this.inputText = text == null ? "" : text;
        this.setting.setValue(this.inputText);
    }

    @Override
    public void render(MatrixStack matrixStack, float mouseX, float mouseY, float alpha) {
        super.render(matrixStack, mouseX, mouseY, alpha);
        setHeight(18);

        String displayText = inputText.isEmpty() ? setting.getName() : inputText;
        boolean isEmpty = inputText.isEmpty();

        float inputWidth = getWidth() - 25;
        RenderUtil.drawRoundedRectangle(getX() + 5, getY() + 4, inputWidth, 13, 2.5F, ColorUtil.applyOpacity(ThemeEditor.getColor(ThemeSettings.BUTTON_INACTIVE), (ThemeEditor.getAlpha(ThemeSettings.BUTTON_INACTIVE) / 255F) * alpha));
        RenderUtil.drawOutlineRectangle(getX() + 5, getY() + 4, inputWidth, 13, 2.5f, ThemeEditor.getColor(ThemeSettings.OUTLINE), ThemeEditor.getAlpha(ThemeSettings.OUTLINE) * alpha);

        if (isTextSelected && !isEmpty) {
            int textWidth = (int) Math.min(Fonts.sf_medium[13].getWidth(displayText), 71);
            RenderUtil.drawMinecraftRectangle(matrixStack, getX() + 7, getY() + 6, textWidth + 1, 8, ColorUtil.applyOpacity(ColorUtil.getColor(26, 53, 255), 255 * alpha));
        }

        int cursorX;
        if (isEmpty) {
            cursorX = (int) (getX() + 7);
        } else {
            int textWidth = (int) Math.min(Fonts.sf_medium[13].getWidth(displayText), 70);
            cursorX = (int) (getX() + 8 + textWidth);

            int rightBound = (int) (getX() + 5 + inputWidth - 2);
            if (cursorX > rightBound) {
                cursorX = rightBound;
            }
        }

        long now = System.currentTimeMillis();
        if (cursorAnimationStart == 0) cursorAnimationStart = now;
        float blinkAlpha = (float) Math.sin((now - cursorAnimationStart) % 1000 / 500f * Math.PI) * 0.5f + 0.5f;
        caretFadeAnimation.update(isFocused ? 1f : 0f);
        if (!isFocused) {
            isTextSelected = false;
        }
        float caretFade = caretFadeAnimation.getValue();
        if (caretFade > 0.01f) {
            float finalAlpha = blinkAlpha * caretFade;
            RenderUtil.drawMinecraftRectangle(matrixStack, cursorX, getY() + 6, 0.5f, 8, ColorUtil.applyOpacity(-1, (int) (finalAlpha * 255)));
        }

        while (Fonts.sf_medium[13].getWidth(displayText) > 71 && !displayText.isEmpty()) {
            displayText = displayText.substring(1);
        }

        Fonts.sf_medium[13].drawString(matrixStack, displayText, getX() + 8, getY() + 9, inputText.isEmpty() ? ColorUtil.applyOpacity(ThemeEditor.getColor(ThemeSettings.TEXT_INACTIVE), (ThemeEditor.getAlpha(ThemeSettings.TEXT_INACTIVE) / 255F) * alpha) : ColorUtil.applyOpacity(ThemeEditor.getColor(ThemeSettings.TEXT), (ThemeEditor.getAlpha(ThemeSettings.TEXT) / 255F) * alpha));

        setting.set(inputText);
    }

    @Override
    public void mouseClicked(float mouseX, float mouseY, int button) {
        boolean isHovered = isHovered(mouseX, mouseY);

        if (isHovered) {
            isFocused = true;
            isTextSelected = false;
            cursorAnimationStart = System.currentTimeMillis();
        } else if (isFocused) {
            isFocused = false;
            isTextSelected = false;
            setting.setValue(inputText);
        }

        super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void mouseReleased(float mouseX, float mouseY, int button) {
        if (isFocused && !isHovered(mouseX, mouseY)) {
            isFocused = false;
            isTextSelected = false;
            setting.setValue(inputText);
        }

        super.mouseReleased(mouseX, mouseY, button);
    }

    public boolean isHovered(float mouseX, float mouseY) {
        return mouseX >= getX() + 5 && mouseX <= getX() + getWidth() - 5 && mouseY >= getY() + 4 && mouseY <= getY() + 15;
    }

    @Override
    public void keyPressed(int keyCode, int scanCode, int modifiers) {
        if (isFocused) {
            if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_ESCAPE) {
                isFocused = false;
                isTextSelected = false;
                setting.setValue(inputText);
                return;
            }

            if (keyCode == GLFW.GLFW_KEY_A && (modifiers & GLFW.GLFW_MOD_CONTROL) != 0) {
                isTextSelected = true;
                return;
            }

            if (keyCode == GLFW.GLFW_KEY_C && (modifiers & GLFW.GLFW_MOD_CONTROL) != 0) {
                if (isTextSelected && !inputText.isEmpty()) {
                    GLFW.glfwSetClipboardString(GLFW.glfwGetCurrentContext(), inputText);
                }
                return;
            }

            if (keyCode == GLFW.GLFW_KEY_V && (modifiers & GLFW.GLFW_MOD_CONTROL) != 0) {
                String clipboardText = GLFW.glfwGetClipboardString(GLFW.glfwGetCurrentContext());
                if (clipboardText != null && !clipboardText.isEmpty()) {
                    if (isTextSelected) {
                        inputText = "";
                        isTextSelected = false;
                    }
                    int maxLen = setting.getName().equals("Название") ? 16 : 100;
                    int remainingChars = maxLen - inputText.length();
                    if (remainingChars > 0) {
                        inputText += clipboardText.substring(0, Math.min(remainingChars, clipboardText.length()));
                    }
                }
                return;
            }

            if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
                if (isTextSelected) {
                    inputText = "";
                    isTextSelected = false;
                } else if (!inputText.isEmpty()) {
                    inputText = inputText.substring(0, inputText.length() - 1);
                }
                return;
            }
        }
        super.keyPressed(keyCode, scanCode, modifiers);
    }

    public void clearText() {
        this.inputText = "";
        this.setting.setValue("");
        this.isFocused = false;
        this.isTextSelected = false;
    }

    @Override
    public void charTyped(char codePoint, int modifiers) {
        if (isFocused) {
            if (isTextSelected) {
                inputText = String.valueOf(codePoint);
                isTextSelected = false;
            } else {
                int maxLen = setting.getName().equals("Название") ? 16 : 100;
                if (inputText.length() < maxLen) {
                    inputText += codePoint;
                }
            }
        }
        super.charTyped(codePoint, modifiers);
    }
}