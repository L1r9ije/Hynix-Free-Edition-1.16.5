/*
 * This file is part of Baritone.
 *
 * Baritone is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Baritone is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Baritone.  If not, see <https://www.gnu.org/licenses/>.
 */

package mods.baritone.behavior;

import com.darkmagician6.eventapi.EventManager;
import com.darkmagician6.eventapi.EventTarget;
import mods.baritone.Baritone;
import mods.baritone.api.api.java.baritone.api.Settings;
import mods.baritone.api.api.java.baritone.api.behavior.ILookBehavior;
import mods.baritone.api.api.java.baritone.api.behavior.look.IAimProcessor;
import mods.baritone.api.api.java.baritone.api.behavior.look.ITickableAimProcessor;
import mods.baritone.api.api.java.baritone.api.event.events.PacketEvent;
import mods.baritone.api.api.java.baritone.api.event.events.TickEvent;
import mods.baritone.api.api.java.baritone.api.event.events.WorldEvent;
import mods.baritone.api.api.java.baritone.api.utils.IPlayerContext;
import mods.baritone.api.api.java.baritone.api.utils.Rotation;
import mods.baritone.behavior.look.ForkableRandom;
import su.hynix.component.impl.RotationComponent;
import su.hynix.events.EventUpdate;

import java.util.Optional;

public final class LookBehavior extends Behavior implements ILookBehavior {

    private final AimProcessor processor;

    // Packet-based server rotation tracking removed
    /**
     * The current look target, may be {@code null}.
     */
    private Target target;
    /**
     * 11.08 2031
     * The last player rotation. Used to restore the player's angle when using free look.
     *
     * @see Settings#freeLook
     */
    private Rotation prevRotation;

    public LookBehavior(Baritone baritone) {
        super(baritone);
        this.processor = new AimProcessor(baritone.getPlayerContext());
        EventManager.register(this);
    }

    @Override
    public void updateTarget(Rotation rotation, boolean blockInteract) {
        this.target = new Target(rotation, blockInteract);
    }

    @Override
    public IAimProcessor getAimProcessor() {
        return this.processor;
    }

    @Override
    public void onTick(TickEvent event) {
        if (event.type() == TickEvent.Type.IN) {
            this.processor.tick();
        }
    }

    @EventTarget
    public void onPlayerUpdate(EventUpdate event) {
        if (this.target == null) {
            return;
        }

        final Rotation actual = this.processor.peekRotation(this.target.rotation);
        final float t = ctx.player().ticksExisted + ctx.minecraft().getRenderPartialTicks();
        final float smoothYaw = (float) (Math.sin(t * 0.50F) * 8.0 + Math.sin(t * 0.04F + 17.2) * 1.5);
        final float smoothPitch = (float) (Math.sin(t * 0.65F) * 2.0 + Math.sin(t * 0.03F + 54.1) * 0.5);
        RotationComponent.update(new su.hynix.handlers.impl.Rotation(
                actual.yaw(),
                actual.pitch()
        ), 180.0F, 1, 5);
        this.target = null;
    }

    @Override
    public void onSendPacket(PacketEvent event) {
    }

    @Override
    public void onWorldEvent(WorldEvent event) {
        this.target = null;
    }

    public void pig() {
        if (this.target != null) {
            final Rotation desired = this.target.rotation;
            RotationComponent.update(new su.hynix.handlers.impl.Rotation(desired.yaw(), ctx.player().rotationPitch), 180.0F, 2, 100);
        }
    }

    public Optional<Rotation> getEffectiveRotation() {
        return Optional.empty();
    }


    private static final class AimProcessor extends AbstractAimProcessor {

        public AimProcessor(final IPlayerContext ctx) {
            super(ctx);
        }

        @Override
        protected Rotation getPrevRotation() {
            return ctx.playerRotations();
        }
    }

    private static abstract class AbstractAimProcessor implements ITickableAimProcessor {

        protected final IPlayerContext ctx;
        private final ForkableRandom rand;
        private double randomYawOffset;
        private double randomPitchOffset;

        public AbstractAimProcessor(IPlayerContext ctx) {
            this.ctx = ctx;
            this.rand = new ForkableRandom();
        }

