package su.hynix.ui.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import lombok.Getter;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.text.ITextComponent;
import org.lwjgl.glfw.GLFW;
import su.hynix.modules.Category;
import su.hynix.modules.api.elements.Element;
import su.hynix.modules.api.elements.ModuleElement;
import su.hynix.modules.impl.visuals.Interface;
import su.hynix.ui.gui.additionals.Searcher;
import su.hynix.ui.gui.autobuy.AutoBuyGui;
import su.hynix.ui.gui.guihandlers.BindingPanelHandler;
import su.hynix.ui.gui.guihandlers.ColorPickerHandler;
import su.hynix.ui.gui.themes.ThemeEditor;
import su.hynix.ui.gui.themes.ThemeSettings;
import su.hynix.utils.Wrapper;
import su.hynix.utils.math.MathUtil;
import su.hynix.utils.misc.OtherUtil;
import su.hynix.utils.render.ColorUtil;
import su.hynix.utils.render.GradientUtil;
import su.hynix.utils.render.font.Fonts;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DropDown extends Screen implements Wrapper {
    private final List<Panel> panels = new ArrayList<>();
    private final Searcher searchField;
    private final ColorPickerHandler colorPickerManager;
    @Getter
    private final BindingPanelHandler bindingPanelManager;
    private boolean ctrlGPressed = false;
    private boolean themeEditorVisible;
    private boolean autoBuyVisible;

    public DropDown(ITextComponent titleIn) {
        super(titleIn);
        for (Category category : Category.values()) {
            panels.add(new Panel(category));
        }
        panels.add(new ThemeEditor());
        panels.add(new AutoBuyGui());
        searchField = new Searcher(0, 0, 120, 24);
        colorPickerManager = new ColorPickerHandler(panels);
        bindingPanelManager = new BindingPanelHandler();
    }

    @Override
    public void tick() {
        updateMovementKeysWhileOpen();
        super.tick();
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        Vector2f mousevec = OtherUtil.getMouse(mouseX, mouseY);
        mouseX = (int) mousevec.getX();
        mouseY = (int) mousevec.getY();
        mc.gameRenderer.setupOverlayRendering(2);

        themeEditorVisible = Interface.themeeditor.get();
        autoBuyVisible = Interface.autobuy.get();

        for (Panel panel : panels) {
            if (isPanelHidden(panel)) {
                continue;
            }

            setPanelPosition(panel, mc.getMainWindow().getScaledWidth(), mc.getMainWindow().getScaledHeight(), (Category.values().length - 1) * 128, 120, 6, 275);
            panel.render(matrixStack, mouseX, mouseY, 1);
        }

        searchField.setX((mc.getMainWindow().getScaledWidth() - searchField.getWidth()) / 2f);
        searchField.setY(mc.getMainWindow().getScaledHeight() / 2f + 148);
        searchField.render(matrixStack);

        updatePanelModules();
        //renderUserInfo(matrixStack, mc.getMainWindow().getScaledHeight());
        renderModuleDescriptions(matrixStack, mouseX, mouseY);

        colorPickerManager.render(matrixStack, mouseX, mouseY);
        bindingPanelManager.render(matrixStack, mouseX, mouseY);
        mc.gameRenderer.setupOverlayRendering();
    }

    private void updateMovementKeysWhileOpen() {
        KeyBinding[] keys = {mc.gameSettings.keyBindForward, mc.gameSettings.keyBindBack, mc.gameSettings.keyBindLeft, mc.gameSettings.keyBindRight, mc.gameSettings.keyBindJump};
        for (KeyBinding keyBinding : keys) {
            boolean isKeyPressed = InputMappings.isKeyDown(mc.getMainWindow().getHandle(), keyBinding.getDefault().getKeyCode());
            keyBinding.setPressed(isKeyPressed);
        }
    }

    private boolean isPanelHidden(Panel panel) {
        return (panel instanceof ThemeEditor && !themeEditorVisible)
                || (panel instanceof AutoBuyGui && !autoBuyVisible);
    }

    private void setPanelPosition(Panel panel, int windowWidth, int windowHeight, float width, float panelWidth, float offset, float panelHeight) {
        final float baseY = windowHeight / 2f - panelHeight / 2f + 0.5f;
        if (panel instanceof ThemeEditor) {
            panel.setX(10);
            panel.setY(baseY);
            return;
        }
        if (panel instanceof AutoBuyGui) {
            panel.setX(windowWidth - panel.getWidth() - 10);
            panel.setY(baseY);
            return;
        }
        panel.setX((windowWidth / 2f) - (width / 2) + panel.getCategory().ordinal() * (panelWidth + offset) + offset / 2f + 67 - (panelWidth + offset));
        panel.setY(baseY);
    }


    private void renderModuleDescriptions(MatrixStack matrixStack, int mouseX, int mouseY) {
        for (Panel panel : panels) {
            float visibleTop = panel.getY() + 28;
            float visibleBottom = visibleTop + panel.getHeight() - 28;

            if (mouseY < visibleTop || mouseY > visibleBottom) {
                continue;
            }

            for (ModuleElement element : panel.getModules()) {
                if (!MathUtil.isHovered(mouseX, mouseY, element.getX(), element.getY(), element.getWidth(), element.getHeight())) {
                    continue;
                }

                float elementTop = element.getY();
                float elementBottom = elementTop + element.getHeight();

                if (elementBottom > visibleTop && elementTop < visibleBottom) {
                    String description = element.getModule().getDescription();
                    if (description != null && !description.isEmpty()) {
                        int tooltipX = (int) ((mc.getMainWindow().getScaledWidth() - Fonts.sf_medium[18].getWidth(description)) / 2f);
                        int color = ColorUtil.getColor(0, 0, 0);
                        ITextComponent gradientText = GradientUtil.gradient("b", ColorUtil.gradientInterface(90), ThemeEditor.getColor(ThemeSettings.LOGO), 90, 45);

                        Fonts.hynix[18].drawText(matrixStack, gradientText, tooltipX - 6, 96, -1);
                        Fonts.sf_medium[18].drawString(matrixStack, description, tooltipX + 6, 95, ThemeEditor.getColor(ThemeSettings.TOOLTIP));
                    }
                    return;
                }
            }
        }
    }

    private void updatePanelModules() {
        String searchText = searchField.getText().toLowerCase();
        for (Panel panel : panels) {
            if (panel instanceof ThemeEditor) continue;
            if (searchText.isEmpty()) {
                panel.setModules(new ArrayList<>(panel.getAllModules()));
            } else {
                List<ModuleElement> filteredModules = panel.getAllModules().stream().filter(module -> module.getModule().getName().toLowerCase().contains(searchText)).collect(Collectors.toList());
                panel.setModules(filteredModules);
            }
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        Vector2f mousevec = OtherUtil.getMouse((int) mouseX, (int) mouseY);
        mouseX = mousevec.getX();
        mouseY = mousevec.getY();

        for (Panel panel : panels) {
            if ((panel instanceof ThemeEditor && !themeEditorVisible)) {
                continue;
            }
            if (MathUtil.isHovered((float) mouseX, (float) mouseY, panel.getX(), panel.getY(), panel.getWidth(), panel.getHeight())) {
                boolean scrolled = panel.mouseScrolled(mouseX, mouseY, delta);
                if (scrolled) {
                    if (colorPickerManager.hasOpenColorPicker(panel)) {
                        colorPickerManager.closeAllPickers();
                    }
                    if (bindingPanelManager.isActive()) {
                        bindingPanelManager.close();
                    }
                    return true;
                }
            }
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        for (Panel panel : panels) {
            if ((panel instanceof ThemeEditor && !themeEditorVisible)) {
                continue;
            }
            panel.charTyped(codePoint, modifiers);
        }
        if (searchField.isFocused()) {
            searchField.charTyped(codePoint);
            return true;
        }
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (searchField.isFocused()) {
            searchField.keyPressed(keyCode, scanCode, modifiers);
            return true;
        }

        if (bindingPanelManager.keyPressed(keyCode)) {
            return true;
        }

        for (Panel panel : panels) {
            if ((panel instanceof ThemeEditor && !themeEditorVisible)) {
                continue;
            }
            panel.keyPressed(keyCode, scanCode, modifiers);
        }

        if (keyCode == GLFW.GLFW_KEY_G && (modifiers & GLFW.GLFW_MOD_CONTROL) != 0) {
            ctrlGPressed = true;
            return true;
        }

        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            closeScreen();
            searchField.setText("");
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }


    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_G || keyCode == GLFW.GLFW_KEY_LEFT_CONTROL || keyCode == GLFW.GLFW_KEY_RIGHT_CONTROL) {
            ctrlGPressed = false;
        }
        return super.keyReleased(keyCode, scanCode, modifiers);
    }


    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        Vector2f mousevec = OtherUtil.getMouse((int) mouseX, (int) mouseY);
        mouseX = mousevec.getX();
        mouseY = mousevec.getY();

        if (colorPickerManager.handleMouseClicked((float) mouseX, (float) mouseY, button)) {
            return true;
        }
        if (bindingPanelManager.isActive() && bindingPanelManager.mouseClicked((float) mouseX, (float) mouseY, button)) {
            return true;
        }

        if (button == GLFW.GLFW_MOUSE_BUTTON_MIDDLE) {
            ModuleElement hovered = findHoveredModuleHeaderElement((int) mouseX, (int) mouseY);
            if (hovered != null) {
                bindingPanelManager.openForModule(hovered);
                return true;
            }
        }

        for (Panel panel : panels) {
            if (panel instanceof ThemeEditor themeEditor && themeEditorVisible) {
                if (MathUtil.isHovered((float) mouseX, (float) mouseY, themeEditor.getX(), themeEditor.getY(), themeEditor.getWidth(), themeEditor.getHeight())) {
                    themeEditor.mouseClicked((float) mouseX, (float) mouseY, button);
                    return true;
                }
            }
        }

        if (searchField.mouseClicked((float) mouseX, (float) mouseY)) {
            return true;
        }

        if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            ModuleElement hovered = findHoveredModuleElement((int) mouseX, (int) mouseY);
            if (hovered != null) {
                if (hovered.getElements().stream().anyMatch(Element::isVisible)) {
                    for (Panel panel : panels) {
                        if (isPanelHidden(panel) || panel instanceof ThemeEditor) continue;
                        for (ModuleElement element : panel.getModules()) {
                            if (element != hovered) {
                                element.setOpen(false);
                            }
                        }
                    }
                }
            }
        }

        for (Panel panel : panels) {
            if (isPanelHidden(panel)) continue;
            panel.mouseClicked((float) mouseX, (float) mouseY, button);
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }


    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        Vector2f mousevec = OtherUtil.getMouse((int) mouseX, (int) mouseY);
        mouseX = (int) mousevec.getX();
        mouseY = (int) mousevec.getY();

        for (Panel panel : panels) {
            panel.mouseReleased((float) mouseX, (float) mouseY, button);
        }

        return super.mouseReleased(mouseX, mouseY, button);
    }


    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        Vector2f mousevec = OtherUtil.getMouse((int) mouseX, (int) mouseY);
        mouseX = (int) mousevec.getX();
        mouseY = (int) mousevec.getY();

        for (Panel panel : panels) {
            if (isPanelHidden(panel)) continue;
            if (MathUtil.isHovered((float) mouseX, (float) mouseY, panel.getX(), panel.getY(), panel.getWidth(), panel.getHeight())) {
                if (panel.mouseDragged(mouseX, mouseY, button, dragX, dragY)) {
                    return true;
                }
            }
        }

        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public void onClose() {
        colorPickerManager.closeAllPickers();
        bindingPanelManager.close();
        searchField.setText("");
        for (Panel panel : panels) {
            if (panel instanceof ThemeEditor) {
                ((ThemeEditor) panel).clearContextMenu();
            }
        }
        super.onClose();
    }

    private ModuleElement findHoveredModuleElement(int mouseX, int mouseY) {
        for (Panel panel : panels) {
            if (isPanelHidden(panel) || panel instanceof ThemeEditor) continue;

            float visibleTop = panel.getY() + 28;
            float visibleBottom = visibleTop + panel.getHeight() - 35;
            if (mouseY < visibleTop || mouseY > visibleBottom) continue;

            for (ModuleElement element : panel.getModules()) {
                if (MathUtil.isHovered(mouseX, mouseY, element.getX(), element.getY(), element.getWidth(), element.getHeight())) {
                    float elementTop = element.getY();
                    float elementBottom = elementTop + element.getHeight();
                    if (elementBottom > visibleTop && elementTop < visibleBottom) {
                        return element;
                    }
                }
            }
        }
        return null;
    }

    private ModuleElement findHoveredModuleHeaderElement(int mouseX, int mouseY) {
        for (Panel panel : panels) {
            if (isPanelHidden(panel) || panel instanceof ThemeEditor) continue;

            float visibleTop = panel.getY() + 28;
            float visibleBottom = visibleTop + panel.getHeight() - 35;
            if (mouseY < visibleTop || mouseY > visibleBottom) continue;

            for (ModuleElement element : panel.getModules()) {
                if (MathUtil.isHovered(mouseX, mouseY, element.getX(), element.getY(), element.getWidth(), 15)) {
                    float elementTop = element.getY();
                    float elementBottom = elementTop + element.getHeight();
                    if (elementBottom > visibleTop && elementTop < visibleBottom) {
                        return element;
                    }
                }
            }
        }
        return null;
    }
}