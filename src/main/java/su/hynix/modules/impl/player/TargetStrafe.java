package su.hynix.modules.impl.player;

import com.darkmagician6.eventapi.EventTarget;
import net.minecraft.block.AirBlock;
import net.minecraft.block.Blocks;
import net.minecraft.block.SoulSandBlock;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.play.client.CEntityActionPacket;
import net.minecraft.potion.Effects;
import net.minecraft.util.math.BlockPos;
import su.hynix.component.impl.SensUtil;
import su.hynix.events.EventInput;
import su.hynix.events.EventMoving;
import su.hynix.events.EventUpdate;
import su.hynix.hynix;
import su.hynix.modules.Category;
import su.hynix.modules.Module;
import su.hynix.modules.api.constructors.impl.BooleanSetting;
import su.hynix.modules.api.constructors.impl.SliderSetting;
import su.hynix.modules.impl.combat.AttackAura;
import su.hynix.utils.math.TimeUtil;

@SuppressWarnings("all")
public class TargetStrafe extends Module {


    private final SliderSetting distance = new SliderSetting("Дистанция", 2.2f, 0.5f, 6f, 0.05f);
    private final SliderSetting speed = new SliderSetting("Скорость", 0.0f, -0.1f, 0.5f, 0.01f);
    private final BooleanSetting autoJump = new BooleanSetting("Авто прыжок", true);
    private final BooleanSetting saveTarget = new BooleanSetting("Сохранять цель", true);
    private final BooleanSetting damageBoost = new BooleanSetting("Буст с дамагом", true);
    private final SliderSetting boostValue = new SliderSetting("Значение буста", 1.5f, 0.1f, 5.0f, 0.05f, () -> damageBoost.get());
    private final SliderSetting boostTime = new SliderSetting("Время буста (мс)", 1000f, 100f, 3000f, 50f, () -> damageBoost.get());
    private final BooleanSetting switchOnHit = new BooleanSetting("Менять сторону при столкновении", true);
    private final TimeUtil boostTimer = new TimeUtil();
    private float side = 1f;
    private LivingEntity savedTarget = null;
    private String targetName = "";
    private boolean boosted = false;
    private double oldSpeed = 0.0;


    public TargetStrafe() {
        super("TargetStrafe", "Стрейф вокруг цели AttackAura", Category.Movement);
        addSettings(distance, speed, autoJump, saveTarget, switchOnHit, damageBoost, boostValue, boostTime);
    }


    @Override
    public void onEnable() {
        super.onEnable();
        savedTarget = null;
        targetName = "";
        oldSpeed = 0.0;
        boosted = false;
    }

    @Override
    public void onDisable() {
        super.onDisable();
        savedTarget = null;
        oldSpeed = 0.0;
    }


    @EventTarget
    public void onUpdate(EventUpdate e) {
        if (mc.player == null || mc.world == null) return;

        LivingEntity t = resolveTarget();
        if (t != null && t.isAlive() && mc.player.isOnGround()
                && autoJump.get() && !mc.gameSettings.keyBindJump.isKeyDown()) {
            mc.player.jump();
        }


        if (boosted && boostTimer.hasTimeElapsed((long) (float) boostTime.get())) {
            boosted = false;
        }
    }

    @EventTarget
    public void onMoving(EventMoving event) {
        if (mc.player == null || mc.world == null) return;
        if (!canStrafe()) return;
        if (mc.player.ticksExisted < 10) return;

        LivingEntity t = resolveTarget();
        if (t == null || !t.isAlive() || t.getHealth() <= 0f) {
            oldSpeed = 0.0;
            return;
        }


        if (switchOnHit.get() && mc.player.collidedHorizontally) {
            side *= -1;
        }


        if (mc.gameSettings.keyBindLeft.isKeyDown()) side = 1f;
        if (mc.gameSettings.keyBindRight.isKeyDown()) side = -1f;


        double angle = Math.atan2(
                mc.player.getPosZ() - t.getPosZ(),
                mc.player.getPosX() - t.getPosX()
        );
        double orbitStep = horizontalSpeed() / Math.max(mc.player.getDistance(t), distance.min);
        angle += orbitStep * side;


        double targetX = t.getPosX() + distance.get() * Math.cos(angle);
        double targetZ = t.getPosZ() + distance.get() * Math.sin(angle);


        double yaw = Math.toDegrees(
                Math.atan2(targetZ - mc.player.getPosZ(), targetX - mc.player.getPosX())
        ) - 90.0;


        double currentDist = mc.player.getDistance(t);
        double distOffset = currentDist <= distance.get() - 0.1 ? speed.get() : 0.0;
        double baseSpeed = calcSpeed(event);
        double finalSpeed = baseSpeed + distOffset;

        applyGcdMotion(event,
                finalSpeed * -Math.sin(Math.toRadians(yaw)),
                finalSpeed * Math.cos(Math.toRadians(yaw)));

        oldSpeed = finalSpeed;
        fixSprintPacket();
    }


