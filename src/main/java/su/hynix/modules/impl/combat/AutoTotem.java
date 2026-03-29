package su.hynix.modules.impl.combat;

import com.darkmagician6.eventapi.EventTarget;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.EnderCrystalEntity;
import net.minecraft.entity.item.TNTEntity;
import net.minecraft.entity.item.minecart.TNTMinecartEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.play.client.CCloseWindowPacket;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.network.play.client.CPickItemPacket;
import net.minecraft.network.play.client.CPlayerDiggingPacket;
import net.minecraft.network.play.server.SSpawnObjectPacket;
import net.minecraft.potion.Effects;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import su.hynix.component.impl.MoveComponent;
import su.hynix.events.EventPacket;
import su.hynix.events.EventUpdate;
import su.hynix.handlers.impl.PickItemFixHandler;
import su.hynix.modules.Category;
import su.hynix.modules.Module;
import su.hynix.modules.api.constructors.impl.BooleanSetting;
import su.hynix.modules.api.constructors.impl.ModeSetting;
import su.hynix.modules.api.constructors.impl.MultiBooleanSetting;
import su.hynix.modules.api.constructors.impl.SliderSetting;
import su.hynix.utils.player.MoveUtil;

import java.util.List;

public class AutoTotem extends Module {

    public final ModeSetting swapMode = new ModeSetting("Режим", "Default", "Default", "Blatant");
    private final SliderSetting health = new SliderSetting("Уровень здоровья", 4, 1, 20, 0.5F);
    private final SliderSetting elytraHealth = new SliderSetting("Здоровье на элитре", 8, 1, 20, 0.5F);
    private final BooleanSetting swapBack = new BooleanSetting("Возвращать предмет", true);
    private final BooleanSetting noBallSwitch = new BooleanSetting("Игнорировать проверки, если в руке шар", false);
    private final BooleanSetting saveEnchanted = new BooleanSetting("Сохранять зачарованный", true);
    private final BooleanSetting matrixBypass = new BooleanSetting("Обход Matrix", true);
    private final BooleanSetting stop = new BooleanSetting("Полная остановка", false, () -> (!matrixBypass.get() && swapMode.is("Default")));
    private final MultiBooleanSetting mode = new MultiBooleanSetting("Проверки на", new BooleanSetting("Золотые сердца", true), new BooleanSetting("Кристаллы", true), new BooleanSetting("Падение", true), new BooleanSetting("Динамит", true), new BooleanSetting("Трезубец", false));
    private final SliderSetting distancecrystall = new SliderSetting("Дистанция проверки на кристалл", 6, 1, 6, 1, () -> mode.is("Кристаллы"));

    private int nonEnchantedTotems, oldItem = -1, ticks;

    public AutoTotem() {
        super("Auto Totem", "Автоматически помещает тотем бессмертия во вторую руку", Category.Combat);
        addSettings(health, elytraHealth, swapMode, swapBack, noBallSwitch, saveEnchanted, matrixBypass, stop, mode, distancecrystall);
    }

    @EventTarget
    private void onUpdate(EventUpdate event) {
        ticks++;

        Slot slot = findTotemSlotAndUpdateCount();

        if (ticks >= 15) {
            if (!isSwapBlockedByBall() && !hasTotemInHand()) {
                if (mode.is("Кристаллы")) {
                    if (!mc.world.getEntitiesWithinAABB(EnderCrystalEntity.class, mc.player.getBoundingBox().grow(distancecrystall.get())).isEmpty() && slot != null) {
                        doSwap(slot.slotNumber);
                        return;
                    }
                }
                if (mode.is("Динамит")) {
                    if (!mc.world.getEntitiesWithinAABB(TNTEntity.class, mc.player.getBoundingBox().grow(8)).isEmpty() && slot != null) {
                        doSwap(slot.slotNumber);
                        return;
                    }
                    if (!mc.world.getEntitiesWithinAABB(TNTMinecartEntity.class, mc.player.getBoundingBox().grow(8)).isEmpty() && slot != null) {
                        doSwap(slot.slotNumber);
                        return;
                    }
                }
                if (mode.is("Трезубец")) {
                    List<TridentEntity> nearbyTridents = mc.world.getEntitiesWithinAABB(
                            TridentEntity.class,
                            mc.player.getBoundingBox().grow(10)
                    );


                    boolean isAnyTridentMoving = nearbyTridents.stream()
                            .anyMatch(trident ->
                                    trident.getPosX() != trident.prevPosX ||
                                            trident.getPosY() != trident.prevPosY ||
                                            trident.getPosZ() != trident.prevPosZ
                            );

                    if (isAnyTridentMoving) {
                        doSwap(slot.slotNumber);
                        return;
                    }
                }
            }

            if (needTotem()) {
                if (slot != null && !hasTotemInHand()) {
                    doSwap(slot.slotNumber);
                }
            } else if (oldItem != -1 && swapBack.get()) {
                if (mode.is("Кристаллы") && !mc.world.getEntitiesWithinAABB(EnderCrystalEntity.class, mc.player.getBoundingBox().grow(distancecrystall.get())).isEmpty()) {
                    return;
                }
                if (mode.is("Динамит") && (!mc.world.getEntitiesWithinAABB(TNTMinecartEntity.class, mc.player.getBoundingBox().grow(8)).isEmpty() || !mc.world.getEntitiesWithinAABB(TNTEntity.class, mc.player.getBoundingBox().grow(8)).isEmpty())) {
                    return;
                }
                if (mode.is("Трезубец")) {
                    List<TridentEntity> nearbyTridents = mc.world.getEntitiesWithinAABB(
                            TridentEntity.class,
                            mc.player.getBoundingBox().grow(10)
                    );

                    boolean isAnyTridentMoving = nearbyTridents.stream()
                            .anyMatch(trident ->
                                    trident.getPosX() != trident.prevPosX ||
                                            trident.getPosY() != trident.prevPosY ||
                                            trident.getPosZ() != trident.prevPosZ
                            );

                    if (isAnyTridentMoving) {
                        return;
                    }
                }
                if (matrixBypass.get()) {
                    performMatrixBypassSwap(oldItem);
                    oldItem = -1;
                    ticks = 0;
                } else {
                    mc.playerController.windowClick(0, oldItem, 40, ClickType.SWAP, mc.player);
                    mc.player.connection.sendPacket(new CCloseWindowPacket(mc.player.openContainer.windowId));
                    oldItem = -1;
                    ticks = 0;
                }
            }
        }
    }

