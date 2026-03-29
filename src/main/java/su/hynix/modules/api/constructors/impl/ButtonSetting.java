package su.hynix.modules.api.constructors.impl;

import lombok.Getter;
import su.hynix.modules.api.constructors.Setting;

import java.util.function.Supplier;

@Getter
public class ButtonSetting extends Setting<Boolean> {

    public final String textOn;
    public final String textOff;
    public boolean defaultVal;

    public ButtonSetting(String name, Boolean defaultVal, String textOn, String textOff) {
        super(name, defaultVal);
        this.defaultVal = defaultVal;
        this.textOff = textOff;
        this.textOn = textOn;
    }

    public ButtonSetting(String name, Boolean defaultVal, String textOn, String textOff, Supplier<Boolean> visible) {
        super(name, defaultVal);
        this.defaultVal = defaultVal;
        this.textOff = textOff;
        this.textOn = textOn;
        visible(visible);
    }
}