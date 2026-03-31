package mods.voicechat.plugins.impl;

import mods.voicechat.api.ServerLevel;
import mods.voicechat.intercompatibility.CommonCompatibilityManager;
import net.minecraft.world.server.ServerWorld;

import java.util.Objects;

public class ServerLevelImpl implements ServerLevel {

    private final ServerWorld serverLevel;

    public ServerLevelImpl(ServerWorld serverLevel) {
        this.serverLevel = serverLevel;
    }

    @Override
    public Object getServerLevel() {
        return CommonCompatibilityManager.INSTANCE.createRawApiLevel(serverLevel);
    }

    public ServerWorld getRawServerLevel() {
        return serverLevel;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        ServerLevelImpl that = (ServerLevelImpl) object;
        return Objects.equals(serverLevel, that.serverLevel);
    }

    @Override
    public int hashCode() {
        return serverLevel != null ? serverLevel.hashCode() : 0;
    }
}
