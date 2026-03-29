package su.hynix.modules.impl.movement;

import com.darkmagician6.eventapi.EventTarget;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ArmorStandEntity;
import net.minecraft.entity.item.BoatEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.vector.Vector3d;
import su.hynix.events.EventUpdate;
import su.hynix.modules.Category;
import su.hynix.modules.Module;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Speed extends Module {

    final Map<Entity, Vector3d> previousPositions = new HashMap<>();

    public Speed() {
        super("Speed", "Позволяет бустится от колизии", Category.Movement);
    }

    @EventTarget
    public void onEvent(EventUpdate event) {
        if (isMoving()) {
            double scanRadius = 1.25;

            List<Entity> nearbyEntities = mc.world.getEntitiesWithinAABBExcludingEntity(
                    mc.player,
                    mc.player.getBoundingBox().grow(scanRadius)
            );

            for (Entity entity : nearbyEntities) {
                if (entity != mc.player &&
                        (entity instanceof PlayerEntity)) {

                    double distanceX = Math.abs(mc.player.getPosX() - entity.getPosX());
                    double distanceZ = Math.abs(mc.player.getPosZ() - entity.getPosZ());
                    double activationDistanceX = 2.1;
                    double activationDistanceZ = 1.3;

                    if (distanceX > activationDistanceX || distanceZ > activationDistanceZ) {
                        continue;
                    }

                    double entitySpeed = getEntitySpeed(entity);
                    double boostAmount;

                    if (entitySpeed < 5) {
                        boostAmount = 0.02;

                        List<Entity> collisionEntities = mc.world.getEntitiesWithinAABBExcludingEntity(
                                mc.player,
                                mc.player.getBoundingBox().grow(0.1)
                        );

                        int collisions = 0;
                        for (Entity collisionEntity : collisionEntities) {
                            if (collisionEntity != mc.player &&
                                    (!(collisionEntity instanceof ArmorStandEntity) || collisionEntity instanceof BoatEntity)) {
                                collisions++;
                            }
                        }


                        if (collisions > 0) {
                            double[] motion = forward(boostAmount);
                            mc.player.addVelocity(motion[0], 0.0, motion[1]);
                        }
                    } else {
                        boostAmount = 0.032;
                        double checkRadius = 1.25;

                        List<Entity> potentialCollisions = mc.world.getEntitiesWithinAABBExcludingEntity(
                                mc.player,
                                mc.player.getBoundingBox().grow(checkRadius)
                        );

                        int collisions = 0;
                        for (Entity collisionEntity : potentialCollisions) {
                            if (collisionEntity != mc.player &&
                                    (!(collisionEntity instanceof ArmorStandEntity) || collisionEntity instanceof BoatEntity)) {

                                double distToCollision = mc.player.getDistance(collisionEntity);
                                if (distToCollision <= checkRadius) {
                                    collisions++;
                                }
                            }
                        }

                        if (collisions > 0) {
                            double[] motion = forward(boostAmount);
                            mc.player.addVelocity(motion[0], 0.0, motion[1]);
                        }
                    }
                    break;
                }
            }
        }

    }

    private double getEntitySpeed(Entity entity) {
        Vector3d currentPos = entity.getPositionVec();
        Vector3d previousPos = previousPositions.getOrDefault(entity, currentPos);

        double dx = currentPos.x - previousPos.x;
        double dz = currentPos.z - previousPos.z;
        double speed = Math.sqrt(dx * dx + dz * dz) * 20.0;

        previousPositions.put(entity, currentPos);

        return speed;
    }

    private double[] forward(double speed) {
        float forward = mc.player.moveForward;
        float strafe = mc.player.moveStrafing;
        float yaw = mc.player.rotationYaw;

        if (forward != 0) {
            if (strafe > 0) {
                yaw += forward > 0 ? -45 : 45;
            } else if (strafe < 0) {
                yaw += forward > 0 ? 45 : -45;
            }
            strafe = 0;
            if (forward > 0) {
                forward = 1;
            } else if (forward < 0) {
                forward = -1;
            }
        }

        double sin = Math.sin(Math.toRadians(yaw + 90));
        double cos = Math.cos(Math.toRadians(yaw + 90));
        double posX = forward * speed * cos + strafe * speed * sin;
        double posZ = forward * speed * sin - strafe * speed * cos;

        return new double[]{posX, posZ};
    }

    private boolean isMoving() {
        return mc.gameSettings.keyBindForward.isKeyDown() ||
                mc.gameSettings.keyBindBack.isKeyDown() ||
                mc.gameSettings.keyBindLeft.isKeyDown() ||
                mc.gameSettings.keyBindRight.isKeyDown();
    }


}
