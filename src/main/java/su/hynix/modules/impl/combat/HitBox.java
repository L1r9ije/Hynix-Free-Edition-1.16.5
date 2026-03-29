package su.hynix.modules.impl.combat;

import com.darkmagician6.eventapi.EventTarget;
import net.minecraft.entity.LivingEntity;
import su.hynix.events.EventEntityHitBox;
import su.hynix.modules.Category;
import su.hynix.modules.Module;
import su.hynix.modules.api.constructors.impl.BooleanSetting;
import su.hynix.modules.api.constructors.impl.SliderSetting;

public class HitBox extends Module {
    public static final SliderSetting size = new SliderSetting("Размер", 0.2f, 0F, 1, 0.05F);
    private final BooleanSetting showHitBox = new BooleanSetting("Отображать Размер", true);

    public HitBox() {
        super("HitBox", "Изменяет размеры хитбоксов существ", Category.Combat);
        addSettings(size, showHitBox);
    }

    @EventTarget
    public void onUpdate(EventEntityHitBox event) {
        if (!(event.getEntity() instanceof LivingEntity)) return;
        event.setSize(size.get());
    }

    public boolean shouldShowHitBox() {
        return isEnabled() && showHitBox.get();
    }
}