package su.hynix.utils.math;


public class TimeUtil {
    private final long ms = System.currentTimeMillis();
    private long lastMS;

    public TimeUtil() {
        reset();
    }

    public static void addTask(long delay, Runnable task) {
        (new Thread(() -> {
            try {
                Thread.sleep(delay);
                task.run();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        })).start();
    }

    public void reset() {
        lastMS = System.currentTimeMillis();
    }

    public long getTimePassed() {
        return System.currentTimeMillis() - lastMS;
    }

    public boolean isReached(long time) {
        return System.currentTimeMillis() - lastMS > time;
    }

    public boolean hasTimeElapsed(long time) {
        return getTimePassed() >= time;
    }

    public boolean finished(long delay) {
        return System.currentTimeMillis() - delay >= lastMS;
    }

    public long getElapsedTime() {
        return System.currentTimeMillis() - this.lastMS;
    }

    public boolean hasTimeElapsed(long time, boolean reset) {
        boolean hasElapsed = getTimePassed() >= time;
        if (hasElapsed && reset) {
            reset();
        }
        return hasElapsed;
    }

    public long getTime() {
        return System.currentTimeMillis() - this.lastMS;
    }

    public void setTime(long time) {
        lastMS = time;
    }

    public boolean hasTimeElapsed() {
        return ms < System.currentTimeMillis();
    }

    public boolean hasReached(double milliseconds) {
        return getTimePassed() >= milliseconds;
    }

}