        private AbstractAimProcessor(final AbstractAimProcessor source) {
            this.ctx = source.ctx;
            this.rand = source.rand.fork();
            this.randomYawOffset = source.randomYawOffset;
            this.randomPitchOffset = source.randomPitchOffset;
        }

        @Override
        public final Rotation peekRotation(final Rotation rotation) {
            final Rotation prev = this.getPrevRotation();

            float desiredYaw = rotation.yaw();
            float desiredPitch = rotation.pitch();

            // In other words, the target doesn't care about the pitch, so it used playerRotations().getPitch()
            // and it's safe to adjust it to a normal level
            if (desiredPitch == prev.pitch()) {
                desiredPitch = nudgeToLevel(desiredPitch);
            }

            desiredYaw += this.randomYawOffset;
            desiredPitch += this.randomPitchOffset;

            return new Rotation(
                    this.calculateMouseMove(prev.yaw(), desiredYaw),
                    this.calculateMouseMove(prev.pitch(), desiredPitch)
            ).clamp();
        }

        @Override
        public final void tick() {
            // randomLooking
            this.randomYawOffset = (this.rand.nextDouble() - 0.5) * Baritone.settings().randomLooking.value;
            this.randomPitchOffset = (this.rand.nextDouble() - 0.5) * Baritone.settings().randomLooking.value;

            // randomLooking113
            double random = this.rand.nextDouble() - 0.5;
            if (Math.abs(random) < 0.1) {
                random *= 4;
            }
            this.randomYawOffset += random * Baritone.settings().randomLooking113.value;
        }

        @Override
        public final void advance(int ticks) {
            for (int i = 0; i < ticks; i++) {
                this.tick();
            }
        }

        @Override
        public Rotation nextRotation(final Rotation rotation) {
            final Rotation actual = this.peekRotation(rotation);
            this.tick();
            return actual;
        }

        @Override
        public final ITickableAimProcessor fork() {
            return new AbstractAimProcessor(this) {

                private Rotation prev = AbstractAimProcessor.this.getPrevRotation();

                @Override
                public Rotation nextRotation(final Rotation rotation) {
                    return (this.prev = super.nextRotation(rotation));
                }

                @Override
                protected Rotation getPrevRotation() {
                    return this.prev;
                }
            };
        }

        protected abstract Rotation getPrevRotation();

        /**
         * Nudges the player's pitch to a regular level. (Between {@code -20} and {@code 10}, increments are by {@code 1})
         */
        private float nudgeToLevel(float pitch) {
            if (pitch < -20) {
                return pitch + 1;
            } else if (pitch > 10) {
                return pitch - 1;
            }
            return pitch;
        }

        private float calculateMouseMove(float current, float target) {
            return target;
        }

        private double angleToMouse(double angleDelta) {
            return 0;
        }

        private double mouseToAngle(double mouseDelta) {
            return 0;
        }
    }

    private record Target(Rotation rotation, Mode mode) {

        private Target(Rotation rotation, boolean mode) {
            this(rotation, Mode.resolve(mode));
        }

        enum Mode {
            /**
             * Rotation will be set client-side and is visual to the player
             */
            CLIENT,

            /**
             * Rotation will be set server-side and is silent to the player
             */
            SERVER,

            /**
             * Rotation will remain unaffected on both the client and server
             */
            NONE;

            static Mode resolve(boolean blockInteract) {
                final Settings settings = Baritone.settings();
                final boolean antiCheat = settings.antiCheatCompatibility.value;
                final boolean blockFreeLook = settings.blockFreeLook.value;
                final boolean freeLook = settings.freeLook.value;

                if (!freeLook) return CLIENT;
                if (!blockFreeLook && blockInteract) return CLIENT;

                // Regardless of if antiCheatCompatibility is enabled, if a blockInteract is requested then the player
                // rotation needs to be set somehow, otherwise Baritone will halt since objectMouseOver() will just be
                // whatever the player is mousing over visually. Let's just settle for setting it silently.
                if (antiCheat || blockInteract) return SERVER;

                // Pathing regularly without antiCheatCompatibility, don't set the player rotation
                return NONE;
            }
        }
    }
}
