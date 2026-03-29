package su.hynix.modules.impl.movement;

import com.darkmagician6.eventapi.EventTarget;
import su.hynix.events.EventNoPush;
import su.hynix.modules.Category;
import su.hynix.modules.Module;
import su.hynix.modules.api.constructors.impl.BooleanSetting;
import su.hynix.modules.api.constructors.impl.MultiBooleanSetting;

public class NoPush extends Module {

    public MultiBooleanSetting mode = new MultiBooleanSetting("Тип", new BooleanSetting("Игроки", true), new BooleanSetting("Блоки", true), new BooleanSetting("Вода", true), new BooleanSetting("Удочки", true));

    public NoPush() {
        super("No Push", "Позволяет не отталкиваться при определенных условиях, обеспечивая стабильное передвижение", Category.Movement);
        addSettings(mode);
    }

    @EventTarget
    public void onEvent(EventNoPush event) {
        boolean cancel = switch (event.noPushType) {
            case Block -> mode.is("Блоки");
            case Water -> mode.is("Вода");
            case Player -> mode.is("Игроки");
            case FishingRod -> mode.is("Удочки");
        };


        if (cancel) {
            event.setCancelled(true);
        }
    }
}
