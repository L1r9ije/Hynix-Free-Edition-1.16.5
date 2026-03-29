package su.hynix.handlers.impl;

import com.darkmagician6.eventapi.EventTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.util.ResourceLocation;
import su.hynix.commands.impl.GpsCommand;
import su.hynix.events.EventRender2D;
import su.hynix.utils.Wrapper;
import su.hynix.utils.render.RenderUtil;
import su.hynix.utils.render.font.Fonts;

public class GpsRenderHandler implements Wrapper {

    @EventTarget
    public void render(EventRender2D eventRender2D) {
        if (!GpsCommand.gpsEnabled || mc.player == null) return;

        float arrowSize = 24;

        float yaw = (float) (Math.toDegrees(Math.atan2(GpsCommand.z - mc.player.getPosZ(), GpsCommand.x - mc.player.getPosX())) - mc.player.rotationYaw - 180);
        int dst = (int) Math.sqrt(Math.pow(GpsCommand.x - mc.player.getPosX(), 2) + Math.pow(GpsCommand.z - mc.player.getPosZ(), 2));

        int screenWidth = mc.getMainWindow().getScaledWidth();
        int screenHeight = mc.getMainWindow().getScaledHeight();

        float drawX = (screenWidth - arrowSize) / 2;
        float drawY = screenHeight / 2f - 220;

        RenderSystem.pushMatrix();
        RenderSystem.translatef(drawX + arrowSize / 2, drawY + arrowSize / 2, 0);
        RenderSystem.rotatef(yaw, 0, 0, 1);
        RenderSystem.translatef(-arrowSize / 2, -arrowSize / 2, 0);
        RenderUtil.drawImage2D(new ResourceLocation("hynix/icons/world_render/gps.png"), 0, 0, arrowSize, arrowSize, -1);
        RenderSystem.popMatrix();

        String text = dst + "m";
        float textWidth = Fonts.sf_medium[14].getWidth(text);

        float centerX = drawX + arrowSize / 2;
        float textY = drawY;

        float textX = centerX - textWidth / 2f;

        Fonts.sf_medium[14].drawString(eventRender2D.getStack(), text, textX, textY, -1);
    }
}