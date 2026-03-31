package mods.voicechat.plugins.impl;

import mods.voicechat.api.Player;
import mods.voicechat.intercompatibility.CommonCompatibilityManager;
import net.minecraft.entity.player.PlayerEntity;

public class PlayerImpl extends EntityImpl implements Player {

    public PlayerImpl(PlayerEntity entity) {
        super(entity);
    }

    @Override
    public Object getPlayer() {
        return CommonCompatibilityManager.INSTANCE.createRawApiPlayer(getRealPlayer());
    }

    public PlayerEntity getRealPlayer() {
        return (PlayerEntity) entity;
    }

}
