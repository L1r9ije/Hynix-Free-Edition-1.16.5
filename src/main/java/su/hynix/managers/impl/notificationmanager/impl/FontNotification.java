package su.hynix.managers.impl.notificationmanager.impl;

import com.mojang.blaze3d.matrix.MatrixStack;
import lombok.Getter;
import net.minecraft.util.text.StringTextComponent;
import su.hynix.managers.impl.notificationmanager.AbstractNotification;
import su.hynix.ui.Interface.elements.impl.NotificationRender;
import su.hynix.ui.gui.themes.ThemeEditor;
import su.hynix.ui.gui.themes.ThemeSettings;
import su.hynix.utils.render.ColorUtil;
import su.hynix.utils.render.RenderUtil;
import su.hynix.utils.render.font.Fonts;

@Getter
public class FontNotification extends AbstractNotification {
    private final String title;
    private final int iconIndex = 0;
    private final Integer color;

    public FontNotification(String title, String message, int color) {
        super(new StringTextComponent(message));
        this.title = title;
        this.color = color;
    }

    @Override
    public void render(float x, float y, MatrixStack matrixStack) {
        initYIfNeeded(y);
        boolean expired = isExpired();
        alphaAnimation.update(expired ? 0f : 1f);
        yAnimation.update((forceExpire && expired) ? y - 2.0f : y);

        float animatedY = yAnimation.getValue();
        float animatedAlpha = alphaAnimation.getValue();

        float width = Fonts.hynix_icons[16].getWidth(title) + Fonts.sf_semibold[12].getWidth(message.getString()) + 5;
        int iconColor = switch (title) {
            case "I", "L" -> ColorUtil.applyOpacity(0xFF0000, animatedAlpha);
            case "H" -> ColorUtil.applyOpacity(0x00FF00, animatedAlpha);
            case "M" -> ColorUtil.applyOpacity(0xFFA500, animatedAlpha);
            default -> ColorUtil.applyOpacity(0xFFFFFF, animatedAlpha);
        };
        RenderUtil.drawBlurredRoundedRectangle(x, animatedY, width + 11.5f, 13, 2, NotificationRender.alphabg.get() ? ColorUtil.applyOpacity(ThemeEditor.getColor(ThemeSettings.WINDOW_BG), 0) : ThemeEditor.getColor(ThemeSettings.WINDOW_BG), animatedAlpha);
        Fonts.hynix_icons[16].drawString(matrixStack, title, (int) x + 5, animatedY + 2 + Fonts.hynix_icons[16].getHeight() - 0.5f, iconColor);
        //       RenderUtil.drawMinecraftRectangle(matrixStack, (int) x + Fonts.hynix_icons[16].getWidth(title) + 8.5f, animatedY + 3, 0.5f, 7, ColorUtil.applyOpacity(ThemeEditor.getColor(ThemeSettings.SEPARATOR), (ThemeEditor.getAlpha(ThemeSettings.SEPARATOR) / 255f) * animatedAlpha));
        Fonts.sf_semibold[12].drawString(matrixStack, message.getString(), (int) x + Fonts.hynix_icons[16].getWidth(title) + 12, animatedY + Fonts.sf_semibold[12].getHeight() + 1.5f, ColorUtil.applyOpacity(color, animatedAlpha));
    }
}