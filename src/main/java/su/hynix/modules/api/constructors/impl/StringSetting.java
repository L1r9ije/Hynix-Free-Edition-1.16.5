package su.hynix.modules.api.constructors.impl;

import su.hynix.modules.api.constructors.Setting;

public class StringSetting extends Setting<String> {

    public StringSetting(String name) {
        super(name, "");
    }

    public String getValue() {
        return get();
    }

    public void setValue(String value) {
        set(value);
    }
}
