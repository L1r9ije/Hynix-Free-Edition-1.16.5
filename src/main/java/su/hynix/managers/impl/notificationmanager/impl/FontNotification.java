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

        float width = Fonts.sf_semibold[12].getWidth(message.getString()) + 10f;
        RenderUtil.drawBlurredRoundedRectangle(x, animatedY, width, 13, 2, NotificationRender.alphabg.get() ? ColorUtil.applyOpacity(ThemeEditor.getColor(ThemeSettings.WINDOW_BG), 0) : ThemeEditor.getColor(ThemeSettings.WINDOW_BG), animatedAlpha);
        Fonts.sf_semibold[12].drawString(matrixStack, message.getString(), (int) x + 5, animatedY + Fonts.sf_semibold[12].getHeight() + 1.5f, ColorUtil.applyOpacity(color, animatedAlpha));
    }
}