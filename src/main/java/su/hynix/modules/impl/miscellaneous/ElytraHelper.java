package su.hynix.modules.impl.miscellaneous;

import com.darkmagician6.eventapi.EventTarget;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ElytraItem;
import net.minecraft.item.Items;
import net.minecraft.network.play.client.CCloseWindowPacket;
import net.minecraft.network.play.client.CEntityActionPacket;
import su.hynix.events.EventInput;
import su.hynix.events.EventKey;
import su.hynix.events.EventUpdate;
import su.hynix.modules.Category;
import su.hynix.modules.Module;
import su.hynix.modules.api.constructors.impl.BindSetting;
import su.hynix.modules.api.constructors.impl.BooleanSetting;
import su.hynix.utils.player.InventoryUtil;
import su.hynix.utils.player.MoveUtil;


public class ElytraHelper extends Module {
    private final BindSetting keySwap = new BindSetting("Кнопка свапа");
    private final BooleanSetting equipchestplate = new BooleanSetting("Снимать элитры при приземлении", false, () -> keySwap.get() != -1);
    private final BindSetting fireworkKey = new BindSetting("Кнопка фейера");
    private final BooleanSetting autoFly = new BooleanSetting("Автоматически взлетать", false);
    private final BooleanSetting usefirework = new BooleanSetting("Автоматически использовать феерверк", false, autoFly::get);
    private final BooleanSetting matrixBypass = new BooleanSetting("Обход Matrix", true);

    private boolean swapRequested = false;
    private boolean fireworkUsed = false;
    private boolean wasOnGround = false;
    private boolean wasInElytraFlight = false;
    private int tickswap;

    public ElytraHelper() {
        super("ElytraSwap", "Позволяет быстро одеть или снять элитру, упрощая полёт и манипуляции с ней", Category.Miscellaneous);
        addSettings(keySwap, equipchestplate, fireworkKey, autoFly, usefirework, matrixBypass);
    }

    @EventTarget
    public void onTick(EventUpdate event) {
        if (tickswap > 0) tickswap--;
        if (swapRequested && (!matrixBypass.get() || !MoveUtil.isMoving())) {
            executeSwap();
            swapRequested = false;
            mc.player.connection.sendPacket(new CCloseWindowPacket(mc.player.openContainer.windowId));
        }
        if (mc.player.isElytraFlying()) wasInElytraFlight = true;
        if (equipchestplate.get()) {
            if (mc.player.isOnGround() && !wasOnGround && wasInElytraFlight) {
                if (mc.player.getItemStackFromSlot(EquipmentSlotType.CHEST).getItem() == Items.ELYTRA) {
                    if (InventoryUtil.getChestPlateSlot() != -1) {
                        InventoryUtil.moveToArmor(InventoryUtil.getChestPlateSlot(), 6);
                    } else {
                        InventoryUtil.pickupItem(6, 0);
                        if (InventoryUtil.findEmptyInventorySlot() != -1) {
                            InventoryUtil.pickupItem(InventoryUtil.findEmptyInventorySlot(), 0);
                        }
                    }
                    wasInElytraFlight = false;
                }
            }
            wasOnGround = mc.player.isOnGround();
        }

        if (autoFly.get() && ElytraItem.isUsable(mc.player.getItemStackFromSlot(EquipmentSlotType.CHEST)) && mc.player.getItemStackFromSlot(EquipmentSlotType.CHEST).getItem() == Items.ELYTRA && !mc.player.isElytraFlying() && !mc.player.isOnGround()) {
            mc.player.startFallFlying();
            mc.player.connection.sendPacket(new CEntityActionPacket(mc.player, CEntityActionPacket.Action.START_FALL_FLYING));
            wasInElytraFlight = true;
            if (usefirework.get() && !fireworkUsed) {
                InventoryUtil.inventorySwapClick(Items.FIREWORK_ROCKET);
                fireworkUsed = true;
            }
        } else if (mc.player.isOnGround() && autoFly.get() && mc.player.getItemStackFromSlot(EquipmentSlotType.CHEST).getItem() == Items.ELYTRA) {
            mc.player.jump();
            fireworkUsed = false;
        }
    }

    @EventTarget
    public void onEvent(EventKey event) {
        if (!event.isHold()) return;
        if (event.getKey() == keySwap.get()) {
            boolean hasElytra = InventoryUtil.getSlot(Items.ELYTRA) != -1;
            boolean hasChestplate = InventoryUtil.getChestPlateSlot() != -1;
            if (!hasElytra && !hasChestplate) return;
            tickswap = 2;
            swapRequested = true;
        }
        if (event.getKey() == fireworkKey.get()) {
            InventoryUtil.inventorySwapClick(Items.FIREWORK_ROCKET);
        }
    }

    @EventTarget
    private void onEvent(EventInput eventInput) {
        if (matrixBypass.get() && tickswap > 0) {
            eventInput.setForward(0);
            eventInput.setStrafe(0);
            eventInput.setSneak(false);
            eventInput.setJump(false);
        }
    }


    private void executeSwap() {
        int targetSlot;
        if (mc.player.getItemStackFromSlot(EquipmentSlotType.CHEST).getItem() == Items.ELYTRA) {
            targetSlot = InventoryUtil.getChestPlateSlot();
        } else {
            targetSlot = InventoryUtil.getSlot(Items.ELYTRA);
        }

        if (targetSlot == -1) return;
        InventoryUtil.moveToArmor(targetSlot, 6);
    }
}