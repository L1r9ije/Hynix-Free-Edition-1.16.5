package su.hynix.modules.impl.miscellaneous;

import com.darkmagician6.eventapi.EventTarget;
import su.hynix.events.EventUpdate;
import su.hynix.modules.Category;
import su.hynix.modules.Module;
import su.hynix.modules.api.constructors.impl.ModeSetting;
import su.hynix.modules.api.constructors.impl.SliderSetting;

public class TapeMouse extends Module {

    public static ModeSetting modeClick = new ModeSetting("Клавиша", "Левая", "Левая", "Правая");
    public static SliderSetting delayForClick = new SliderSetting("Задержка", 1, 1, 30, 1);

    private int tickCounter = 0;

    public TapeMouse() {
        super("Tape Mouse", "Автоматически воспроизводит клик мыши ", Category.Miscellaneous);
        addSettings(modeClick, delayForClick);
    }

    @EventTarget
    public void onUpdate(EventUpdate event) {
        tickCounter++;

        if (tickCounter >= delayForClick.get()) {
            if (modeClick.is("Левая")) {
                mc.clickMouse();
            } else if (modeClick.is("Правая")) {
                mc.rightClickMouse();
            }
            tickCounter = 0;
        }
    }
}
