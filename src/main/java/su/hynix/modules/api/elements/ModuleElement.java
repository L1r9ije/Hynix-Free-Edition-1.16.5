package su.hynix.modules.api.elements;

import com.mojang.blaze3d.matrix.MatrixStack;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.util.text.ITextComponent;
import su.hynix.modules.Module;
import su.hynix.modules.api.constructors.Setting;
import su.hynix.modules.api.constructors.impl.*;
import su.hynix.modules.api.elements.impl.*;
import su.hynix.ui.gui.themes.ThemeEditor;
import su.hynix.ui.gui.themes.ThemeSettings;
import su.hynix.utils.animation.AnimationUtil;
import su.hynix.utils.math.MathUtil;
import su.hynix.utils.render.ColorUtil;
import su.hynix.utils.render.GradientUtil;
import su.hynix.utils.render.RenderUtil;
import su.hynix.utils.render.ScissorUtil;
import su.hynix.utils.render.font.Fonts;

@Setter
@Getter
public class ModuleElement extends Element {
    private final Module module;
    private final AnimationUtil animation = new AnimationUtil(0.0f, 15);
    private final AnimationUtil alphaAnimation = new AnimationUtil(0.0f, 12);
    private final ObjectArrayList<Element> elements = new ObjectArrayList<>();
    public boolean open;
    private boolean bind;

    public ModuleElement(Module module) {
        this.module = module;
        for (Setting<?> setting : module.getSettings()) {
            if (setting instanceof BooleanSetting booleanSetting) {
                elements.add(new BooleanElement(booleanSetting));
            }
            if (setting instanceof SliderSetting sliderSetting) {
                elements.add(new SliderElement(sliderSetting));
            }
            if (setting instanceof BindSetting bindSetting) {
                elements.add(new BindElement(bindSetting));
            }
            if (setting instanceof ModeSetting modeSetting) {
                elements.add(new ModeElement(modeSetting));
            }
            if (setting instanceof MultiBooleanSetting multiBooleanSetting) {
                elements.add(new MultiBooleanElement(multiBooleanSetting));
            }
            if (setting instanceof ColorSetting colorSetting) {
                elements.add(new ColorElement(colorSetting));
            }
            if (setting instanceof StringSetting stringSetting) {
                elements.add(new StringElement(stringSetting));
            }
            if (setting instanceof TextSetting textSetting) {
                elements.add(new TextElement(textSetting));
            }
            if (setting instanceof ButtonSetting buttonSetting) {
                elements.add(new ButtonElement(buttonSetting, buttonSetting.getTextOn(), buttonSetting.getTextOff()));
            }
            if (setting instanceof ClickSetting clickSetting) {
                elements.add(new ClickElement(clickSetting));
            }
        }
    }

    @Override
    public void render(MatrixStack stack, float mouseX, float mouseY, float alpha) {
        module.getAnimation().update(module.isEnabled() ? 1f : 0f);

        RenderUtil.drawRoundedRectangle(getX(), getY(), getWidth(), getHeight(), 4, ColorUtil.interpolate(ThemeEditor.getColor(ThemeSettings.FIELD), ThemeEditor.getColor(ThemeSettings.FIELD_INACTIVE), module.getAnimation().getValue()));
        RenderUtil.drawOutlineRectangle(getX(), getY(), getWidth(), getHeight(), 4, ThemeEditor.getColor(ThemeSettings.OUTLINE), ThemeEditor.getAlpha(ThemeSettings.OUTLINE));
        float textX = getX() + 5;
        int textY = (int) (getY() + (16 - Fonts.sf_regular[16].getHeight()) / 2);

        Fonts.sf_regular[16].drawString(stack, module.getName(), textX, textY + 1.5f, ThemeEditor.getColor(ThemeSettings.TEXT));

        if (module.isPremium()) {
            ITextComponent gradientServer = GradientUtil.gradient("PREMIUM", ColorUtil.gradientGui(90), ThemeEditor.getColor(ThemeSettings.PREMIUM), 3, 35);
            Fonts.sf_regular[12].drawText(stack, gradientServer, elements.stream().anyMatch(Element::isVisible) ? getWidth() + getX() - Fonts.sf_regular[12].getWidth("PREMIUM") - 14.5F : getWidth() + getX() - Fonts.sf_regular[12].getWidth("PREMIUM") - 5.5F, textY + 3, ThemeEditor.getColor(ThemeSettings.PREMIUM));
        }
        if (open && animation.getValue() > 0.6) {
            RenderUtil.drawRoundedRectangle(getX(), getY() + 17.5f, getWidth(), 0.5f, 0, ThemeEditor.getColor(ThemeSettings.OUTLINE));
        }

        if (elements.stream().anyMatch(Element::isVisible)) {
            Fonts.hynix[16].drawString(stack, "h", getX() + getWidth() - 12, getY() + 7.565f,
                    ColorUtil.interpolate(ThemeEditor.getColor(ThemeSettings.LOGO), ThemeEditor.getColor(ThemeSettings.TEXT), module.getAnimation().getValue()));
        }

        drawComponents(stack, mouseX, mouseY);
        super.render(stack, mouseX, mouseY, alpha);
    }

