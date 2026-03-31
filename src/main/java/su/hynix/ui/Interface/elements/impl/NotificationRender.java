package su.hynix.ui.Interface.elements.impl;

import com.mojang.blaze3d.matrix.MatrixStack;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.gui.screen.ChatScreen;
import su.hynix.events.EventRender2D;
import su.hynix.managers.impl.dragmanager.Dragging;
import su.hynix.managers.impl.notificationmanager.NotificationManager;
import su.hynix.modules.api.constructors.impl.BooleanSetting;
import su.hynix.ui.Interface.elements.ElementRender;
import su.hynix.ui.gui.themes.ThemeEditor;
import su.hynix.ui.gui.themes.ThemeSettings;
import su.hynix.utils.animation.AnimationUtil;
import su.hynix.utils.render.ColorUtil;
import su.hynix.utils.render.RenderUtil;
import su.hynix.utils.render.font.Fonts;

@RequiredArgsConstructor
public class NotificationRender implements ElementRender {

    public static BooleanSetting shield = new BooleanSetting("Ломание щита", false);
    public static BooleanSetting spec = new BooleanSetting("Просьба о наблюдении", false);
    public static BooleanSetting warps = new BooleanSetting("Пиар варпов", false);
    public static BooleanSetting module = new BooleanSetting("Состояние модулей/настроек", false);
    public static BooleanSetting lowstrength = new BooleanSetting("Низкая прочность предметов", false);
    public static BooleanSetting effects = new BooleanSetting("Закончились важные зелья", false);
    public static BooleanSetting alphabg = new BooleanSetting("Прозрачный фон", false);
    private final Dragging dragging;
    private final AnimationUtil alphaAnimation = new AnimationUtil(0.0f, 10);
    private boolean wasChatOpen = false;

    @Override
    public void render(EventRender2D.Post event) {
        MatrixStack ms = event.getStack();
        boolean isChatOpen = mc.currentScreen instanceof ChatScreen;
        boolean shouldShow = isChatOpen && !NotificationManager.hasActiveNotifications();
        float targetAlpha = shouldShow ? 1.0f : 0.0f;
        wasChatOpen = isChatOpen;


        alphaAnimation.update(targetAlpha);
        float globalAlpha = alphaAnimation.getValue();


        String preview = "Тыкни,чтобы настроить";

        int textColor = ColorUtil.applyOpacity(ThemeEditor.getColor(ThemeSettings.WINDOW_TEXT), (ThemeEditor.getAlpha(ThemeSettings.WINDOW_TEXT) / 255f) * globalAlpha);
        int separatorColor = ColorUtil.applyOpacity(ThemeEditor.getColor(ThemeSettings.SEPARATOR), (ThemeEditor.getAlpha(ThemeSettings.SEPARATOR) / 255f) * globalAlpha);
        float totalWidth = Fonts.sf_semibold[12].getWidth(preview) + 10f;
        float posX = (mc.getMainWindow().getScaledWidth() - totalWidth) / 2f;
        float posY = mc.getMainWindow().getScaledHeight() / 2f + 13f;
        RenderUtil.drawBlurredRoundedRectangle(posX, posY, totalWidth, 13, 2, alphabg.get() ? ColorUtil.applyOpacity(ThemeEditor.getColor(ThemeSettings.WINDOW_BG), 0) : ThemeEditor.getColor(ThemeSettings.WINDOW_BG), globalAlpha);
        Fonts.sf_semibold[12].drawString(ms, preview, posX + 5f, posY + Fonts.sf_semibold[12].getHeight() + 1.5f, textColor);

        if (alphaAnimation.getTarget() >= 0.999f) {
            dragging.setHeight(13);
            dragging.setWidth(totalWidth);
        } else {
            dragging.setHeight(0);
            dragging.setWidth(0);
        }
    }
}