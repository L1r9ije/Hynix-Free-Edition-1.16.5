package su.hynix.modules.impl.combat;

import com.darkmagician6.eventapi.EventTarget;
import lombok.Getter;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ArmorStandEntity;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.merchant.villager.WanderingTraderEntity;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.monster.PhantomEntity;
import net.minecraft.entity.monster.ShulkerEntity;
import net.minecraft.entity.monster.SlimeEntity;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.passive.fish.CodEntity;
import net.minecraft.entity.passive.fish.SalmonEntity;
import net.minecraft.entity.passive.fish.TropicalFishEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.client.CAnimateHandPacket;
import net.minecraft.network.play.client.CEntityActionPacket;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.network.play.client.CPlayerDiggingPacket;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import su.hynix.component.impl.RotationComponent;
import su.hynix.component.impl.RotationUtil;
import su.hynix.component.impl.SensUtil;
import su.hynix.component.impl.SmoothRotationComponent;
import su.hynix.events.*;
import su.hynix.handlers.impl.LookHandler;
import su.hynix.handlers.impl.Rotation;
import su.hynix.handlers.impl.TPSHandler;
import su.hynix.hynix;
import su.hynix.modules.Category;
import su.hynix.modules.Module;
import su.hynix.modules.api.constructors.impl.BooleanSetting;
import su.hynix.modules.api.constructors.impl.ModeSetting;
import su.hynix.modules.api.constructors.impl.MultiBooleanSetting;
import su.hynix.modules.api.constructors.impl.SliderSetting;
import su.hynix.modules.impl.movement.AirStuck;
import su.hynix.modules.impl.movement.Speed;
import su.hynix.modules.impl.movement.Sprint;
import su.hynix.utils.math.AuraUtil;
import su.hynix.utils.math.MathUtil;
import su.hynix.utils.math.TimeUtil;
import su.hynix.utils.misc.ServerUtil;
import su.hynix.utils.player.InventoryUtil;
import su.hynix.utils.player.MoveUtil;
import su.hynix.utils.player.PerfectDelay;
import su.hynix.utils.player.RayTraceUtil;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import static java.lang.Math.toDegrees;
import static net.minecraft.util.math.MathHelper.clamp;
import static net.minecraft.util.math.MathHelper.wrapDegrees;

@SuppressWarnings("all")
public class AttackAura extends Module {

    @Getter
    public static LivingEntity target = null;
    @Getter
    public static double bpsTarget = 0.0f;
    public final ModeSetting componentMode = new ModeSetting("Режим ротации", "Плавный", "Плавный", "FunTime", "SpookyTime", "Reallyworld", "Droid", "HolyWorld");
    public final MultiBooleanSetting checks = new MultiBooleanSetting("Прочее", new BooleanSetting("Выключить после смерти", true), new BooleanSetting("Не бить когда ешь", false), new BooleanSetting("Бить только с оружием", false), new BooleanSetting("TPSSync", false));
    public final MultiBooleanSetting targets = new MultiBooleanSetting("Цели", new BooleanSetting("Игроки", true), new BooleanSetting("Друзья", false), new BooleanSetting("Голые", true), new BooleanSetting("Животные", false), new BooleanSetting("Мобы", false));
    public final ModeSetting sortMode = new ModeSetting("Сортировать по", "Всему сразу", "Дистанции", "Здоровью", "Броне", "Всему сразу");

    public final SliderSetting attackRange = new SliderSetting("Радиус атаки", 3.0F, 2.5F, 6.0F, 0.1F);
    public final SliderSetting rotateDistance = new SliderSetting("Радиус преследования", 1.5F, 0.0F, 3.0F, 0.1F);
    public final BooleanSetting ray = new BooleanSetting("Проверка наведения", false, () -> componentMode.is("SpookyTime"));
    public final ModeSetting correctionType = new ModeSetting("Коррекция движения", "Свободная", "Свободная", "Сфокусированная", "Фулл Таргет");
    public final BooleanSetting attackThroughWalls = new BooleanSetting("Не Бить через стены", false);
    public final BooleanSetting setPitch = new BooleanSetting("Поворачивать pitch", false);
    public final BooleanSetting onlySpaceCritical = new BooleanSetting("Умные криты", false);
    @Getter
    private final BooleanSetting throughWalls = new BooleanSetting("Сквозь стены", true);

    private final BooleanSetting shielbreaker = new BooleanSetting("Ломать Щит ", true);
    private final BooleanSetting SHEIS = new BooleanSetting("Отжимать Щит ", true);
    public boolean canCrit;
    public float adjYaw;
    public float adjPitch;
    public float yawDelta;
    public double lastSpeed = 0;
    PerfectDelay perfectDelay = new PerfectDelay();
    TimeUtil stopWatch = new TimeUtil();
    int tickSprint;
    int rayTicks = 0;
    float lastYaw = 0;
    float lastPitch = 0;
    float preLastYaw = 0;
    float preLastPitch = 0;
    long rayTraceDisabledTime = -1;
    @Getter
    private ModeSetting targetesp = new ModeSetting("Отображение цели", "Кольцо", "Ромб", "Кольцо", "Призраки", "Не отображать");
    @Getter
    private BooleanSetting onlycrit = new BooleanSetting("Бить только критами", true);
    private BooleanSetting bypassWalls = new BooleanSetting("Обход стен RW", false);
    @Getter
    private Vector2f lerpRotation = Vector2f.ZERO;
    private Vector2f lerpRot = Vector2f.ZERO;
    private Vector2f lerprRot = Vector2f.ZERO;
    private int count;
    private int counter;
    @Getter
    private long cps = 0L;
    private float currentYawOffset = 0f;
    private float currentPitchOffset = 0f;
    private float targetYawOffset = 0f;
    private float targetPitchOffset = 0f;
    private long nextOffsetUpdateTime = 0;