    public void drawComponents(MatrixStack stack, float mouseX, float mouseY) {
        animation.update(open ? 1.0f : 0.0f);
        alphaAnimation.update(open ? 1.0f : 0.0f);

        if (animation.getValue() > 0.01) {
            ScissorUtil.start(getX(), getY(), getWidth(), getHeight());
            float baseYOffset = -8 * (1 - animation.getValue());
            float baseY = getY() + 20 + baseYOffset;
            float currentYOffset = 0;

            for (Element element : elements) {
                float visibleValue = element.getVisibilityAnimation();

                if (visibleValue > 0) {
                    float offsetY = -8 * (1.0f - visibleValue);
                    float targetY = baseY + currentYOffset + offsetY;
                    float currentY = baseY + (targetY - baseY) * animation.getValue();

                    element.setX(Math.round(getX()));
                    element.setY(Math.round(currentY));
                    element.setWidth(getWidth());

                    float alphaFade = alphaAnimation.getValue() * visibleValue;
                    element.render(stack, mouseX, mouseY, alphaFade);

                    currentYOffset += element.getHeight() * visibleValue;
                }
            }
            ScissorUtil.end();
        }
    }

    @Override
    public void keyPressed(int keyCode, int scanCode, int modifiers) {
        for (Element element : elements) {
            if (element.isVisible()) element.keyPressed(keyCode, scanCode, modifiers);
        }
        super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void charTyped(char codePoint, int modifiers) {
        for (Element element : elements) {
            if (element.isVisible()) element.charTyped(codePoint, modifiers);
        }
        super.charTyped(codePoint, modifiers);
    }

    @Override
    public void mouseClicked(float mouseX, float mouseY, int button) {
        if (isInScissorZone(mouseX, mouseY) && isHovered(mouseX, mouseY, 15)) {
            if (button == 0) {
//                if (module.getAnimation().isAlive() && module.getAnimation().getValue() != module.getAnimation().getTarget()) {
//                    return;
//                }
                module.toggle();
            } else if (button == 1) {
                if (elements.stream().anyMatch(Element::isVisible)) {
                    open = !open;
                }
            } else if (button == 2) {
                bind = !bind;
            }
        }

        if (open && animation.getValue() > 0.99f) {
            if (isInScissorZone(mouseX, mouseY)) {
                for (int i = elements.size() - 1; i >= 0; i--) {
                    Element element = elements.get(i);
                    if (!element.isVisible()) continue;
                    if (element.getVisibilityAnimation() < 0.99f) continue;
                    if (element.isHovered(mouseX, mouseY)) {
                        element.mouseClicked(mouseX, mouseY, button);
                        break;
                    }
                }
            }
        }

        super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (open) {
            for (Element element : elements) {
                if (element.isVisible() && element.mouseScrolled(mouseX, mouseY, delta)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (open) {
            for (Element element : elements) {
                if (element.isVisible() && element.mouseDragged(mouseX, mouseY, button, dragX, dragY)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isInScissorZone(float mouseX, float mouseY) {
        if (getPanel() == null) return false;
        return MathUtil.isHovered(mouseX, mouseY, getPanel().getX(), getPanel().getY() + 28, getPanel().getWidth(), getPanel().getHeight() - 35);
    }

    @Override
    public void mouseReleased(float mouseX, float mouseY, int button) {
        for (Element element : elements) {
            element.mouseReleased(mouseX, mouseY, button);
        }
        super.mouseReleased(mouseX, mouseY, button);
    }
}