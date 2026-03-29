package su.hynix.managers.impl.notificationmanager.impl;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import su.hynix.managers.impl.notificationmanager.AbstractNotification;
import su.hynix.ui.Interface.elements.impl.NotificationRender;
import su.hynix.ui.gui.themes.ThemeEditor;
import su.hynix.ui.gui.themes.ThemeSettings;
import su.hynix.utils.render.ColorUtil;
import su.hynix.utils.render.RenderUtil;
import su.hynix.utils.render.font.Fonts;

public class ImageNotification extends AbstractNotification {
    private final ResourceLocation image;
    private final Integer overrideColor;

    public ImageNotification(ResourceLocation image, ITextComponent message) {
        super(message);
        this.image = image;
        this.overrideColor = null;
    }

    public ImageNotification(ResourceLocation image, ITextComponent message, int overrideColor) {
        super(message);
        this.image = image;
        this.overrideColor = overrideColor;
    }

    @Override
    public void render(float x, float y, MatrixStack matrixStack) {
        initYIfNeeded(y);
        boolean expired = isExpired();
        alphaAnimation.update(expired ? 0f : 1f);
        yAnimation.update((forceExpire && expired) ? y - 4.0f : y);
        float animatedY = yAnimation.getValue();
        float animatedAlpha = alphaAnimation.getValue();

        RenderUtil.drawBlurredRoundedRectangle(x, animatedY, Fonts.sf_semibold[12].getWidth(message.getString()) + 24.0f, 13, 2, NotificationRender.alphabg.get() ? ColorUtil.applyOpacity(ThemeEditor.getColor(ThemeSettings.WINDOW_BG), 0) : ThemeEditor.getColor(ThemeSettings.WINDOW_BG), animatedAlpha);
        int offsetX = (int) (x + 4.0f);
        int offsetY = (int) (animatedY + 2.5f);
        if (overrideColor != null && image.getPath().contains("potion")) {
            int colored = ColorUtil.applyOpacity(overrideColor, animatedAlpha);
            RenderUtil.drawPotionLiquid(colored, offsetX, offsetY, 8, 8);
        }
        RenderUtil.drawImage2D(image, offsetX, offsetY, 8, 8, ColorUtil.applyOpacity(0xFFFFFFFF, animatedAlpha));
        RenderUtil.drawMinecraftRectangle(matrixStack, (int) x + 16, animatedY + 3, 0.5f, 7, ColorUtil.applyOpacity(ThemeEditor.getColor(ThemeSettings.SEPARATOR), (ThemeEditor.getAlpha(ThemeSettings.SEPARATOR) / 255f) * animatedAlpha));
        Fonts.sf_semibold[12].drawText(matrixStack, message, (int) x + 20.0f, animatedY + Fonts.sf_semibold[12].getHeight() + 1.5f, ColorUtil.applyOpacity(-1, 1 * animatedAlpha));
    }
}