    public AttackAura() {
        super("Attack Aura", "Автоматически атакует существ в радиусе", Category.Combat);
        addSettings(componentMode, checks, targets, sortMode, correctionType, targetesp, attackRange, rotateDistance, onlycrit, onlySpaceCritical, shielbreaker, SHEIS, bypassWalls, ray, attackThroughWalls);
    }

    public static float random(float min, float max) {
        return (float) (Math.random() * (max - min) + min);
    }

    @EventTarget
    public void onEvent(EventSwapWorld event) {
        reset();
    }

    @EventTarget
    public void onEvent(EventMoving event) {
        if (target == null || mc.player == null || mc.world == null) {
            canCrit = false;
            return;
        }
        final boolean fallCheck = mc.player.nextFallDistance != 0F;
        canCrit = !event.isToGround() && event.getFrom().y > event.getTo().y && fallCheck;
    }

    @EventTarget
    public void onInput(EventInput eventInput) {
        boolean rotateActive = RotationComponent.getInstance().isRotating();
        if (this.correctionType.is("Свободная") && rotateActive) {
            MoveUtil.fixMovement(eventInput, LookHandler.getFreeYaw());
        }
        if (this.correctionType.is("Фулл Таргет") && rotateActive) {
            MoveUtil.moveToPosition(eventInput, target.getPositionVec(), mc.player.rotationYaw);
        }
        if (tickSprint > 0) {
            eventInput.setForward(0);
            tickSprint--;
        }
    }

    // --- Добавлены недостающие методы ---

    @EventTarget
    public void test(EventWillLand eventWillLand) {
        if (hynix.getInstance().getModuleManager().getModule(AirStuck.class).isEnabled()) {
            canCrit = true;
            return;
        }
    }

    @EventTarget
    public void onEvent(EventUpdate e) {
        if (checks.is("Выключить после смерти") && !mc.player.isAlive()) {
            toggle();
            return;
        }
        if (target != null) {
            double dx = target.getPosX() - target.prevPosX;
            double dy = target.getPosY() - target.prevPosY;
            double dz = target.getPosZ() - target.prevPosZ;
            double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
            double speed = distance * 20.0;
            this.lastSpeed = speed;
        }
        if (target == null || !this.isValidTarget(target)) {
            target = this.findTarget();
        }
        if (target == null || mc.player == null || mc.world == null) {
            reset();
            return;
        }
        if (hynix.getInstance().getModuleManager().getModule(AirStuck.class).isEnabled()) {
            canCrit = true;
        }
        if (!hynix.getInstance().getModuleManager().getModule(PacketCriticals.class).isEnabled() && target != null) {
            if (canAttack() && onlycrit.get() && cps <= System.currentTimeMillis()) {
                updateAttack();
                cps = System.currentTimeMillis() + 460L;
            }
        }
        if (rayTrace()) rayTicks++;
        else rayTicks = 0;

        if (checkReturn()) return;
        if (componentMode.is("SpookyTime")) {
            if (ray.get()) {
                if (rayTicks > 1) updateAttack();
            } else {
                updateAttack();
            }
        } else {
            updateAttack();
        }
    }

    // ------------------------------------

    @EventTarget
    public void onEvent(EventPacket event) {
        IPacket<?> packet = event.getPacket();
        if (packet instanceof CHeldItemChangePacket) {
            perfectDelay.reset(650L);
        } else if (packet instanceof CAnimateHandPacket) {
            perfectDelay.reset(500L);
        }
    }

    @EventTarget
    public void Event(EventPostUpdate event) {
        if (hynix.getInstance().getModuleManager().getModule(PacketCriticals.class).isEnabled() && target != null) {
            if (canAttack() && onlycrit.get() && cps <= System.currentTimeMillis()) {
                updateAttack();
                cps = System.currentTimeMillis() + 460L;
            }
        }
    }

    @EventTarget
    public void onEvent(EventGameUpdate event) {
        if (target == null || mc.player == null || mc.world == null) {
            reset();
            return;
        }
        if (checkReturn()) return;
        updateRotation();
    }

