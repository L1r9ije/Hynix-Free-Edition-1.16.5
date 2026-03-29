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
import su.hynix.utils.math.TimeUtil;
import su.hynix.utils.render.ColorUtil;
import su.hynix.utils.render.RenderUtil;
import su.hynix.utils.render.font.Fonts;

@RequiredArgsConstructor
public class NotificationRender implements ElementRender {
    private static final String[] ICON_SEQUENCE = {"G", "S", "J", "K", "I", "H", "M", "L"};
    public static BooleanSetting shield = new BooleanSetting("Ломание щита", false);
    public static BooleanSetting spec = new BooleanSetting("Просьба о наблюдении", false);
    public static BooleanSetting warps = new BooleanSetting("Пиар варпов", false);
    public static BooleanSetting module = new BooleanSetting("Состояние модулей/настроек", false);
    public static BooleanSetting lowstrength = new BooleanSetting("Низкая прочность предметов", false);
    public static BooleanSetting effects = new BooleanSetting("Закончились важные зелья", false);
    public static BooleanSetting alphabg = new BooleanSetting("Прозрачный фон", false);
    private final Dragging dragging;
    private final AnimationUtil alphaAnimation = new AnimationUtil(0.0f, 10);
    private final TimeUtil iconTimer = new TimeUtil();
    private int iconIndex = 0;
    private boolean wasChatOpen = false;

    @Override
    public void render(EventRender2D.Post event) {
        MatrixStack ms = event.getStack();
        boolean isChatOpen = mc.currentScreen instanceof ChatScreen;
        boolean shouldShow = isChatOpen && !NotificationManager.hasActiveNotifications();
        float targetAlpha = shouldShow ? 1.0f : 0.0f;
        if (isChatOpen && !wasChatOpen) {
            iconIndex = 0;
            iconTimer.reset();
        }
        wasChatOpen = isChatOpen;


        alphaAnimation.update(targetAlpha);
        float globalAlpha = alphaAnimation.getValue();

        if (isChatOpen) {
            if (iconTimer.hasTimeElapsed(500, true)) {
                iconIndex = (iconIndex + 1) % ICON_SEQUENCE.length;
            }
        }
        String icon = ICON_SEQUENCE[iconIndex];
        String preview = "Тыкни,чтобы настроить";

        int textColor = ColorUtil.applyOpacity(ThemeEditor.getColor(ThemeSettings.WINDOW_TEXT), (ThemeEditor.getAlpha(ThemeSettings.WINDOW_TEXT) / 255f) * globalAlpha);
        int separatorColor = ColorUtil.applyOpacity(ThemeEditor.getColor(ThemeSettings.SEPARATOR), (ThemeEditor.getAlpha(ThemeSettings.SEPARATOR) / 255f) * globalAlpha);
        float width = Fonts.icons[16].getWidth(icon) + Fonts.sf_semibold[12].getWidth(preview) + 5;
        float totalWidth = width - 6.5f;
        float posX = (mc.getMainWindow().getScaledWidth() - totalWidth) / 2f;
        float posY = mc.getMainWindow().getScaledHeight() / 2f + 13f;
        RenderUtil.drawBlurredRoundedRectangle(posX, posY, totalWidth, 13, 2, alphabg.get() ? ColorUtil.applyOpacity(ThemeEditor.getColor(ThemeSettings.WINDOW_BG), 0) : ThemeEditor.getColor(ThemeSettings.WINDOW_BG), globalAlpha);
        int iconColor = switch (icon) {
            case "I", "L" -> ColorUtil.applyOpacity(0xFF0000, globalAlpha);
            case "H" -> ColorUtil.applyOpacity(0x00FF00, globalAlpha);
            case "M" -> ColorUtil.applyOpacity(0xFFA500, globalAlpha);
            default -> ColorUtil.applyOpacity(0xFFFFFF, globalAlpha);
        };
        //  Fonts.icons[16].drawString(ms, icon, posX + 5, posY + Fonts.icons[16].getHeight() - 0.5f, iconColor);
        //    RenderUtil.drawMinecraftRectangle(ms, posX + Fonts.icons[16].getWidth(icon) + 8.5f, posY + 3, 0.5f, 7, separatorColor);
        Fonts.sf_semibold[12].drawString(ms, preview, posX + Fonts.icons[16].getWidth(icon) - 5, posY + Fonts.sf_semibold[12].getHeight() + 1.5f, textColor);

        if (alphaAnimation.getTarget() >= 0.999f) {
            dragging.setHeight(13);
            dragging.setWidth(totalWidth);
        } else {
            dragging.setHeight(0);
            dragging.setWidth(0);
        }
    }
}
