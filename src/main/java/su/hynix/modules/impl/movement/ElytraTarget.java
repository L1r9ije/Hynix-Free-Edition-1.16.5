package su.hynix.modules.impl.movement;

import lombok.Getter;
import su.hynix.modules.Category;
import su.hynix.modules.Module;
import su.hynix.modules.api.constructors.impl.BooleanSetting;
import su.hynix.modules.api.constructors.impl.SliderSetting;

@Getter
public class ElytraTarget extends Module {
    public static final BooleanSetting predictMovement = new BooleanSetting("Перегонять", true);
    public static final SliderSetting predictMultiplier = new SliderSetting("Перелет", 2.0f, 1.0f, 6.0f, 0.5f, predictMovement::get);
    public static SliderSetting pursuitdistance = new SliderSetting("Растояние преследования", 30, 20, 100, 5);


    public ElytraTarget() {
        super("Elytra Target", "Преследует таргета на элитре", Category.Movement);
        addSettings(pursuitdistance, predictMovement, predictMultiplier);
    }
}