    private void updateRotation() {
        double maxHeight = (AuraUtil.getStrictDistance((target)) / attackDistance());
        Vector3d vec = target.getPositionVec()
                .add(0, clamp(mc.player.getEyePosition(mc.getRenderPartialTicks()).y - target.getPosY(), 0, maxHeight), 0)
                .subtract(mc.player.getEyePosition(mc.getRenderPartialTicks()))
                .normalize();

        float rawYaw = (float) toDegrees(Math.atan2(-vec.x, vec.z));
        float rawPitch = (float) clamp(-toDegrees(Math.atan2(vec.y, Math.hypot(vec.x, vec.z))), -90F, 90F);

        float speed = new SecureRandom().nextBoolean() ? randomLerp(0.3F, 0.4F) : randomLerp(0.5F, 0.6F);

        float cos = (float) Math.cos(System.currentTimeMillis() / 70D);
        float sin = (float) Math.sin(System.currentTimeMillis() / 115D);
        float cosF = (float) Math.cos(System.currentTimeMillis() / 44D);

        float yawF = (float) Math.ceil(randomLerp(25F, 35) * cosF);
        float yaw = (float) Math.ceil(randomLerp(1F, 3) * cos);
        float pitch = (float) Math.ceil(randomLerp(1F, 2) * sin);
        float pitchF = (float) Math.ceil(randomLerp(7F, 15) * sin);

        if (componentMode.is("SpookyTime")) {
            yaw = 0;
            pitch = 0;
        }

        if (componentMode.is("PhotoGraf")) {
            int suck = count % 3;
            float random = stopWatch.getElapsedTime() / 40F + (count % 6);
            Rotation randomAngle = switch (suck) {
                case 0 -> new Rotation((float) Math.cos(random), (float) Math.sin(random));
                case 1 -> new Rotation((float) Math.sin(random), (float) Math.cos(random));
                case 2 -> new Rotation((float) Math.sin(random), (float) -Math.cos(random));
                default -> new Rotation((float) -Math.cos(random), (float) Math.sin(random));
            };

            float yawadd = randomLerp(3, 5) * randomAngle.getYaw();
            float pitch2 = randomLerp(0, 2) * (float) Math.cos((double) System.currentTimeMillis() / 5000);
            float pitchadd = randomLerp(2, 4) * randomAngle.getPitch() + pitch2;
            if (canCrit) pitchadd = yawadd = 0;
            float addition = (1F - cooldownFromLastSwing()) * (randomLerp(20, 40));
            yaw = (canCrit ? 0 : 19.23253f) * (count % 2 == 0 ? -1 : 1) + addition * (count % 2 == 0 ? -1 : 1) + yawadd;
            pitch = (-addition + pitchadd);
        }
        lerpRotation = new Vector2f(wrapLerp(speed, lerpRotation.x, rawYaw + yaw), wrapLerp(speed / 2F, lerpRotation.y, clamp(rawPitch + pitch, -90F, 90F)));
        lerpRot = new Vector2f(wrapLerp(speed, lerpRot.x, LookHandler.getFreeYaw() + yawF), wrapLerp(speed / 2F, lerpRot.y, clamp(pitchF, -90F, 90F)));
        lerprRot = new Vector2f(wrapLerp(speed, lerprRot.x, RotationUtil.calculateLimitedAim(target, 12).x + yaw), wrapLerp(speed, lerprRot.y, RotationUtil.calculateLimitedAim(target, 12).y + pitch));

        Rotation rRot = new Rotation((mc.player.rotationYaw + (float) Math.ceil(lerprRot.x - mc.player.rotationYaw)), (mc.player.rotationPitch + (float) Math.ceil(lerprRot.y - mc.player.rotationPitch)));
        Rotation rRotka = new Rotation(!canFTRotate() ? LookHandler.getFreeYaw() : (mc.player.rotationYaw + (float) Math.ceil(lerprRot.x - mc.player.rotationYaw)), !canFTRotate() ? LookHandler.getFreePitch() : (mc.player.rotationPitch + (float) Math.ceil(lerprRot.y - mc.player.rotationPitch)));
        Rotation rotation = new Rotation(mc.player.rotationYaw + (float) Math.ceil(lerpRotation.x - mc.player.rotationYaw), mc.player.rotationPitch + (float) Math.ceil(MathHelper.wrapDegrees(lerpRotation.y) - MathHelper.wrapDegrees(mc.player.rotationPitch)));

        float fov = (float) AuraUtil.calculateFOVFromCamera(target);
        float baseFov = 360;
        float sign = wrapDegrees(rotation.getYaw() - wrapDegrees(mc.player.rotationYaw));
        yawDelta = ((rotation.getYaw() - mc.player.rotationYaw) % 360 + 540) % 360 - 180;

        if (Math.abs(fov) < baseFov) {
            if (componentMode.is("PhotoGraf")) {
                if (target != null && mc.player.getDistance(target) < 0.75f) {
                    return;
                }
                double random = Math.random() * 2.5f;
                float yawSpeed = (float) (9 - random);
                float pitchSpeed = (float) (4.0f - random);
                float returnYawSpeed = (float) (18 + random);
                float returnPitchSpeed = (float) (18 + random);

                RotationComponent.update(rRot, yawSpeed, pitchSpeed, returnYawSpeed, returnPitchSpeed, 0, 1, false);
            }

            float attackDist = attackRange.get();
            float rotateDist = rotateDistance.get();
            float finalDist = attackDist + rotateDist + 0.1f;
            boolean rayTrace = RayTraceUtil.rayTraceSingleEntity(mc.player.rotationYaw, mc.player.rotationPitch, attackDist + finalDist, target);

            if (this.componentMode.is("Droid")) {
                double random = Math.random() * 1.0f;
                double yawSpeed = 0.0f;
                double pitchSpeed = 0.0f;
                long currentTime = System.currentTimeMillis();

                if (rayTrace) {
                    if (mc.player.getDistanceEye(target) > 0.5f) {
                        if (!RayTraceUtil.rayTraceSmallHitBox(mc.player.rotationYaw, mc.player.rotationPitch, finalDist, target)) {
                            yawSpeed = randomLerp(0.6f, 0.8f);
                            pitchSpeed = randomLerp(0.2f, 0.6f);
                        } else {
                            if (cooldownFromLastSwing() < 0.25f && MoveUtil.isMoving()) {
                                yawSpeed = randomLerp(-0.02f, 0.02f);
                                pitchSpeed = randomLerp(-0.01f, 0.01f);
                            }
                        }
                    }
                    rayTraceDisabledTime = -1;
                } else {
                    if (rayTraceDisabledTime == -1) {
                        rayTraceDisabledTime = currentTime;
                    }
                    long timeSinceDisabled = currentTime - rayTraceDisabledTime;
                    if (timeSinceDisabled < 400) {
                        double progressYaw = (timeSinceDisabled) / 400.0f;
                        double progressPitch = (timeSinceDisabled) / 600.0f;
                        double progressSpeedPitch = 12.0f;
                        double progressSpeedYaw = 24.0f;

                        if (mc.player.getDistanceEye(target) < 0.95f && MoveUtil.isMoving()) {
                            yawSpeed = randomLerp(8.0f, 12.0f);
                        } else {
                            yawSpeed = lerp(0.0f, (float) progressSpeedYaw, progressYaw);
                        }
                        pitchSpeed = lerp(0.0f, (float) progressSpeedPitch, progressPitch);

                        lastYaw = (float) yawSpeed;
                        lastPitch = (float) pitchSpeed;
                    } else {
                        yawSpeed = lastYaw + random;
                        pitchSpeed = lastPitch + random;
                    }
                }
                double returnYawSpeed = 8.0f + random;
                double returnPitchSpeed = 4.0f + random;
                this.updateCakeWorldRotation((float) yawSpeed, (float) pitchSpeed, (float) returnYawSpeed, (float) returnPitchSpeed);
            }

            if (componentMode.is("FunTime")) {
                Rotation rotacia = rotka(new Rotation(mc.player.rotationYaw, mc.player.rotationPitch), rRotka);
                float speedY = 15;
                if (canFTRotate()) speedY = 360;
                RotationComponent.update(rotacia, speedY, speedY, 45, 45, 10, 5, false);
            }
            if (componentMode.is("HolyWorld")) {
                float targetYaw = target.rotationYaw;
                double offset = 0.28;
                double ox = -MathHelper.sin(targetYaw * ((float) Math.PI / 180F)) * offset;
                double oz = MathHelper.cos(targetYaw * ((float) Math.PI / 180F)) * offset;
                Vector3d shifted;
                if (hynix.getInstance().getModuleManager().getModule(Speed.class).isEnabled()) {
                    shifted = new Vector3d(
                            target.getPosX() + ox,
                            target.getPosY() + target.getHeight() * 0.75f,
                            target.getPosZ() + oz
                    );
                } else {
                    shifted = new Vector3d(target.getPosX(), target.getPosY() + target.getHeight() * 0.75f, target.getPosZ());
                }

                Vector3d playerPosition = mc.player.getEyePosition(1.0F);
                Vector3d direction = shifted.subtract(playerPosition).normalize();
                float yawToTarget = (float) Math.toDegrees(Math.atan2(-direction.x, direction.z));
                float pitchToTarget = (float) MathHelper.clamp((int) Math.toDegrees(Math.asin(-direction.y)), -88, 88);
                float yawDelta = wrapDegrees(yawToTarget - lerpRotation.x);
                float pitchDelta = pitchToTarget - lerpRotation.y;
                float clampedYaw = Math.min(Math.max(Math.abs(yawDelta), 0), 100);
                float clampedPitch = Math.min(Math.max(Math.abs(pitchDelta), 0), 30);
                cos = (float) Math.cos(System.currentTimeMillis() / 100D);
                float xAnims = (float) Math.ceil(randomLerp(6, 12) * cos);

                sin = (float) Math.sin(System.currentTimeMillis() / 100D);
                float yAnims = (float) Math.ceil(randomLerp(2, 4) * sin);
                yaw = lerpRotation.x + (yawDelta > 0 ? clampedYaw : -clampedYaw);
                pitch = lerpRotation.y + (pitchDelta > 0 ? clampedPitch : -clampedPitch);

                float gcd = SensUtil.getGCDValue();
                yaw -= (yaw - lerpRotation.x) % gcd;
                pitch -= (pitch - lerpRotation.y) % gcd;

                lerpRotation = new Vector2f(yaw, pitch);

                rotation = new Rotation(
                        mc.player.rotationYaw + (float) Math.ceil(MathHelper.wrapDegrees(yaw + xAnims) - MathHelper.wrapDegrees(mc.player.rotationYaw)),
                        mc.player.rotationPitch + (float) Math.ceil(MathHelper.wrapDegrees(pitch + yAnims) - MathHelper.wrapDegrees(mc.player.rotationPitch))
                );
                float speedd;
                if (hynix.getInstance().getModuleManager().getModule(Speed.class).isEnabled()) {
                    speedd = 1;
                } else {
                    speedd = 3;
                }
                SmoothRotationComponent.update(rotation, speedd, 5F, 1.5F, 1.5F, 1, 5, false);
            }
            if (componentMode.is("SpookyTime")) {
                Vector3d vecs = new Vector3d(target.getPosX(), target.getPosY() + target.getHeight() * 0.8f, target.getPosZ());
                Vector3d playerPosition = mc.player.getEyePosition(1.0F);
                Vector3d direction = vecs.subtract(playerPosition).normalize();

                float rawYaws = (float) toDegrees(Math.atan2(-direction.x, direction.z));
                float rawPitchs = (float) clamp(-toDegrees(Math.atan2(direction.y, Math.hypot(direction.x, direction.z))), -89.0f, 89.0f);

                float yawDelta = MathHelper.wrapDegrees(rawYaws - lerpRotation.x);
                float pitchDelta = MathHelper.wrapDegrees(rawPitchs - lerpRotation.y);

                float clampedYaw = Math.min(Math.max(Math.abs(yawDelta), 0), yawSpeed(1));
                float clampedPitch = Math.min(Math.max(Math.abs(pitchDelta), 0), 8);

                double distance = playerPosition.distanceTo(vecs);
                double bbWidth = target.getWidth();
                double bbHeight = target.getHeight();

                float maxYawOffset = (float) toDegrees(Math.atan((bbWidth / 2) / distance));
                float maxPitchOffset = (float) toDegrees(Math.atan((bbHeight / 2) / distance));

                float finalYaw = lerpRotation.x + (yawDelta > 0 ? clampedYaw : -clampedYaw);
                float finalPitch = lerpRotation.y + (pitchDelta > 0 ? clampedPitch : -clampedPitch);

                long now = System.currentTimeMillis();
                Random rng = new Random();
                if (rayTrace()) {
                    if (now >= nextOffsetUpdateTime) {
                        targetYawOffset = rng.nextFloat() * 32f - 16f;
                        targetPitchOffset = rng.nextFloat() * 24f - 12f;

                        targetYawOffset = clamp(targetYawOffset, -maxYawOffset, maxYawOffset);
                        targetPitchOffset = clamp(targetPitchOffset, -maxPitchOffset, maxPitchOffset);

                        nextOffsetUpdateTime = now + 80 + rng.nextInt(70);
                    }

                    float offsetLerp = 0.4f;
                    currentYawOffset += (targetYawOffset - currentYawOffset) * offsetLerp;
                    currentPitchOffset += (targetPitchOffset - currentPitchOffset) * offsetLerp;
                } else {
                    currentYawOffset = 0f;
                    currentPitchOffset = 0f;
                }

                finalYaw += currentYawOffset;
                finalPitch += currentPitchOffset;
                finalPitch = clamp(finalPitch, -89.0f, 89.0f);

                float gcd = SensUtil.getGCDValue();
                finalYaw -= (finalYaw - lerpRotation.x) % gcd;
                finalPitch -= (finalPitch - lerpRotation.y) % gcd;

                lerpRotation = new Vector2f(finalYaw, finalPitch);

                Rotation rotation2 = new Rotation(
                        mc.player.rotationYaw + (float) Math.ceil(MathHelper.wrapDegrees(finalYaw) - MathHelper.wrapDegrees(mc.player.rotationYaw)),
                        mc.player.rotationPitch + (float) Math.ceil(MathHelper.wrapDegrees(finalPitch) - MathHelper.wrapDegrees(mc.player.rotationPitch))
                );

                SmoothRotationComponent.update(rotation2, 3F, 10F, 4F, 4F, 1, 5, false);
            }
        }

        if (componentMode.is("Reallyworld")) {
            fastRotation();
        }
    }

