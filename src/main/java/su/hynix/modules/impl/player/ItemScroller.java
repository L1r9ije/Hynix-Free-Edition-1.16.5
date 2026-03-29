package su.hynix.modules.impl.player;

import su.hynix.modules.Category;
import su.hynix.modules.Module;
import su.hynix.modules.api.constructors.impl.SliderSetting;

public class ItemScroller extends Module {

    public SliderSetting delay = new SliderSetting("Задержка", 8, 0, 10, 1);

    public ItemScroller() {
        super("Item Scroller", "Позволяет быстро перекладывать вещи в инвенторе", Category.Player);
        addSettings(delay);
    }
}
