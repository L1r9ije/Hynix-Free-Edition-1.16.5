package mods.voicechat.plugins.impl;

import mods.voicechat.api.Position;
import net.minecraft.util.math.vector.Vector3d;

import java.util.Objects;

public record PositionImpl(Vector3d position) implements Position {

    public PositionImpl(double x, double y, double z) {
        this(new Vector3d(x, y, z));
    }

    @Override
    public double getX() {
        return position.x;
    }

    @Override
    public double getY() {
        return position.y;
    }

    @Override
    public double getZ() {
        return position.z;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        PositionImpl position1 = (PositionImpl) object;
        return Objects.equals(position, position1.position);
    }

}