    private boolean canFTRotate() {
        return componentMode.is("FunTime");
    }

    private void updateCakeWorldRotation(float yaw, float pitch, float returnYaw, float returnPitch) {
        RotationComponent.update(new Rotation(yaw, pitch), yaw, pitch, returnYaw, returnPitch, 0, 15, false);
    }

    public float lerp(float input, float target, double step) {
        return (float) (input + step * (target - input));
    }

    private void fastRotation() {
        if (target == null || mc.player == null || mc.world == null) {
            return;
        }

        float currentYaw = mc.player.rotationYaw;
        float currentPitch = mc.player.rotationPitch;

        float deltaYaw = MathHelper.wrapDegrees(lerpRotation.x - currentYaw);
        float deltaPitch = MathHelper.wrapDegrees(lerpRotation.y - currentPitch);

        float newYaw = currentYaw + deltaYaw;
        float newPitch = currentPitch + deltaPitch;

        newYaw += MathUtil.random(-3, 3);
        newPitch += MathUtil.random(-2, 2);

        RotationComponent.update(new Rotation(newYaw, newPitch), 180, 180, 0, 5);
    }

    private Rotation rotka(Rotation currentAngle, Rotation targetAngle) {
        int count = counter;
        float angleYaw = MathHelper.wrapDegrees(targetAngle.getYaw() - currentAngle.getYaw());
        float anglePitch = MathHelper.wrapDegrees(targetAngle.getPitch() - currentAngle.getPitch());
        Rotation angleDelta = new Rotation(angleYaw, anglePitch);
        float yawDelta = angleDelta.getYaw(), pitchDelta = angleDelta.getPitch();
        float rotationDifference = (float) Math.hypot(Math.abs(yawDelta), Math.abs(pitchDelta));

        if (canFTRotate()) {
            float speed = canCrit ? 1 : new SecureRandom().nextBoolean() ? 0.2F : 0.1F;

            float lineYaw = (Math.abs(yawDelta / rotationDifference) * 180);
            float linePitch = (Math.abs(pitchDelta / rotationDifference) * 180);

            float moveYaw = clamp(yawDelta, -lineYaw, lineYaw);
            float movePitch = clamp(pitchDelta, -linePitch, linePitch);

            Rotation moveAngle = new Rotation(currentAngle.getYaw(), currentAngle.getPitch());
            moveAngle.setYaw(MathHelper.lerp(randomLerp(speed, speed + 0.2F), currentAngle.getYaw(), currentAngle.getYaw() + moveYaw));
            moveAngle.setPitch(MathHelper.lerp(randomLerp(speed, speed + 0.2F), currentAngle.getPitch(), currentAngle.getPitch() + movePitch));

            return moveAngle;
        } else {
            int suck = count % 3;
            float speed = stopWatch.finished(400) ? new SecureRandom().nextBoolean() ? 0.4F : 0.2F : -0.2F;
            float random = stopWatch.getElapsedTime() / 40F + (count % 6);

            Rotation randomAngle = switch (suck) {
                case 0 -> new Rotation((float) Math.cos(random), (float) Math.sin(random));
                case 1 -> new Rotation((float) Math.sin(random), (float) Math.cos(random));
                case 2 -> new Rotation((float) Math.sin(random), (float) -Math.cos(random));
                default -> new Rotation((float) -Math.cos(random), (float) Math.sin(random));
            };

            float yaw = randomLerp(5, 8) * randomAngle.getYaw();
            float pitch2 = randomLerp(0, 2) * (float) Math.cos((double) System.currentTimeMillis() / 5000);
            float pitch = randomLerp(2, 6) * randomAngle.getPitch() + pitch2;

            float lineYaw = (Math.abs(yawDelta / rotationDifference) * 180);
            float linePitch = (Math.abs(pitchDelta / rotationDifference) * 180);

            float moveYaw = clamp(yawDelta, -lineYaw, lineYaw);
            float movePitch = clamp(pitchDelta, -linePitch, linePitch);

            Rotation moveAngle = new Rotation(currentAngle.getYaw(), currentAngle.getPitch());
            moveAngle.setYaw(MathHelper.lerp(clamp(randomLerp(speed, speed + 0.2F), 0, 1), currentAngle.getYaw(), currentAngle.getYaw() + moveYaw) + yaw);
            moveAngle.setPitch(MathHelper.lerp(clamp(randomLerp(speed, speed + 0.2F), 0, 1), currentAngle.getPitch(), currentAngle.getPitch() + movePitch) + pitch);

            return moveAngle;
        }
    }

