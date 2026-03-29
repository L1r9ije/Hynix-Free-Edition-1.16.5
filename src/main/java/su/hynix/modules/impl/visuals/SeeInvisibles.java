package su.hynix.modules.impl.visuals;

import lombok.Getter;
import su.hynix.modules.Category;
import su.hynix.modules.Module;
import su.hynix.modules.api.constructors.impl.BooleanSetting;
import su.hynix.modules.api.constructors.impl.SliderSetting;

@Getter
public class SeeInvisibles extends Module {

    private final SliderSetting alpha = new SliderSetting("Прозрачность", 0.5F, 0.3F, 1.0F, 0.1F);
    private final BooleanSetting armorStands = new BooleanSetting("Отображать стойки брони", false);

    public SeeInvisibles() {
        super("See Invisibles", "Позволяет изменить прозрачность невидимых игроков, облегчая их обнаружение", Category.Visuals);
        addSettings(alpha, armorStands);
    }
}