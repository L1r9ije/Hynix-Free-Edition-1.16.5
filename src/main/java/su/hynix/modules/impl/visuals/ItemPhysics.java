package su.hynix.modules.impl.visuals;

import su.hynix.modules.Category;
import su.hynix.modules.Module;
import su.hynix.modules.api.constructors.impl.BooleanSetting;

public class ItemPhysics extends Module {
    public static BooleanSetting size = new BooleanSetting("Уменьшить предметы", false);

    public ItemPhysics() {
        super("Item Physics", "Добавляет физику предметам на земле", Category.Visuals);
        addSettings(size);
    }
}