    private float yawSpeed(float diff) {
        return clamp(
                Math.abs(diff),
                random(35, 60),
                random(300, 450)
        );
    }

    @Override
    public void onDisable() {
        super.onDisable();
        target = null;
        if (componentMode.is("FunTime")) {
            RotationComponent.update(new Rotation(LookHandler.getFreeYaw(), LookHandler.getFreePitch()), 20, 20, 30, 30, 0, 30, false);
        }
        counter = 9;
        lerpRotation = Vector2f.ZERO;
        lerpRot = Vector2f.ZERO;
    }

    @Override
    public void onEnable() {
        super.onEnable();
        lerpRotation = new Vector2f(mc.player.rotationYaw, mc.player.rotationPitch);
        lerpRot = new Vector2f(mc.player.rotationYaw, mc.player.rotationPitch);
    }

    public float wrapLerp(float step, float input, float target) {
        return input + step * MathHelper.wrapDegrees(target - input);
    }

    public float randomLerp(float min, float max) {
        return MathHelper.lerp(new SecureRandom().nextFloat(), min, max);
    }

    public float cooldownFromLastSwing() {
        return clamp(mc.player.ticksSinceLastSwing / randomLerp(8, 12), 0.0F, 1.0F);
    }

    private void updateAttack() {
        Sprint autoSprint = (Sprint) hynix.getInstance().getModuleManager().getModule(Sprint.class);
        if (canAttack() && rayTrace() && AuraUtil.getStrictDistance(target) < attackDistance()) {

            if (autoSprint.getMode().is("Пакетный") && autoSprint.RAGE() && CEntityActionPacket.lastUpdatedSprint) {
                mc.player.connection.sendPacket(new CEntityActionPacket(mc.player, CEntityActionPacket.Action.STOP_SPRINTING));
            }

            if (bypassWalls.get()) {
                Vector3d startVec = mc.player.getEyePosition(mc.getRenderPartialTicks());
                Vector3d targetPos = target.getEyePosition(mc.getRenderPartialTicks());
                Vector3d direction = targetPos.subtract(startVec);
                double distance = direction.length();

                if (distance < 1.0E-3) return;

                Vector3d normalizedDir = direction.normalize();
                for (double i = 0; i < distance; i += 0.5) {
                    Vector3d point = startVec.add(normalizedDir.scale(i));
                    BlockPos pos = new BlockPos(point);
                    if (!mc.world.isAirBlock(pos)) {
                        mc.player.connection.sendPacket(new CPlayerDiggingPacket(CPlayerDiggingPacket.Action.START_DESTROY_BLOCK, pos, Direction.UP));
                        mc.player.connection.sendPacket(new CPlayerDiggingPacket(CPlayerDiggingPacket.Action.STOP_DESTROY_BLOCK, pos, Direction.UP));
                    }
                }
            }
            if (target instanceof PlayerEntity player) {
                if (this.shielbreaker.get()) {
                    this.breakShieldPlayer(player);
                }
            }

            if (this.SHEIS.get() && mc.player.isBlocking()) {
                mc.playerController.onStoppedUsingItem(mc.player);
            }

            attackEntity(target);
            if (autoSprint.getMode().is("Пакетный") && autoSprint.RAGE()) {
                mc.player.connection.sendPacket(new CEntityActionPacket(mc.player, CEntityActionPacket.Action.START_SPRINTING));
            }

            stopWatch.reset();

            count = (count + 1) % 2;
            counter++;
            canCrit = false;

        } else if (!mc.player.canEntityBeSeen(target) && ServerUtil.isConnectedToServer("holyworld") && mc.player.getServerBrand().contains("HolyWorld")) {
            RotationComponent.update(new Rotation(Rotation.cameraYaw(), 90), 360, 360, 0, 5);
        }
    }