    @EventTarget
    public void onInput(EventInput event) {
        if (!canStrafe()) return;
        LivingEntity t = resolveTarget();
        if (t == null || !t.isAlive()) return;


        event.setForward(0);
        event.setStrafe(0);
    }


    private double calcSpeed(EventMoving event) {
        double base = horizontalSpeed();

        if (!mc.player.isOnGround()) {
            double accel = 0.026;
            double maxSpeed = 0.3 + speed.get();

            if (damageBoost.get() && boosted) {
                maxSpeed += boostValue.get() / 10.0;
            }

            double next = oldSpeed + accel;
            base = Math.min(next, maxSpeed);
        }

        return base;
    }


    private void fixSprintPacket() {
        if (CEntityActionPacket.lastUpdatedSprint != mc.player.isSprinting()) {
            mc.player.connection.sendPacket(new CEntityActionPacket(
                    mc.player,
                    mc.player.isSprinting()
                            ? CEntityActionPacket.Action.START_SPRINTING
                            : CEntityActionPacket.Action.STOP_SPRINTING
            ));
        }
    }


    private double horizontalSpeed() {
        double mx = mc.player.getMotion().x;
        double mz = mc.player.getMotion().z;
        return Math.sqrt(mx * mx + mz * mz);
    }


    private void applyGcdMotion(EventMoving event, double x, double z) {
        float gcd = SensUtil.getGCDValue();
        double noiseX = x - (x % gcd);
        double noiseZ = z - (z % gcd);
        event.getMotion().x = noiseX;
        event.getMotion().z = noiseZ;
    }

    private LivingEntity resolveTarget() {
        AttackAura aura = (AttackAura) hynix.getInstance().getModuleManager().getModule(AttackAura.class);
        if (aura == null || !aura.isEnabled()) return null;

        LivingEntity auraTarget = AttackAura.getTarget();

        if (auraTarget != null) {
            targetName = auraTarget.getName().getString();
        }

        if (saveTarget.get() && auraTarget == null && targetName != null && !targetName.isEmpty()) {

            return savedTarget = findByName(targetName);
        }

        return savedTarget = auraTarget;
    }

    private LivingEntity findByName(String name) {
        for (Entity e : mc.world.getAllEntities()) {
            if (e instanceof PlayerEntity
                    && e.getName().getString().equalsIgnoreCase(name)) {
                return (LivingEntity) e;
            }
        }
        return savedTarget;
    }

    private boolean canStrafe() {
        if (mc.player == null || mc.world == null) return false;
        if (mc.player.isSneaking()) return false;
        if (mc.player.isElytraFlying()) return false;
        if (mc.player.isInWater() || mc.player.isInLava()) return false;
        if (mc.player.abilities.isFlying) return false;
        if (mc.player.isPotionActive(Effects.LEVITATION)) return false;

        BlockPos pos = new BlockPos(mc.player.getPositionVec());
        BlockPos above = pos.up();
        BlockPos below = pos.down();
        if (mc.world.getBlockState(above).getBlock() instanceof AirBlock
                && mc.world.getBlockState(below).getBlock() == Blocks.WATER) {
            return false;
        }

        Material mat = mc.world.getBlockState(pos).getMaterial();
        if (mat == Material.WEB) return false;
        if (mc.world.getBlockState(below).getBlock() instanceof SoulSandBlock) return false;

        return true;
    }
}