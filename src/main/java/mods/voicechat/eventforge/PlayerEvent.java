package mods.voicechat.eventforge;

import net.minecraft.entity.player.PlayerEntity;

public class PlayerEvent extends LivingEvent {
    private final PlayerEntity entityPlayer;

    public PlayerEvent(PlayerEntity player) {
        super(player);
        this.entityPlayer = player;
    }

    public PlayerEntity getPlayer() {
        return this.entityPlayer;
    }

    public static class PlayerLoggedInEvent extends PlayerEvent {
        public PlayerLoggedInEvent(PlayerEntity player) {
            super(player);
        }
    }

    public static class PlayerLoggedOutEvent extends PlayerEvent {
        public PlayerLoggedOutEvent(PlayerEntity player) {
            super(player);
        }
    }
}