    private void breakShieldPlayer(PlayerEntity entity) {
        if (entity.isBlocking()) {
            int invSlot = InventoryUtil.getAxe(false);
            int hotBarSlot = InventoryUtil.getAxe(true);
            int bestSlot = InventoryUtil.findBestSlotInHotBar();
            if (hotBarSlot == -1 && invSlot != -1) {
                InventoryUtil.switchItem(Items.NETHERITE_AXE, 7, true, 300);
                mc.player.connection.sendPacket(new CHeldItemChangePacket(7));
                mc.playerController.attackEntity(mc.player, entity);
                mc.player.swingArm(Hand.MAIN_HAND);
                mc.player.connection.sendPacket(new CHeldItemChangePacket(mc.player.inventory.currentItem));
            }
            if (hotBarSlot != -1) {
                mc.player.connection.sendPacket(new CHeldItemChangePacket(hotBarSlot));
                mc.playerController.attackEntity(mc.player, entity);
                mc.player.swingArm(Hand.MAIN_HAND);
                mc.player.connection.sendPacket(new CHeldItemChangePacket(mc.player.inventory.currentItem));
            }
        }
    }

    private boolean checkReturn() {
        return mc.player.isHandActive() && checks.is("Не бить когда ешь") || (!(mc.player.getHeldItemMainhand().getItem() instanceof AxeItem || mc.player.getHeldItemMainhand().getItem() instanceof SwordItem) && checks.is("Бить только с оружием"));
    }

