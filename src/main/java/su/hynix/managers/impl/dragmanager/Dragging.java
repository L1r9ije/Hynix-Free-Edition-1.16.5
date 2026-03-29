package su.hynix.managers.impl.dragmanager;

import com.darkmagician6.eventapi.EventManager;
import com.darkmagician6.eventapi.EventTarget;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector2f;
import su.hynix.events.EventDragging;
import su.hynix.events.EventMouseClicked;
import su.hynix.events.EventMouseReleased;
import su.hynix.events.EventRender2D;
import su.hynix.hynix;
import su.hynix.managers.impl.dragmanager.elements.impl.DraggingBooleanElement;
import su.hynix.managers.impl.dragmanager.elements.impl.DraggingModeElement;
import su.hynix.modules.api.constructors.Setting;
import su.hynix.modules.api.constructors.impl.BooleanSetting;
import su.hynix.modules.api.constructors.impl.ModeSetting;
import su.hynix.modules.api.constructors.impl.MultiBooleanSetting;
import su.hynix.modules.api.elements.Element;
import su.hynix.modules.impl.visuals.Interface;
import su.hynix.ui.gui.themes.ThemeEditor;
import su.hynix.ui.gui.themes.ThemeSettings;
import su.hynix.utils.Wrapper;
import su.hynix.utils.animation.AnimationUtil;
import su.hynix.utils.animation.Easings;
import su.hynix.utils.math.MathUtil;
import su.hynix.utils.misc.OtherUtil;
import su.hynix.utils.render.ColorUtil;
import su.hynix.utils.render.RenderUtil;
import su.hynix.utils.render.font.Fonts;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class Dragging implements Wrapper {
    private static Dragging currentlyDragging = null;
    private final float initialX, initialY;
    private final MultiBooleanSetting elements;
    private final AnimationUtil textAlphaAnimation = new AnimationUtil(0f, 15f, Easings.LINEAR);
    private final AnimationUtil scaleAnimation = new AnimationUtil(0f, 10f, Easings.LINEAR);
    private final List<Setting<?>> settings = new ArrayList<>();
    private final List<Element> settingElements = new ArrayList<>();
    private final DraggingManager draggingManager;
    private final boolean draggable;
    private String name;
    private float x, y, width, height;
    private boolean isDragging, isHovered, settingsVisible;
    private float offsetX, offsetY, settingsX, settingsY, settingsOffsetX, settingsOffsetY;

    public Dragging(String name, float x, float y, MultiBooleanSetting elements) {
        this(name, x, y, elements, true);
    }

    public Dragging(String name, float x, float y, MultiBooleanSetting elements, boolean draggable) {
        this.name = name;
        this.x = x;
        this.y = y;
        this.initialX = x;
        this.initialY = y;
        this.elements = elements;
        this.draggable = draggable;

        this.draggingManager = hynix.getInstance().getDraggingManager();
        draggingManager.addDraggable(this);
        EventManager.register(this);
    }

    public void addSettings(Setting<?>... settings) {
        for (Setting<?> s : settings) {
            this.settings.add(s);
            if (s instanceof BooleanSetting bs) {
                settingElements.add(new DraggingBooleanElement(bs));
            }
            if (s instanceof ModeSetting bs) {
                settingElements.add(new DraggingModeElement(bs));
            }
        }

        draggingManager.loadSettingsFor(this);
    }

    @EventTarget
    public void onMouseClicked(EventMouseClicked event) {
        if (!(mc.currentScreen instanceof ChatScreen)) return;
        if (mc.gameSettings.showDebugInfo) return;
        Vector2f mouse = OtherUtil.getMouse((int) event.getMouseX(), (int) event.getMouseY());
        float mx = mouse.getX();
        float my = mouse.getY();

        if (settingsVisible && event.getKey() == 0) {
            float elemW = 75f;
            float offset = 24f;
            for (Element element : settingElements) {
                if (element instanceof DraggingBooleanElement dbe) {
                    float textWidth = Fonts.sf_medium[12].getWidth(dbe.getSetting().getName());
                    elemW = Math.max(elemW, textWidth + offset);
                } else if (element instanceof DraggingModeElement dme) {
                    float textWidth = Fonts.sf_medium[12].getWidth(dme.getSetting().getName());
                    elemW = Math.max(elemW, textWidth + offset);
                }
            }
            float totalHeight = 2f;
            for (Element element : settingElements) {
                totalHeight += element.getHeight();
            }

            if (!MathUtil.isHovered(mx, my, settingsX, settingsY, elemW, totalHeight)) {
                settingsVisible = false;
                event.setCancelled(true);
                return;
            }

            for (Element element : settingElements) {
                if (element.isHovered(mx, my)) {
                    element.mouseClicked(mx, my, event.getKey());
                    event.setCancelled(true);
                    return;
                }
            }
        }

        if (isAnySettingsOpen() && !settingsVisible) {
            if (event.getKey() != 1) return;
        }

        if (event.getKey() == 1 && isInside(mx, my)) {
            if (!isTopmostUnderMouse(mx, my)) {
                return;
            }
            for (Dragging d : draggingManager.getRenderOrder()) {
                if (d != this && d.settingsVisible) {
                    d.settingsVisible = false;
                }
            }
            if (!settingsVisible) {
                settingsOffsetX = mx - x;
                settingsOffsetY = my - y;
                settingsX = x + settingsOffsetX;
                settingsY = y + settingsOffsetY;
                scaleAnimation.setValue(0f);
            }
            settingsVisible = !settingsVisible;
            event.setCancelled(true);
            return;
        }

        if (draggable && event.getKey() == 0 && currentlyDragging == null) {
            for (Dragging draggable : draggingManager.getRenderOrder()) {
                if (!draggable.isVisible() || !draggable.isInside(mx, my)) continue;
                if (draggable == this) {
                    settingsVisible = false;
                    isDragging = true;
                    currentlyDragging = this;
                    offsetX = mx - x;
                    offsetY = my - y;
                    draggingManager.bringToFront(this);
                    event.setCancelled(true);
                    return;
                }
            }
        }

        if (draggable && event.getKey() == 2 && isInside(mx, my)) {
            x = initialX;
            y = initialY;
            draggingManager.save();
            event.setCancelled(true);
        }
    }


    @EventTarget
    public void onMouseReleased(EventMouseReleased event) {
        if (draggable && mc.currentScreen instanceof ChatScreen && !mc.gameSettings.showDebugInfo && event.getKey() == 0 && isDragging) {
            isDragging = false;
            currentlyDragging = null;
            draggingManager.save();
            event.setCancelled(true);
        }
    }

    private boolean isInside(float mouseX, float mouseY) {
        return MathUtil.isHovered(mouseX, mouseY, x, y, width, height);
    }

    private boolean isVisible() {
        return elements == null || elements.is(name);
    }

    public void updatePosition(int mouseX, int mouseY) {
        if (!draggable) return;
        if (!isDragging) return;

        int screenWidth = mc.getMainWindow().getScaledWidth();
        int screenHeight = mc.getMainWindow().getScaledHeight();

        float maxX = Math.max(0f, screenWidth - width);
        float maxY = Math.max(0f, screenHeight - height);

        x = MathHelper.clamp(mouseX - offsetX, 0f, maxX);
        y = MathHelper.clamp(mouseY - offsetY, 0f, maxY);

        if (scaleAnimation.getValue() > 0f) {
            settingsX = x + settingsOffsetX;
            settingsY = y + settingsOffsetY;
        }
    }


    @EventTarget
    public void onEvent(EventDragging e) {
        Vector2f mouse = OtherUtil.getMouse((int) e.getMouseX(), (int) e.getMouseY());
        float mx = mouse.x, my = mouse.y;
        boolean chatOpen = mc.currentScreen instanceof ChatScreen && hynix.getInstance().getModuleManager().getModule(Interface.class).isEnabled() && !mc.gameSettings.showDebugInfo;
        mc.gameRenderer.setupOverlayRendering(2);
        if (chatOpen) {
            updatePosition((int) mx, (int) my);
        }
        mc.gameRenderer.setupOverlayRendering();
    }

    @EventTarget
    public void onEvent(EventRender2D.Send e) {
        Vector2f mouse = OtherUtil.getMouse((int) mc.mouseHelper.getMouseX(), (int) mc.mouseHelper.getMouseY());
        float mx = mouse.x / 2, my = mouse.y / 2;
        if (!draggable && name.equals("Уведомления")) {
            float width = Fonts.icons[16].getWidth("K") + Fonts.sf_regular[12].getWidth("Это уведомление, кликни на меня для настройки") + 28;
            x = (mc.getMainWindow().getScaledWidth() - width) / 2f;
            y = mc.getMainWindow().getScaledHeight() / 2f + 13f;
        }
        boolean chatOpen = mc.currentScreen instanceof ChatScreen && hynix.getInstance().getModuleManager().getModule(Interface.class).isEnabled() && !mc.gameSettings.showDebugInfo;
        if (!chatOpen) {
            settingsVisible = false;
            if (isDragging) {
                isDragging = false;
                currentlyDragging = null;
                draggingManager.save();
            }
        }

        Dragging topmostHovered = null;
        if (chatOpen && !isDragging) {
            for (Dragging d : draggingManager.getRenderOrder()) {
                if (d == null) continue;
                boolean dVisible = (d.elements == null || d.elements.is(d.name));
                if (!dVisible) continue;
                if (MathUtil.isHovered(mx, my, d.x, d.y, d.width, d.height)) {
                    topmostHovered = d;
                }
            }
        }

        isHovered = chatOpen && !isDragging && topmostHovered == this && !isAnySettingsOpen() && !settingsVisible;
        textAlphaAnimation.update(isHovered ? 1.0f : 0.0f);
        if (textAlphaAnimation.getValue() > 0f && isVisible()) {
            renderTooltip(e);
        }
        scaleAnimation.update(settingsVisible ? 1f : 0f);
        if (scaleAnimation.getValue() > 0f && !settingElements.isEmpty()) {
            renderSettingsElements(e, mx, my);
        }
    }

    private void renderTooltip(EventRender2D.Send e) {
        if (!draggable) {
            String settingsText = "ПКМ - Дополнительные настройки";
            float textHeight = Fonts.sf_regular[11].getHeight();
            float settingsTextWidth = Fonts.sf_regular[11].getWidth(settingsText);
            float screenWidth = mc.getMainWindow().getScaledWidth();
            float screenHeight = mc.getMainWindow().getScaledHeight();
            float screenCenterY = screenHeight / 2.0f;
            float textX = x + 2;
            float textY = y + height < screenCenterY ? y + height + 2.5f : y - textHeight - 1;
            textX = Math.min(textX, screenWidth - settingsTextWidth);
            textY = MathHelper.clamp(textY, 0, screenHeight - textHeight);
            float alpha = (ThemeEditor.getAlpha(ThemeSettings.TOOLTIP) / 255f) * textAlphaAnimation.getValue();
            int color = ColorUtil.applyOpacity(ThemeEditor.getColor(ThemeSettings.TOOLTIP), alpha);
            Fonts.sf_regular[11].drawString(e.getStack(), settingsText, textX, textY, color);
        } else {
            String resetText = "СКМ - Сбросить расположение";
            String settingsText = "ПКМ - Дополнительные настройки";
            float textHeight = Fonts.sf_regular[11].getHeight();
            float resetTextWidth = Fonts.sf_regular[11].getWidth(resetText);
            float settingsTextWidth = Fonts.sf_regular[11].getWidth(settingsText);

            float screenWidth = mc.getMainWindow().getScaledWidth();
            float screenHeight = mc.getMainWindow().getScaledHeight();
            float screenCenterY = screenHeight / 2.0f;

            float textX = x + 2;
            float textY = y + height < screenCenterY ? y + height + 2.5f : y - textHeight - 1;

            textX = Math.min(textX, screenWidth - Math.max(resetTextWidth, settingsTextWidth));
            textY = MathHelper.clamp(textY, 0, screenHeight - textHeight);

            float settingsTextY = y + height < screenCenterY ? textY + textHeight + 2.5f : textY - textHeight - 2.5f;
            settingsTextY += (1.0f - textAlphaAnimation.getValue()) * (y + height < screenCenterY ? -textHeight - 2 : textHeight + 2);
            settingsTextY = MathHelper.clamp(settingsTextY, 0, screenHeight - textHeight);

            float alpha = (ThemeEditor.getAlpha(ThemeSettings.TOOLTIP) / 255f) * textAlphaAnimation.getValue();
            int color = ColorUtil.applyOpacity(ThemeEditor.getColor(ThemeSettings.TOOLTIP), alpha);

            Fonts.sf_regular[11].drawString(e.getStack(), resetText, textX, textY, color);
            Fonts.sf_regular[11].drawString(e.getStack(), settingsText, textX, settingsTextY, color);
        }
    }

    private void renderSettingsElements(EventRender2D.Send e, float mouseX, float mouseY) {
        float elemW = 75f;
        float offset = 24f;
        for (Element element : settingElements) {
            if (element instanceof DraggingBooleanElement dbe) {
                float textWidth = Fonts.sf_medium[12].getWidth(dbe.getSetting().getName());
                elemW = Math.max(elemW, textWidth + offset);
            } else if (element instanceof DraggingModeElement dme) {
                float textWidth = Fonts.sf_medium[12].getWidth(dme.getSetting().getName());
                elemW = Math.max(elemW, textWidth + offset);
            }
        }
        float totalHeight = 4f;
        for (Element element : settingElements) {
            totalHeight += element.getHeight();
        }
        RenderUtil.scaleStart(settingsX + elemW / 2.0f, settingsY + totalHeight / 2.0f, scaleAnimation.getValue());

        RenderUtil.drawRoundedRectangleGradientGlowed(settingsX - 3, settingsY - 3, elemW + 6, totalHeight + 6, 2, ThemeEditor.getColor(ThemeSettings.OUTLINE), ThemeEditor.getColor(ThemeSettings.OUTLINE), ThemeEditor.getColor(ThemeSettings.OUTLINE), ThemeEditor.getColor(ThemeSettings.OUTLINE), 1, 4);
        RenderUtil.drawBlurredRoundedRectangle(settingsX, settingsY, elemW, totalHeight, 2, ThemeEditor.getColor(ThemeSettings.WINDOW_BG), 1);

        float currentY = settingsY + 1;
        for (Element element : settingElements) {
            element.setX(settingsX);
            element.setY(currentY);
            element.setWidth(elemW);
            element.render(e.getStack(), mouseX, mouseY, 1f);
            currentY += element.getHeight();
        }

        RenderUtil.scaleEnd();
    }

    private boolean isAnySettingsOpen() {
        for (Dragging d : draggingManager.getRenderOrder()) {
            if (d == null) continue;
            if (d.settingsVisible) return true;
        }
        return false;
    }

    private boolean isTopmostUnderMouse(float mouseX, float mouseY) {
        Dragging candidate = null;
        for (Dragging d : draggingManager.getRenderOrder()) {
            if (d == null) continue;
            boolean dVisible = (d.elements == null || d.elements.is(d.name));
            if (!dVisible) continue;
            if (MathUtil.isHovered(mouseX, mouseY, d.x, d.y, d.width, d.height)) {
                candidate = d;
            }
        }
        return candidate == this;
    }
}