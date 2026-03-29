package su.hynix.ui.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import su.hynix.hynix;
import su.hynix.modules.Category;
import su.hynix.modules.Module;
import su.hynix.modules.api.elements.IBuilder;
import su.hynix.modules.api.elements.ModuleElement;
import su.hynix.ui.gui.themes.ThemeEditor;
import su.hynix.ui.gui.themes.ThemeSettings;
import su.hynix.utils.animation.AnimationUtil;
import su.hynix.utils.math.MathUtil;
import su.hynix.utils.render.ColorUtil;
import su.hynix.utils.render.GradientUtil;
import su.hynix.utils.render.RenderUtil;
import su.hynix.utils.render.ScissorUtil;
import su.hynix.utils.render.font.Fonts;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Panel implements IBuilder {
    private final List<ModuleElement> allModules = new ArrayList<>();
    protected float x;
    protected float y;
    protected float width = 120;
    protected float height = 275;
    float maxHeight = 0;
    private Category category;
    private List<ModuleElement> modules = new ArrayList<>();
    private AnimationUtil scrollAnimation = new AnimationUtil(0f, 10);
    private float scroll;

    public Panel() {
    }


    public Panel(Category category) {
        initializePanel(category);
    }

    private void initializePanel(Category category) {
        this.category = category;
        for (Module module : hynix.getInstance().getModuleManager().getModules()) {
            if (module.getCategory() == category) {
                ModuleElement component = new ModuleElement(module);
                component.setPanel(this);
                allModules.add(component);
            }
        }
        modules.addAll(allModules);
    }

    @Override
    public void render(MatrixStack stack, float mouseX, float mouseY, float alpha) {
        scrollAnimation.update(scroll);
        float animatedScroll = scrollAnimation.getValue();

        RenderUtil.drawBlurredRoundedRectangle(x, y + 4, width, height - 4, 8, ThemeEditor.getColor(ThemeSettings.MAIN), 1);
        RenderUtil.drawOutlineRectangle(x, y + 4, width, height - 4, 8, ThemeEditor.getColor(ThemeSettings.OUTLINE), ThemeEditor.getAlpha(ThemeSettings.OUTLINE));

        drawHeader(stack);
        drawContent(stack, mouseX, mouseY, alpha, animatedScroll);
    }

    private void drawHeader(MatrixStack stack) {
        final String categoryName = category.name();
        final float padding = 4;

        float textWidth = Fonts.sf_regular[26].getWidth(categoryName);
        float textHeight = Fonts.sf_medium[26].getHeight();
        int startX = (int) (x + (width - textWidth - padding) / 2);
        int textY = (int) (y - textHeight / 2 + 13);
        String iconText =
                categoryName.equals("Combat") ? "a" :
                        categoryName.equals("Movement") ? "b" :
                        categoryName.equals("Visuals") ? "d" :
                        categoryName.equals("Player") ? "c" : "e";
        ITextComponent gradientText = GradientUtil.gradient(iconText, ColorUtil.gradientInterface(90), ThemeEditor.getColor(ThemeSettings.LOGO), 70, 45);

        Fonts.sf_medium[19].drawString(stack, categoryName.replace("Miscellaneous", "Misc"), getX() + 10, getY() + 12 + 1, ThemeEditor.getColor(ThemeSettings.HEADER));
        Fonts.hynix[20].drawText(stack, gradientText, getX() + getWidth() - 18, getY() + 12 + 1, -1);
    }

    private void drawContent(MatrixStack stack, float mouseX, float mouseY, float alpha, float animatedScroll) {
        float contentHeight = getHeight() - 34.5f;

        if (maxHeight > contentHeight) {
            scroll = MathHelper.clamp(scroll, -maxHeight + contentHeight, 0);
            animatedScroll = MathHelper.clamp(animatedScroll, -maxHeight + contentHeight, 0);
        } else {
            scroll = animatedScroll = 0;
        }

        if (maxHeight > contentHeight) {
            float scrollbarX = x + width - 6;
            float scrollbarAreaHeight = height - 37;

            float thumbHeight = Math.max(20f, (contentHeight / maxHeight) * scrollbarAreaHeight);
            float thumbPosition = (-animatedScroll / (maxHeight - contentHeight)) * (scrollbarAreaHeight - thumbHeight);
        }

        float visibleTop = getY() + 28;
        float visibleBottom = visibleTop + height - 28;

        ScissorUtil.start(getX(), getY() + 28, getWidth(), getHeight() - 28.66f);

        float offset = 0;
        for (ModuleElement element : modules) {
            element.setX(getX() + 8);
            element.setY(Math.round(getY() + 28f + offset + animatedScroll));
            element.setWidth(getWidth() - 16);
            element.setHeight(18);

            if (element.getAnimation().getValue() > 0) {
                float componentOffset = (float) element.getElements().stream().mapToDouble(sub -> sub.getHeight() * sub.getVisibilityAnimation()).sum();
                float animatedHeight = 17 + componentOffset * element.getAnimation().getValue();
                element.setHeight(animatedHeight);
            }

            float moduleTop = element.getY();
            float moduleBottom = moduleTop + element.getHeight();
            if (moduleBottom > visibleTop && moduleTop < visibleBottom) {
                element.render(stack, mouseX, mouseY, alpha);
            }

            offset += element.getHeight() + 3;
        }
        maxHeight = offset;
        ScissorUtil.end();
    }


    @Override
    public void mouseClicked(float mouseX, float mouseY, int button) {
        if (MathUtil.isHovered(mouseX, mouseY, getX(), getY() + 28, getX() + getWidth() - getX(), getY() + 28 + getHeight() - 35 - getY() + 28)) {
            for (ModuleElement element : modules) {
                float moduleTop = element.getY();
                float moduleBottom = moduleTop + element.getHeight();
                if (moduleBottom > getY() + 28 && moduleTop < getY() + 28 + getHeight() - 35) {
                    if (MathUtil.isHovered(mouseX, mouseY, element.getX(), element.getY(), element.getWidth(), element.getHeight())) {
                        element.mouseClicked(mouseX, mouseY, button);
                    }
                }
            }
        }
    }


    @Override
    public void mouseReleased(float mouseX, float mouseY, int button) {
        for (ModuleElement element : modules) {
            element.mouseReleased(mouseX, mouseY, button);
        }
    }


    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (MathUtil.isHovered((float) mouseX, (float) mouseY, getX(), getY() + 28, getWidth(), getHeight() - 35)) {
            float visibleTop = getY() + 28;
            float visibleBottom = visibleTop + getHeight() - 28;
            for (ModuleElement element : modules) {
                float moduleTop = element.getY();
                float moduleBottom = moduleTop + element.getHeight();
                if (moduleBottom > visibleTop && moduleTop < visibleBottom) {
                    if (element.mouseDragged(mouseX, mouseY, button, dragX, dragY)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public void keyPressed(int keyCode, int scanCode, int modifiers) {
        for (ModuleElement element : modules) {
            element.keyPressed(keyCode, scanCode, modifiers);
        }
    }

    @Override
    public void charTyped(char codePoint, int modifiers) {
        for (ModuleElement element : modules) {
            element.charTyped(codePoint, modifiers);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        for (ModuleElement element : modules) {
            if (element.mouseScrolled(mouseX, mouseY, delta)) {
                return true;
            }
        }
        if (MathUtil.isHovered((float) mouseX, (float) mouseY, getX(), getY(), getWidth(), getHeight())) {
            float contentHeight = getHeight() - 34.5f;
            boolean canScroll = maxHeight > contentHeight;
            float previousScroll = scroll;

            setScroll((float) (getScroll() + (delta * 20)));
            scroll = MathHelper.clamp(scroll, -maxHeight + contentHeight, 0);

            return canScroll && scroll != previousScroll;
        }
        return false;
    }
}