    private void attackEntity(Entity entity) {
        mc.playerController.attackEntity(mc.player, entity);
        mc.player.swingArm(Hand.MAIN_HAND);
    }

    public boolean canAttack() {
        boolean ready = stopWatch.hasTimeElapsed(450) && mc.player.getCooledAttackStrength(checks.is("TPSSync") ? TPSHandler.getAdjustTicks() : 0.5F) > 0.9F;
        boolean air = mc.player.movementInput.jump || !mc.player.isOnGround();

        if (AuraUtil.isJumpBlockedByCeiling()) {
            return ready;
        } else if (hynix.getInstance().getModuleManager().getModule(PacketCriticals.class).isEnabled()) {
            return ready && (!onlycrit.get() || !mc.player.isValidAttackCondition() || (onlySpaceCritical.get() && !air) || mc.player.canCritical());
        } else {
            return ready && (!onlycrit.get() || !mc.player.isValidAttackCondition() || (onlySpaceCritical.get() && !air) || (mc.player.canCritical() && canCrit));
        }
    }

    private LivingEntity findTarget() {
        List<LivingEntity> targets = new ArrayList<>();

        for (Entity entity : mc.world.getAllEntities()) {
            if (entity instanceof LivingEntity && isValidTarget((LivingEntity) entity)) {
                targets.add((LivingEntity) entity);
            }
        }

        if (targets.isEmpty()) {
            return null;
        }

        if (targets.size() > 1) {
            switch (sortMode.get()) {
                case "Всему сразу" -> targets.sort(Comparator.comparingDouble(target -> {
                    if (target instanceof PlayerEntity playerEntity) return -getEntityArmor(playerEntity);
                    if (target instanceof LivingEntity livingEntity) return -livingEntity.getTotalArmorValue();
                    return 0;
                }).thenComparing((object1, object2) -> {
                    double health1 = getEntityHealth((LivingEntity) object1);
                    double health2 = getEntityHealth((LivingEntity) object2);
                    return Double.compare(health1, health2);
                }).thenComparing((object1, object2) -> {
                    double distance1 = mc.player.getDistance((LivingEntity) object1);
                    double distance2 = mc.player.getDistance((LivingEntity) object2);
                    return Double.compare(distance1, distance2);
                }));
                case "Дистанции" -> targets.sort(Comparator.comparingDouble(entity -> mc.player.getDistance(entity)));
                case "Броне" ->
                        targets.sort(Comparator.comparingDouble(entity -> entity instanceof PlayerEntity ? getEntityArmor((PlayerEntity) entity) : entity.getTotalArmorValue()));
                case "Здоровью" -> targets.sort(Comparator.comparingDouble(this::getEntityHealth));
            }
        }

        return targets.get(0);
    }

