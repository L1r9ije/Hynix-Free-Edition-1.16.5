package su.hynix.modules.impl.combat;

import com.darkmagician6.eventapi.EventTarget;
import su.hynix.events.EventEntityRayTrace;
import su.hynix.modules.Category;
import su.hynix.modules.Module;

public class NoEntityTrace extends Module {

    public NoEntityTrace() {
        super("NoEntityTrace", "Отключает взаимодействие с сущностями", Category.Combat);
    }

    @EventTarget
    public void onEvent(EventEntityRayTrace event) {
        event.isCancelled();
    }
}
