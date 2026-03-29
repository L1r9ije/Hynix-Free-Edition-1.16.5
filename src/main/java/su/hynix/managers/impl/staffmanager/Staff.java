package su.hynix.managers.impl.staffmanager;

import net.minecraft.util.text.ITextComponent;

import java.util.Objects;

public record Staff(String name, ITextComponent prefix, boolean vanished) {

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Staff staff)) return false;
        return Objects.equals(name, staff.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}