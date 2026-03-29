package su.hynix.modules.impl.combat;

import com.darkmagician6.eventapi.EventTarget;
import lombok.Getter;
import net.minecraft.client.entity.player.RemoteClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ArmorStandEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.play.client.CClickWindowPacket;
import net.minecraft.network.play.client.CCloseWindowPacket;
import net.minecraft.network.play.client.CEntityActionPacket;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.util.Hand;
import su.hynix.events.*;
import su.hynix.handlers.impl.TPSHandler;
import su.hynix.hynix;
import su.hynix.modules.Category;
import su.hynix.modules.Module;
import su.hynix.modules.api.constructors.impl.BooleanSetting;
import su.hynix.modules.api.constructors.impl.MultiBooleanSetting;
import su.hynix.modules.api.constructors.impl.SliderSetting;
import su.hynix.modules.impl.movement.AirStuck;
import su.hynix.utils.math.AuraUtil;
import su.hynix.utils.math.TimeUtil;
import su.hynix.utils.misc.TargetUtil;
import su.hynix.utils.player.InventoryUtil;
import su.hynix.utils.player.RayTraceUtil;

import java.util.Comparator;
import java.util.stream.StreamSupport;

@Getter
@SuppressWarnings("all")
public class TriggerBot extends Module {
    public LivingEntity target = null;
    private MultiBooleanSetting targets = new MultiBooleanSetting("Кого атаковать", new BooleanSetting("Игроков", true), new BooleanSetting("Друзей", false), new BooleanSetting("Голых", true), new BooleanSetting("Жителей", false), new BooleanSetting("Животных", false), new BooleanSetting("Мобов", false));
    private SliderSetting distance = new SliderSetting("Радиус атаки", 3.0f, 2.0f, 5.0f, 0.1F);
    private MultiBooleanSetting no_hit = new MultiBooleanSetting("Не бить если", new BooleanSetting("Открыт контейнер", true), new BooleanSetting("Используешь еду", false));
    private BooleanSetting onlycrit = new BooleanSetting("Бить только критами", true);
    private BooleanSetting shieldbreak = new BooleanSetting("Ломать щит", true);
    private BooleanSetting smartcrit = new BooleanSetting("Только при зажатом пробеле", false, () -> onlycrit.get());
    private BooleanSetting noThroughWalls = new BooleanSetting("Не бить через стены", false);
    private BooleanSetting tpsync = new BooleanSetting("Синхронизация с ТПС", false);
    private TimeUtil attackTimer = new TimeUtil();
    private int legitSprintResetTicks;
    private boolean canCrit;
    private boolean raytraced;

    public TriggerBot() {
        super("Trigger Bot", "Автоматически атакует существ в радиусе", Category.Combat);
        addSettings(targets, no_hit, distance, noThroughWalls, onlycrit, smartcrit, tpsync, shieldbreak);
    }

    @EventTarget
    public void onEvent(EventInteract event) {
        if (target != null) event.setCancelled(true);
    }

    @EventTarget
    public void onEvent(EventWillLand event) {
        if (target == null) {
            canCrit = false;
            return;
        }
        if (hynix.getInstance().getModuleManager().getModule(AirStuck.class).isEnabled()) {
            canCrit = true;
            return;
        }
        if (mc.player.fallDistance > 0.1F && event.isWillLand()) {
            canCrit = false;
        } else {
            canCrit = true;
        }
    }

    @EventTarget
    public void onEvent(EventSwapWorld event) {
        target = null;
    }

    @EventTarget
    public void onUpdate(EventUpdate event) {
        target = findTarget();
        if (hynix.getInstance().getModuleManager().getModule(AirStuck.class).isEnabled()) {
            canCrit = true;
        }
        raytraced = target == null || !RayTraceUtil.rayTraceEntity(mc.player.rotationYaw, mc.player.rotationPitch, distance.get(), target, noThroughWalls.get());
        if (!hynix.getInstance().getModuleManager().getModule(PacketCriticals.class).isEnabled()) {
            attack();
        }
    }

    @EventTarget
    public void Event(EventPostUpdate event) {
        if (hynix.getInstance().getModuleManager().getModule(PacketCriticals.class).isEnabled()) {
            attack();
        }
    }

    @EventTarget
    private void onInput(EventInput e) {
        if (legitSprintResetTicks > 0) {
            e.setForward(0.0F);
            e.setStrafe(0.0F);
            --legitSprintResetTicks;
        }
    }

    private void attack() {
        if (raytraced) return;
        if (canAttack()) {

            boolean sprint = mc.player.serverSprintState && !mc.player.isInWater() && !mc.player.isInLava() && !mc.player.isSwimming();
            if (sprint) {
                legitSprintResetTicks = 1;
                if (mc.player.serverSprintState) {
                    mc.player.setServerSprintState(false);
                    mc.player.setSprinting(false);
                    mc.player.connection.sendPacket(new CEntityActionPacket(mc.player, CEntityActionPacket.Action.STOP_SPRINTING));
                }
            }

            resolvePlayers();
            mc.playerController.attackEntity(mc.player, target);
            mc.player.swingArm(Hand.MAIN_HAND);
            releaseResolver();
            canCrit = false;
            attackTimer.reset();
            shieldBreaker();
        }
    }


