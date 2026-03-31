package su.hynix.modules.impl.movement;

import com.darkmagician6.eventapi.EventTarget;
import lombok.Getter;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ArmorStandEntity;
import net.minecraft.entity.item.BoatEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.math.vector.Vector3d;
import su.hynix.events.EventUpdate;
import su.hynix.modules.Category;
import su.hynix.modules.Module;
import su.hynix.modules.api.constructors.impl.BooleanSetting;
import su.hynix.modules.api.constructors.impl.ModeSetting;
import su.hynix.modules.api.constructors.impl.SliderSetting;
import su.hynix.utils.math.TimeUtil;
import su.hynix.utils.player.MoveUtil;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@SuppressWarnings("all")
public class Speed extends Module {


    @Getter
    public final ModeSetting mode = new ModeSetting("Режим", "HvH", "Entity", "SP-DUEL", "HvH", "MetaHvH");


    private final SliderSetting scanRadius = new SliderSetting("Радиус сканирования", 1.25f, 0.5f, 3.0f, 0.05f,
            () -> mode.is("Entity") || mode.is("SP-DUEL"));
    private final SliderSetting boostLow = new SliderSetting("Буст (низкая скорость)", 0.020f, 0.005f, 0.08f, 0.001f,
            () -> mode.is("Entity") || mode.is("SP-DUEL"));
    private final SliderSetting boostHigh = new SliderSetting("Буст (высокая скорость)", 0.032f, 0.010f, 0.10f, 0.001f,
            () -> mode.is("Entity"));

    // HvH
    private final BooleanSetting noSlowness = new BooleanSetting("Игнор замедления", false,
            () -> mode.is("HvH"));

    // ─── Внутреннее состояние ───────────────────────────────────────────────────
    private final Map<LivingEntity, Vector3d> previousPositions = new HashMap<>();
    private final TimeUtil stopWatch = new TimeUtil();
    private int airTicks = 0;

    // ─── Конструктор ────────────────────────────────────────────────────────────
    public Speed() {
        super("Speed", "Буст скорости через коллизии / setSpeed", Category.Movement);
        addSettings(mode, scanRadius, boostLow, boostHigh, noSlowness);
    }

    // ─── Lifecycle ───────────────────────────────────────────────────────────────
    @Override
    public void onEnable() {
        super.onEnable();
        airTicks = 0;
        previousPositions.clear();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        airTicks = 0;
        previousPositions.clear();
    }

    // ─── Основной апдейт ────────────────────────────────────────────────────────
    @EventTarget
    public void onEvent(EventUpdate event) {
        if (mc.player == null || mc.world == null) return;

        // Счётчик тиков в воздухе
        if (mc.player.isOnGround()) {
            airTicks = 0;
        } else {
            airTicks++;
        }

        switch (mode.get()) {
            case "Entity" -> handleEntity(0.10f);
            case "SP-DUEL" -> handleEntity(0.07f);
            case "HvH" -> handleHvH();
            case "MetaHvH" -> handleMetaHvH();
        }
    }

    // ════════════════════════════════════════════════════════════════════════════
    //  ENTITY / SP-DUEL
    // ════════════════════════════════════════════════════════════════════════════

