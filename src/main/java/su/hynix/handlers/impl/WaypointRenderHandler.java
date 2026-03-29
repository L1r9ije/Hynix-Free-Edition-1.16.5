package su.hynix.handlers.impl;

import com.darkmagician6.eventapi.EventTarget;
import net.minecraft.util.math.vector.Vector3d;
import org.joml.Vector2f;
import su.hynix.events.EventRender2D;
import su.hynix.hynix;
import su.hynix.managers.impl.WaypointManager;
import su.hynix.utils.Wrapper;
import su.hynix.utils.render.ColorUtil;
import su.hynix.utils.render.ProjectUtil;
import su.hynix.utils.render.font.Fonts;

import java.util.List;

public class WaypointRenderHandler implements Wrapper {

    @EventTarget
    public void render(EventRender2D eventRender2D) {
        if (mc.player == null) return;

        String serverKey = mc.getCurrentServerData() != null ? mc.getCurrentServerData().serverIP : "singleplayer";
        List<WaypointManager.WaypointEntry> waypoints = hynix.getInstance().getWaypointManager().getWaypoints(serverKey);
        if (waypoints.isEmpty()) return;

        double px = mc.player.getPosX();
        double pz = mc.player.getPosZ();

        for (WaypointManager.WaypointEntry wp : waypoints) {
            Vector3d worldPos = new Vector3d(wp.x(), wp.y(), wp.z());
            Vector2f screen = ProjectUtil.project2D(worldPos);
            if (Float.isInfinite(screen.x) || Float.isInfinite(screen.y) || screen.x == Float.MAX_VALUE || screen.y == Float.MAX_VALUE)
                continue;

            String icon = "B";
            String label = wp.name() + ": " + (int) Math.round(Math.hypot(px - wp.x(), pz - wp.z())) + "m";

            float iconWidth = Fonts.waypoint_icons[36].getWidth(icon);
            float iconHeight = Fonts.waypoint_icons[36].getHeight();

            float drawX = screen.x - iconWidth / 2f;
            float drawY = screen.y - iconHeight / 2f;

            Fonts.waypoint_icons[36].drawOutlineString(eventRender2D.getStack(), icon, drawX, drawY, -1, false, true, false, true, ColorUtil.getColor(0, 0, 0, 255));

            float textWidth = Fonts.sf_semibold[15].getWidth(label);
            float textX = screen.x - textWidth / 2f;
            float textY = drawY + iconHeight + 2.0f + 3;
            Fonts.sf_semibold[15].drawOutlineString(eventRender2D.getStack(), label, textX, textY, -1, false, true, false, true, ColorUtil.getColor(0, 0, 0, 255));
        }
    }
}