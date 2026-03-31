package net.minecraft.client.particle;

import com.darkmagician6.eventapi.EventManager;
import su.hynix.events.EventTotemParticle;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particles.BasicParticleType;

import java.util.Random;

public class TotemOfUndyingParticle extends SimpleAnimatedParticle {
    private TotemOfUndyingParticle(ClientWorld world, double x, double y, double z, double motionX, double motionY, double motionZ, IAnimatedSprite spriteWithAge) {
        super(world, x, y, z, spriteWithAge, -0.05F);
        this.motionX = motionX;
        this.motionY = motionY;
        this.motionZ = motionZ;
        this.particleScale *= 0.75F;
        this.maxAge = 60 + this.rand.nextInt(12);
        this.selectSpriteWithAge(spriteWithAge);

        if (this.rand.nextInt(4) == 0) {
            this.setColor(0.6F + this.rand.nextFloat() * 0.2F, 0.6F + this.rand.nextFloat() * 0.3F, this.rand.nextFloat() * 0.2F);
        } else {
            this.setColor(0.1F + this.rand.nextFloat() * 0.2F, 0.4F + this.rand.nextFloat() * 0.3F, this.rand.nextFloat() * 0.2F);
        }

        this.setBaseAirFriction(0.6F);
    }

    public static class Factory implements IParticleFactory<BasicParticleType> {
        private final IAnimatedSprite spriteSet;

        public Factory(IAnimatedSprite spriteSet) {
            this.spriteSet = spriteSet;
        }

        public Particle makeParticle(BasicParticleType typeIn, ClientWorld worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            EventTotemParticle event = new EventTotemParticle(x, y, z, xSpeed, ySpeed, zSpeed);
            EventManager.call(event);

            if (event.isCancelled()) {
                return null;
            }

            return new TotemOfUndyingParticle(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, this.spriteSet);
        }
    }
}
