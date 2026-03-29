package su.hynix.ui.Interface.elements.impl;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.util.ResourceLocation;
import su.hynix.events.EventRender2D;
import su.hynix.managers.impl.dragmanager.Dragging;
import su.hynix.modules.api.constructors.impl.BooleanSetting;
import su.hynix.modules.api.constructors.impl.ModeSetting;
import su.hynix.ui.Interface.elements.ElementRender;
import su.hynix.ui.gui.themes.ThemeEditor;
import su.hynix.ui.gui.themes.ThemeSettings;
import su.hynix.utils.render.ColorUtil;
import su.hynix.utils.render.RenderUtil;
import su.hynix.utils.render.font.Fonts;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class WatermarkRender implements ElementRender {

    private static final ResourceLocation LOGO_PNG = new ResourceLocation("minecraft", "hynix/icons/logo/logo.png");

    public static BooleanSetting login = new BooleanSetting("Логин", true);
    public static BooleanSetting fps = new BooleanSetting("Фпс", true);
    public static BooleanSetting time = new BooleanSetting("Время", true);
    public static BooleanSetting coordinates = new BooleanSetting("Координаты", true);
    public static BooleanSetting ping = new BooleanSetting("Пинг", true);
    public static BooleanSetting tps = new BooleanSetting("ТПС", true);
    public static BooleanSetting bps = new BooleanSetting("БПС", true);
    public static ModeSetting logotype = new ModeSetting("Лого", "Полное", "Полное", "Компактное");
    public static BooleanSetting alphabg = new BooleanSetting("Прозрачный фон", false);

    private final Dragging dragging;

    public WatermarkRender(Dragging dragging) {
        this.dragging = dragging;
    }

    @Override
    public void render(EventRender2D.Post event) {
        float posX = dragging.getX();
        float posY = dragging.getY();
        MatrixStack ms = event.getStack();
        float maxX = posX;

        String nameText = "HynixUser";
        String fpsText = Minecraft.debugFPS + " fps";
        String timeText = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));

        float buildTextWidth = Fonts.sf_medium[14].getWidth("DV");
        float nameTextWidth = Fonts.sf_medium[14].getWidth(nameText);
        float fpsTextWidth = Fonts.sf_medium[14].getWidth(fpsText);
        float timeTextWidth = Fonts.sf_medium[14].getWidth(timeText);

        float currentX = posX;

        boolean nameEnabled = login.get();
        boolean fpsEnabled = fps.get();
        boolean timeEnabled = time.get();

        float buildRectWidth = buildTextWidth + 9.0f;
        float combinedRectWidth = 0.0f;
        if (nameEnabled) combinedRectWidth += (2.0f + Fonts.icons[14].getWidth("W") + 3.0f + nameTextWidth);
        if (fpsEnabled) combinedRectWidth += (5.0f + Fonts.icons[14].getWidth("X") + 3.0f + fpsTextWidth);
        if (timeEnabled) combinedRectWidth += (5.0f + Fonts.icons[14].getWidth("V") + 3.0f + timeTextWidth);


        RenderUtil.drawBlurredRoundedRectangle(posX - 0.5f, posY - 0.5f, buildRectWidth + 1 + combinedRectWidth, 15.0f, 4.5f, alphabg.get() ? ColorUtil.applyOpacity(ThemeEditor.getColor(ThemeSettings.WINDOW_BG), 0) : ThemeEditor.getColor(ThemeSettings.WINDOW_BG), 1);


        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        Minecraft.getInstance().getTextureManager().bindTexture(LOGO_PNG);


        AbstractGui.blit(ms, (int) (posX + 3), (int) (posY + 2.5f), 0, 0, 10, 10, 10, 10);
        RenderSystem.disableBlend();
        // --------------------------------------

        maxX = Math.max(maxX, posX + buildRectWidth);
        currentX += buildRectWidth + 2.0f;

        if (nameEnabled || fpsEnabled || timeEnabled) {
            float subBlockX = buildTextWidth + 12.0f;

            if (nameEnabled) {
                Fonts.icons[14].drawString(ms, "W", subBlockX, posY + 6.5f, ThemeEditor.getColor(ThemeSettings.LOGO));
                subBlockX += Fonts.icons[14].getWidth("W") + 3.0f;
                Fonts.sf_medium[14].drawString(ms, nameText, subBlockX - 0.5f, posY + 5.5f, ThemeEditor.getColor(ThemeSettings.LOGO_TEXT));
                subBlockX += nameTextWidth + 5.0f;
            }

            if (fpsEnabled) {
                Fonts.icons[14].drawString(ms, "X", subBlockX, posY + 6.5f, ThemeEditor.getColor(ThemeSettings.LOGO));
                subBlockX += Fonts.icons[14].getWidth("X") + 3.0f;
                Fonts.sf_medium[14].drawString(ms, fpsText, subBlockX - 0.5f, posY + 5.5f, ThemeEditor.getColor(ThemeSettings.LOGO_TEXT));
                subBlockX += fpsTextWidth + 5.0f;
            }

            if (timeEnabled) {
                Fonts.icons[14].drawString(ms, "V", subBlockX, posY + 6.5f, ThemeEditor.getColor(ThemeSettings.LOGO));
                subBlockX += Fonts.icons[14].getWidth("V") + 3.0f;
                Fonts.sf_medium[14].drawString(ms, timeText, subBlockX - 0.5f, posY + 5.5f, ThemeEditor.getColor(ThemeSettings.LOGO_TEXT));
            }
        }

        dragging.setWidth(maxX - posX);
        dragging.setHeight(14);
    }
}