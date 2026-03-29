package su.hynix.ui.gui.additionals;

import com.mojang.blaze3d.matrix.MatrixStack;
import lombok.Getter;
import lombok.Setter;
import org.lwjgl.glfw.GLFW;
import su.hynix.ui.gui.themes.ThemeEditor;
import su.hynix.ui.gui.themes.ThemeSettings;
import su.hynix.utils.render.ColorUtil;
import su.hynix.utils.render.RenderUtil;
import su.hynix.utils.render.font.Fonts;


@Getter
@Setter
public class Searcher {

    private float x, y, width, height;
    private String text = "";
    private boolean focused = false;
    private boolean activated = false;
    private boolean isTextSelected = false;
    private long cursorAnimationStart = 0;

    public Searcher(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public void render(MatrixStack stack) {
        RenderUtil.drawBlurredRoundedRectangle(x, y, width, height, 6, ThemeEditor.getColor(ThemeSettings.MAIN), 1);
        RenderUtil.drawOutlineRectangle(x, y, width, height, 6, ThemeEditor.getColor(ThemeSettings.OUTLINE), ThemeEditor.getAlpha(ThemeSettings.OUTLINE));

        String displayText = text.isEmpty() && !focused ? "Search" : text;
        float textX = x + 5;
        float maxWidth = width - 10;

        if (!text.isEmpty() && Fonts.sf_medium[15].getWidth(displayText) > maxWidth) {
            while (Fonts.sf_medium[15].getWidth(displayText) > maxWidth && displayText.length() > 1) {
                displayText = displayText.substring(1);
            }
        }

        float cursorX;
        if (text.isEmpty() && !focused) {
            cursorX = textX + Fonts.sf_medium[15].getWidth("Search");
        } else {
            cursorX = textX + Fonts.sf_medium[15].getWidth(displayText);
        }

        float rightBound = x + width - 5;
        if (cursorX > rightBound) {
            cursorX = rightBound;
        }

        if (activated) {
            long now = System.currentTimeMillis();
            if (cursorAnimationStart == 0) cursorAnimationStart = now;

            float alpha = (float) Math.sin((now - cursorAnimationStart) % 1000 / 500f * Math.PI) * 0.5f + 0.5f;
            float cursorWidth = 4;
            RenderUtil.drawMinecraftRectangle(stack, cursorX, y + height - 10, cursorWidth, 0.5f, ColorUtil.applyOpacity(-1, (int) (alpha * 255)));
        } else {
            cursorAnimationStart = 0;
        }

        Fonts.sf_medium[15].drawString(stack, displayText, textX + 1.5f, y + (height / 2 - 4) + 2, text.isEmpty() && !focused ? ThemeEditor.getColor(ThemeSettings.TEXT_INACTIVE) : ThemeEditor.getColor(ThemeSettings.TEXT));
        Fonts.hynix[16].drawString(stack, "g", getX() + getWidth() - 15, y + (height / 2 - 4) + 2, ThemeEditor.getColor(ThemeSettings.TEXT_INACTIVE));

    }


    public boolean mouseClicked(float mouseX, float mouseY) {
        boolean wasFocused = focused;
        focused = mouseX >= x && mouseX <= x + width - 1 && mouseY >= y && mouseY <= y + height - 1;

        if (focused && !wasFocused) {
            activated = true;
            cursorAnimationStart = System.currentTimeMillis();
            isTextSelected = false;
        } else if (!focused) {
            activated = false;
            isTextSelected = false;
        }

        return focused;
    }


    public void charTyped(char codePoint) {
        if (focused && activated) {
            if (isTextSelected) {
                text = String.valueOf(codePoint);
                isTextSelected = false;
            } else if (codePoint >= 32 && codePoint <= 126 && text.length() < 30) {
                text += codePoint;
            }
        }
    }


    public void keyPressed(int keyCode, int scanCode, int modifiers) {
        if (focused && activated) {
            if (keyCode == GLFW.GLFW_KEY_A && (modifiers & GLFW.GLFW_MOD_CONTROL) != 0) {
                if (!text.isEmpty()) {
                    isTextSelected = true;
                }
                return;
            }

            if (keyCode == GLFW.GLFW_KEY_C && (modifiers & GLFW.GLFW_MOD_CONTROL) != 0) {
                if (isTextSelected && !text.isEmpty()) {
                    GLFW.glfwSetClipboardString(GLFW.glfwGetCurrentContext(), text);
                }
                return;
            }

            if (keyCode == GLFW.GLFW_KEY_V && (modifiers & GLFW.GLFW_MOD_CONTROL) != 0) {
                String clipboardText = GLFW.glfwGetClipboardString(GLFW.glfwGetCurrentContext());
                if (clipboardText != null && !clipboardText.isEmpty()) {
                    if (isTextSelected) {
                        text = "";
                        isTextSelected = false;
                    }
                    int remainingChars = 30 - text.length();
                    if (remainingChars > 0) {
                        text += clipboardText.substring(0, Math.min(remainingChars, clipboardText.length()));
                    }
                }
                return;
            }

            if (keyCode == GLFW.GLFW_KEY_BACKSPACE && !text.isEmpty()) {
                if (isTextSelected) {
                    text = "";
                    isTextSelected = false;
                } else {
                    text = text.substring(0, text.length() - 1);
                }
                return;
            }

            if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_ESCAPE) {
                activated = false;
                focused = false;
                isTextSelected = false;
            }
        }
    }
}