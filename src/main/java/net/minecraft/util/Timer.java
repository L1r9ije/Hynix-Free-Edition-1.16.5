package net.minecraft.util;

import com.darkmagician6.eventapi.EventManager;
import su.hynix.events.EventTimer;
import lombok.Getter;
import lombok.Setter;

public class Timer {
    private final float tickLength;
    public float renderPartialTicks;
    public float elapsedPartialTicks;
    private long lastSyncSysClock;
    @Getter
    @Setter
    private float speed = 1F;

    public Timer(float ticks, long lastSyncSysClock) {
        this.tickLength = 1000.0F / ticks;
        this.lastSyncSysClock = lastSyncSysClock;
    }

    public void resetSpeed() {
        setSpeed(1F);
    }

    public int getPartialTicks(long gameTime) {
        final EventTimer event = new EventTimer(gameTime);
        EventManager.call(event);

        this.elapsedPartialTicks = ((float) (gameTime - this.lastSyncSysClock) / this.tickLength) * speed;
        this.lastSyncSysClock = gameTime;
        this.renderPartialTicks += this.elapsedPartialTicks;
        int i = (int) this.renderPartialTicks;
        this.renderPartialTicks -= (float) i;
        return i;
    }
}
