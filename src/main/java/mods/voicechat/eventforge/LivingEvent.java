package mods.voicechat.eventforge;

import net.minecraft.entity.LivingEntity;

public class LivingEvent extends EntityEvent {
    private final LivingEntity entityLiving;

    public LivingEvent(LivingEntity entity) {
        super(entity);
        this.entityLiving = entity;
    }

    public LivingEntity getEntityLiving() {
        return this.entityLiving;
    }
}
