package su.hynix.utils.player;

import lombok.Getter;
import lombok.experimental.Accessors;
import su.hynix.utils.math.TimeUtil;


@Getter
@Accessors(fluent = true)
public class PerfectDelay {
    private final TimeUtil time = new TimeUtil();
    private long delay = 0;

    public boolean cooldownComplete() {
        return cooldownComplete(this.delay);
    }

    public boolean cooldownComplete(long delay) {
        return time.finished(delay);
    }

    public void reset(long delay) {
        this.time.reset();
        this.delay = delay;
    }

}