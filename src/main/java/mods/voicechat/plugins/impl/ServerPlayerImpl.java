package mods.voicechat.plugins.impl;

import mods.voicechat.api.ServerLevel;
import mods.voicechat.api.ServerPlayer;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.world.server.ServerWorld;

public class ServerPlayerImpl extends PlayerImpl implements ServerPlayer {

    public ServerPlayerImpl(ServerPlayerEntity entity) {
        super(entity);
    }

    public ServerPlayerEntity getRealServerPlayer() {
        return (ServerPlayerEntity) entity;
    }

    @Override
    public ServerLevel getServerLevel() {
        return new ServerLevelImpl((ServerWorld) entity.world);
    }
}