    public double getEntityArmor(PlayerEntity target) {
        double totalArmor = 0.0D;

        for (ItemStack armorStack : target.inventory.armorInventory) {
            if (armorStack != null && armorStack.getItem() instanceof ArmorItem) {
                totalArmor += this.getProtectionLvl(armorStack);
            }
        }

        return totalArmor;
    }

    public double getEntityHealth(Entity ent) {
        if (ent instanceof PlayerEntity player) {
            double armorValue = this.getEntityArmor(player) / 20.0D;
            return (double) (player.getHealth() + player.getAbsorptionAmount()) * armorValue;
        } else if (ent instanceof LivingEntity livingEntity) {
            return livingEntity.getHealth() + livingEntity.getAbsorptionAmount();
        } else {
            return 0.0D;
        }
    }

    private double getProtectionLvl(ItemStack stack) {
        ArmorItem armor = (ArmorItem) stack.getItem();
        double damageReduce = armor.getDamageReduceAmount();
        if (stack.isEnchanted()) {
            damageReduce += (double) EnchantmentHelper.getEnchantmentLevel(Enchantments.PROTECTION, stack) * 0.25D;
        }

        return damageReduce;
    }

    private boolean isValidTarget(LivingEntity entity) {
        if (entity instanceof ClientPlayerEntity) return false;

        if (entity.ticksExisted < 3) return false;
        if (mc.player.getDistanceEyePos(entity) >= getMaxAimRange()) return false;

        if (entity instanceof PlayerEntity playerEntity) {
            if (hynix.getInstance().getModuleManager().getModule(AntiBot.class).isEnabled() && AntiBot.bot.contains(playerEntity)) {
                return false;
            }
            if (!targets.is("Друзья") && hynix.getInstance().getFriendManager().isFriend(playerEntity.getName().getString())) {
                return false;
            }
            if (playerEntity.getName().getString().equalsIgnoreCase(mc.player.getName().getString())) return false;
        }

        if (entity instanceof PlayerEntity && entity.getTotalArmorValue() == 0 && !targets.is("Голые")) return false;
        if (entity instanceof PlayerEntity && !targets.is("Игроки")) return false;
        if ((entity instanceof MonsterEntity || entity instanceof PhantomEntity || entity instanceof BatEntity || entity instanceof ShulkerEntity || entity instanceof VillagerEntity || entity instanceof WanderingTraderEntity || entity instanceof SlimeEntity || entity instanceof IronGolemEntity) && !targets.is("Мобы"))
            return false;
        if ((entity instanceof AnimalEntity || entity instanceof SalmonEntity || entity instanceof TropicalFishEntity || entity instanceof CodEntity || entity instanceof SquidEntity || entity instanceof DolphinEntity) && !targets.is("Животные"))
            return false;

        // ВОТ ЗДЕСЬ БЫЛА ПРОПУЩЕНА СКОБКА, КОТОРАЯ ЛОМАЛА ВЕСЬ ФАЙЛ
        return !entity.isInvulnerable() && entity.isAlive() && !(entity instanceof ArmorStandEntity);
    }

    public boolean rayTrace() {
        return (RayTraceUtil.rayTraceEntity(mc.player.rotationYaw, mc.player.rotationPitch, attackDistance(), target, attackThroughWalls.get()));
    }

    public double getMaxRange() {
        float originalDistance = this.componentMode.is("Легитная") ? 0.2f : 0.0f;
        return (double) this.attackRange.get() - originalDistance;
    }

    public double getMaxAimRange() {
        float attackDist = attackRange.get();
        float rotateDist = rotateDistance.get();
        float originalAimDistance = this.componentMode.is("Легитная") ? 0.2f : 0.0f;
        return mc.player.isElytraFlying() ? attackDist : attackDist + rotateDist - originalAimDistance;
    }

    public double attackDistance() {
        return Math.max(mc.playerController.extendedReach() ? 6.0D : 3.0D, attackRange.get());
    }

    private void reset() {
        target = null;
        canCrit = false;
        adjYaw = 0;
        adjPitch = 0;
        preLastYaw = 0;
        preLastPitch = 0;
        rayTraceDisabledTime = -1;
    }
}