package su.hynix.modules.impl.miscellaneous;

import su.hynix.modules.Category;
import su.hynix.modules.Module;
import su.hynix.modules.api.constructors.impl.ModeSetting;
import su.hynix.modules.api.constructors.impl.SliderSetting;

public class ToggleSounds extends Module {
    public static ModeSetting type = new ModeSetting("Тип звука", "Тип 1", "Тип 1", "Тип 2", "Тип 3", "Тип 4");
    // , "Кастом"
//    public static ClickSetting on = new ClickSetting("Звук включения", ).visible(() -> type.is("Кастом"));
//    public static ClickSetting off = new ClickSetting("Звук отключения", ).visible(() -> type.is("Кастом"));
    public static SliderSetting volume = new SliderSetting("Громкость", 75, 0, 100, 1);

    public ToggleSounds() {
        super("Toggle Sounds", "Воспроизводит звуки при изменении состояния модулей", Category.Miscellaneous);
        addSettings(type, volume);
    }

    public static String getSoundFile(boolean isEnable) {
        String mode = type.get();
        return switch (mode) {
            case "Тип 1" -> isEnable ? "enabled0" : "disabled0";
            case "Тип 2" -> isEnable ? "enabled1" : "disabled1";
            case "Тип 3" -> isEnable ? "enabled2" : "disabled2";
            case "Тип 4" -> isEnable ? "enabled3" : "disabled3";
            default -> null;
        };
    }
}