    @EventTarget
    private void onPacket(EventPacket event) {
        if (!event.isReceive()) {
            return;
        }

        if (event.getPacket() instanceof SSpawnObjectPacket packet) {
            if (packet.getType() != EntityType.END_CRYSTAL) {
                return;
            }

            if (!mode.is("Кристаллы")) {
                return;
            }
            if (isSwapBlockedByBall()) {
                return;
            }

            if (mc.player.getDistance(new EnderCrystalEntity(mc.world, packet.getX(), packet.getY(), packet.getZ())) > distancecrystall.get() || hasTotemInHand()) {
                return;
            }

            Slot crystalSlot = findTotemSlotAndUpdateCount();
            if (crystalSlot == null) {
                return;
            }

            doSwap(crystalSlot.slotNumber);
        }
    }

    private boolean hasTotemInHand() {
        for (Hand hand : Hand.values()) {
            ItemStack stack = mc.player.getHeldItem(hand);
            if (stack.getItem() == Items.TOTEM_OF_UNDYING && (!saveEnchanted.get() || !stack.isEnchanted() || nonEnchantedTotems <= 0)) {
                return true;
            }
        }
        return false;
    }

    private boolean needTotem() {
        float currentHealth = mc.player.getHealth();
        if (mode.is("Золотые сердца") && mc.player.isPotionActive(Effects.ABSORPTION)) {
            currentHealth += mc.player.getAbsorptionAmount();
        }

        boolean blockByBall = isSwapBlockedByBall();
        if (!blockByBall && (mode.is("Падение") && !mc.player.isInWater() && !mc.player.isElytraFlying() && mc.player.fallDistance > 10)) {
            return true;
        }

        float healthThreshold = mc.player.isElytraFlying() ? elytraHealth.get() : health.get();
        return currentHealth <= healthThreshold;
    }


    private Slot findTotemSlotAndUpdateCount() {
        nonEnchantedTotems = (int) mc.player.openContainer.inventorySlots.stream()
                .filter(s -> s.getStack().getItem() == Items.TOTEM_OF_UNDYING && !s.getStack().isEnchanted())
                .count();
        return mc.player.openContainer.inventorySlots.stream()
                .filter(s -> s.getStack().getItem() == Items.TOTEM_OF_UNDYING && (!saveEnchanted.get() || !s.getStack().isEnchanted() || nonEnchantedTotems <= 0))
                .findFirst()
                .orElse(null);
    }

    private void doSwap(int fromSlot) {
        if (!mc.player.getHeldItemOffhand().isEmpty() && oldItem == -1) oldItem = fromSlot;
        if (matrixBypass.get()) {
            performMatrixBypassSwap(fromSlot);
            ticks = 0;
            return;
        }
        if (swapMode.is("Default")) {
            if (MoveUtil.isMoving()) MoveComponent.stopTicks = 1;
            if (stop.get()) MoveComponent.stop = true;
            if (!MoveUtil.isMoving() || (!stop.get() && !mc.player.isSprinting())) {
                mc.playerController.windowClick(0, fromSlot, 40, ClickType.SWAP, mc.player);
                mc.player.connection.sendPacket(new CCloseWindowPacket(mc.player.openContainer.windowId));
            }
        } else {
            mc.playerController.windowClick(0, fromSlot, 40, ClickType.SWAP, mc.player);
            mc.player.connection.sendPacket(new CCloseWindowPacket(mc.player.openContainer.windowId));
        }
        ticks = 0;
    }

    private boolean isHotbarSlot(int slot) {
        return slot >= 36 && slot <= 44;
    }

    private boolean isSwapBlockedByBall() {
        return noBallSwitch.get()
                && mc.player.getHeldItemOffhand().getItem() == Items.PLAYER_HEAD
                && !(mode.is("Падение") && mc.player.fallDistance > 5);
    }

    private void performMatrixBypassSwap(int slotToSelect) {
        boolean usedPickPacket = !isHotbarSlot(slotToSelect);
        int originalHotbarSlot = mc.player.inventory.currentItem;
        if (usedPickPacket) PickItemFixHandler.activateFix();
        if (usedPickPacket) {
            mc.player.connection.sendPacket(new CPickItemPacket(slotToSelect));
        } else {
            int hotbarIndex = slotToSelect - 36;
            mc.player.connection.sendPacket(new CHeldItemChangePacket(hotbarIndex));
        }
        mc.player.connection.sendPacket(new CPlayerDiggingPacket(CPlayerDiggingPacket.Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ZERO, Direction.DOWN));
        if (usedPickPacket) {
            mc.player.connection.sendPacket(new CPickItemPacket(slotToSelect));
        } else {
            mc.player.connection.sendPacket(new CHeldItemChangePacket(originalHotbarSlot));
        }
    }

    @Override
    public void onDisable() {
        oldItem = -1;
        ticks = 0;
        super.onDisable();
    }
}
