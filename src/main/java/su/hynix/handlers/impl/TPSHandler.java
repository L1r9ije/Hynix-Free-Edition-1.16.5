package su.hynix.handlers.impl;

import com.darkmagician6.eventapi.EventTarget;
import lombok.Getter;
import net.minecraft.network.play.server.SUpdateTimePacket;
import net.minecraft.util.math.MathHelper;
import su.hynix.events.EventPacket;

public class TPSHandler {
    @Getter
    private static float adjustTicks = 0;
    @Getter
    private static float TPS = 20;
    long timestamp;

    @EventTarget
    private void onPacket(EventPacket e) {
        if (e.getPacket() instanceof SUpdateTimePacket) updateTPS();
    }

    private void updateTPS() {
        long delay = System.nanoTime() - timestamp;
        float maxTPS = 20;
        float rawTPS = maxTPS * (1e9f / delay);
        float boundedTPS = MathHelper.clamp(rawTPS, 0, maxTPS);
        TPS = Math.round(boundedTPS * 2) / 2.0f;
        adjustTicks = boundedTPS - maxTPS;
        timestamp = System.nanoTime();
    }
}
