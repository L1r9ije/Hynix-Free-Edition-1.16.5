package net.minecraft.inventory;

import javax.annotation.Nullable;

public interface IClearable {
    static void clearObj(@Nullable Object object) {
        if (object instanceof IClearable) {
            ((IClearable) object).clear();
        }
    }

    void clear();
}