    public boolean canAttack() {
        boolean ready = attackTimer.hasTimeElapsed(450) && mc.player.getCooledAttackStrength(tpsync.get() ? TPSHandler.getAdjustTicks() : 0.5F) > 0.9F;
        boolean air = mc.player.movementInput.jump || !mc.player.isOnGround();
        if ((no_hit.is("Используешь еду") && mc.player.isEating()) || (no_hit.is("Открыт контейнер") && mc.currentScreen != null && mc.currentScreen != hynix.getInstance().getDropDown())) {
            return false;
        }

        if (AuraUtil.isJumpBlockedByCeiling()) {
            return ready;
        } else if (hynix.getInstance().getModuleManager().getModule(PacketCriticals.class).isEnabled()) {
            return ready && (!onlycrit.get() || !mc.player.isValidAttackCondition() || (smartcrit.get() && !air) || mc.player.canCritical());
        } else {
            return ready && (!onlycrit.get() || !mc.player.isValidAttackCondition() || (smartcrit.get() && !air) || (mc.player.canCritical() && canCrit));
        }
    }


    private boolean shieldBreaker() {
        if (!shieldbreak.get()) {
            return false;
        }
        int axeSlot = InventoryUtil.findAxeSlot();
        if (target.getActiveItemStack().getItem() == Items.SHIELD && axeSlot != -1) {
            if (axeSlot < 9) {
                mc.getConnection().sendPacket(new CHeldItemChangePacket(axeSlot));
                mc.playerController.attackEntity(mc.player, target);
                mc.player.swingArm(Hand.MAIN_HAND);
                mc.getConnection().sendPacket(new CHeldItemChangePacket(mc.player.inventory.currentItem));
            } else {
                mc.getConnection().sendPacket(new CClickWindowPacket(mc.player.container.windowId, axeSlot, mc.player.inventory.currentItem, ClickType.SWAP, ItemStack.EMPTY, mc.player.openContainer.getNextTransactionID(mc.player.inventory)));
                mc.getConnection().sendPacket(new CCloseWindowPacket(mc.player.container.windowId));
                mc.playerController.attackEntity(mc.player, target);
                mc.player.swingArm(Hand.MAIN_HAND);
                mc.getConnection().sendPacket(new CClickWindowPacket(mc.player.container.windowId, axeSlot, mc.player.inventory.currentItem, ClickType.SWAP, ItemStack.EMPTY, mc.player.openContainer.getNextTransactionID(mc.player.inventory)));
                mc.getConnection().sendPacket(new CCloseWindowPacket(mc.player.container.windowId));
            }
            return true;
        }
        return false;
    }

    private LivingEntity findTarget() {
        return StreamSupport.stream(mc.world.getAllEntities().spliterator(), false)
                .filter(e -> e instanceof LivingEntity)
                .map(e -> (LivingEntity) e)
                .filter(this::isTarget)
                .filter(entity -> RayTraceUtil.rayTraceEntity(mc.player.rotationYaw, mc.player.rotationPitch, distance.get(), entity, noThroughWalls.get()))
                .sorted(Comparator.comparingDouble(entity -> entity.getDistance(mc.player)))
                .findFirst().orElse(null);
    }

    private boolean isTarget(LivingEntity entity) {
        if (entity == null || !entity.isAlive() || entity == mc.player || entity instanceof ArmorStandEntity) {
            return false;
        }
        float baseDistance = distance.get().floatValue();
        if (entity instanceof PlayerEntity player) {
            if (hynix.getInstance().getModuleManager().getModule(AntiBot.class).isEnabled() && AntiBot.bot.contains(player)) {
                return false;
            }
        }
        return TargetUtil.isPlayerTarget(entity, targets, true) || TargetUtil.isVillagerTarget(entity, targets) || TargetUtil.isAnimalTarget(entity, targets) || TargetUtil.isMobTarget(entity, targets);
    }

    public void resolvePlayers() {
        for (PlayerEntity player : mc.world.getPlayers()) {
            if (player instanceof RemoteClientPlayerEntity) {
                ((RemoteClientPlayerEntity) player).resolve();
            }
        }
    }

    public void releaseResolver() {
        for (PlayerEntity player : mc.world.getPlayers()) {
            if (player instanceof RemoteClientPlayerEntity) {
                ((RemoteClientPlayerEntity) player).releaseResolver();
            }
        }
    }

    @Override
    public void onDisable() {
        target = null;
        canCrit = false;
        super.onDisable();
    }
}