    /**
     * Буст через физическую коллизию с другими игроками/сущностями.
     * При движении ищет ближайшего игрока, считает его скорость и добавляет
     * velocity вперёд если есть реальный контакт с AABB.
     */
    private void handleEntity(float airJumpFactor) {
        if (!isMoving()) return;

        float scan = scanRadius.get();

        List<LivingEntity> nearby = mc.world.getEntitiesWithinAABB(
                LivingEntity.class,
                mc.player.getBoundingBox().grow(scan)
        );

        for (LivingEntity entity : nearby) {
            if (entity == mc.player) continue;
            if (entity instanceof ArmorStandEntity) continue;

            // Проверка реального расстояния по XZ
            double dx = Math.abs(mc.player.getPosX() - entity.getPosX());
            double dz = Math.abs(mc.player.getPosZ() - entity.getPosZ());
            if (dx > scan * 1.7 || dz > scan * 1.1) continue;

            // Считаем скорость цели за прошлый тик
            double entitySpeed = getEntitySpeed(entity);

            // Есть ли реальный контакт AABB (включая лодки)
            int collisions = countCollisions(scan);
            if (collisions == 0) break;

            // Выбираем величину буста
            double boost;
            if (entitySpeed < 5.0) {
                boost = boostLow.get();
            } else {
                boost = mode.is("SP-DUEL") ? boostLow.get() : boostHigh.get();
            }

            // Бустим только в воздухе — иначе на земле ломает позицию
            if (!mc.player.isOnGround()) {
                double[] motion = motionForward(boost);
                mc.player.addVelocity(motion[0], 0.0, motion[1]);
                mc.player.jumpMovementFactor = airJumpFactor;
            }
            break;
        }
    }

    // ════════════════════════════════════════════════════════════════════════════
    //  HvH
    // ════════════════════════════════════════════════════════════════════════════

    /**
     * Выставляет скорость движения напрямую через MoveUtil.setSpeed.
     * Учитывает уровень зелья скорости и плавно меняет множитель по тикам в воздухе.
     */
    private void handleHvH() {
        if (!isMoving()) return;
        if (mc.player.isElytraFlying()) return;

        EffectInstance speedEffect = mc.player.getActivePotionEffect(Effects.SPEED);
        EffectInstance slownessEffect = mc.player.getActivePotionEffect(Effects.SLOWNESS);

        if (slownessEffect != null && !noSlowness.get()) return;

        // Базовая горизонтальная скорость без поушена ≈ 0.2446 блока/тик
        float base;
        if (speedEffect == null) {
            base = 0.2446f;
        } else {
            switch (speedEffect.getAmplifier()) {
                case 0 -> base = 0.2872f;   // Speed I
                case 1 -> base = 0.3600f;   // Speed II
                case 2 -> base = 0.4175f;   // Speed III
                case 3 -> base = 0.4780f;   // Speed IV
                default -> base = 0.5400f;   // Speed V+
            }
        }

        // Урон от замедления
        if (slownessEffect != null) {
            base *= (1f - 0.15f * (slownessEffect.getAmplifier() + 1));
        }

        // Воздушный буст — нарастает плавно
        if (!mc.player.isOnGround()) {
            float airMult;
            if (airTicks <= 1) airMult = 1.52f;
            else if (airTicks <= 3) airMult = 1.43f;
            else if (airTicks <= 6) airMult = 1.32f;
            else airMult = 1.20f;
            base *= airMult;
        }

        MoveUtil.setMotion(base);
    }

    // ════════════════════════════════════════════════════════════════════════════
    //  MetaHvH
    // ════════════════════════════════════════════════════════════════════════════

    /**
     * Расширенный HvH-режим с учётом специальных предметов в офруке.
     * Скорости подобраны под Speed II–IV и конкретные предметы-усилители.
     */
    private void handleMetaHvH() {
        if (!isMoving()) return;
        if (mc.player.isPotionActive(Effects.SLOWNESS)) return;

        // Определяем предмет в офруке по байт-имени (защита от переименований)
        byte[] nameBytes = mc.player.getHeldItemOffhand()
                .getDisplayName().getString()
                .getBytes(StandardCharsets.UTF_8);
        String byteStr = Arrays.toString(nameBytes);

        // Ломтик Дыни / Акула / Тигровая акула — дают разные прибавки к скорости
        boolean isMelon = byteStr.equals("[-48, -88, -48, -80, -47, -128, 32, -48, -108, -47, -117, -48, -67, -48, -72]");
        boolean isShark = byteStr.equals("[-48, -88, -48, -80, -47, -128, 32, 75, 73, 78, 71]");
        boolean isTiger = byteStr.equals("[-48, -94, -48, -72, -48, -77, -47, -128, -48, -72, -48, -67, -48, -67, -48, -80, -47, -113, 32, -48, -77, -48, -66, -48, -69, -48, -66, -48, -78, -48, -80]");

        EffectInstance speedEffect = mc.player.getActivePotionEffect(Effects.SPEED);

        boolean inAir = !mc.player.isOnGround() && mc.player.fallDistance <= 0.25f;
        boolean onGround = mc.player.isOnGround();

        if (speedEffect == null) {
            if (inAir) MoveUtil.setMotion(isMelon ? 0.31f : 0.29f);
            return;
        }

        int amp = speedEffect.getAmplifier();

        switch (amp) {
            case 0 -> {  // Speed I
                if (inAir) MoveUtil.setMotion(0.38f);
                else if (onGround) MoveUtil.setMotion(0.29f);
            }
            case 1 -> {  // Speed II
                if (inAir) MoveUtil.setMotion(0.52f);
                else if (onGround) MoveUtil.setMotion(0.355f);
            }
            case 2 -> {  // Speed III
                if (inAir) {
                    MoveUtil.setMotion(isShark ? 0.72f : isTiger ? 0.70f : isMelon ? 0.63f : 0.585f);
                } else if (onGround) {
                    MoveUtil.setMotion(isShark ? 0.50f : isTiger ? 0.49f : isMelon ? 0.44f : 0.423f);
                }
            }
            case 3 -> {  // Speed IV
                if (inAir) {
                    MoveUtil.setMotion(isShark ? 0.82f : isMelon ? 0.76f : 0.70f);
                } else if (onGround) {
                    MoveUtil.setMotion(isShark ? 0.58f : isMelon ? 0.53f : 0.49f);
                }
            }
            default -> { // Speed V+
                if (inAir) MoveUtil.setMotion(0.92f);
                else if (onGround) MoveUtil.setMotion(0.65f);
            }
        }
    }

    // ════════════════════════════════════════════════════════════════════════════
    //  Вспомогательные методы
    // ════════════════════════════════════════════════════════════════════════════

    /**
     * Считает кол-во реальных коллизий в заданном радиусе (кроме стендов).
     */
    private int countCollisions(double radius) {
        int count = 0;
        for (net.minecraft.entity.Entity e : mc.world.getEntitiesWithinAABBExcludingEntity(
                mc.player, mc.player.getBoundingBox().grow(radius))) {
            if (e instanceof ArmorStandEntity) continue;
            if (e instanceof BoatEntity || e instanceof LivingEntity) count++;
        }
        return count;
    }

    /**
     * Скорость сущности за последний тик (блоков/сек).
     */
    private double getEntitySpeed(LivingEntity entity) {
        Vector3d cur = entity.getPositionVec();
        Vector3d prev = previousPositions.getOrDefault(entity, cur);
        double dx = cur.x - prev.x;
        double dz = cur.z - prev.z;
        previousPositions.put(entity, cur);
        return Math.sqrt(dx * dx + dz * dz) * 20.0;
    }

    /**
     * Вектор движения вперёд с учётом yaw и стрейфа.
     */
    private double[] motionForward(double speed) {
        float fwd = mc.player.moveForward;
        float strafe = mc.player.moveStrafing;
        float yaw = mc.player.rotationYaw;

        if (fwd != 0) {
            if (strafe > 0) yaw += fwd > 0 ? -45 : 45;
            else if (strafe < 0) yaw += fwd > 0 ? 45 : -45;
            strafe = 0;
            fwd = fwd > 0 ? 1 : -1;
        }

        double sin = Math.sin(Math.toRadians(yaw + 90));
        double cos = Math.cos(Math.toRadians(yaw + 90));
        return new double[]{
                fwd * speed * cos + strafe * speed * sin,
                fwd * speed * sin - strafe * speed * cos
        };
    }

    private boolean isMoving() {
        return mc.gameSettings.keyBindForward.isKeyDown()
                || mc.gameSettings.keyBindBack.isKeyDown()
                || mc.gameSettings.keyBindLeft.isKeyDown()
                || mc.gameSettings.keyBindRight.isKeyDown();
    }
}