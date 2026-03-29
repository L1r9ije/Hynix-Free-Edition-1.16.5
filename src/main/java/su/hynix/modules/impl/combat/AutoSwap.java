package su.hynix.modules.impl.combat;

import com.darkmagician6.eventapi.EventTarget;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.item.AirItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.play.client.CCloseWindowPacket;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.network.play.client.CPickItemPacket;
import net.minecraft.network.play.client.CPlayerDiggingPacket;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import su.hynix.component.impl.MoveComponent;
import su.hynix.events.EventKey;
import su.hynix.events.EventUpdate;
import su.hynix.handlers.impl.PickItemFixHandler;
import su.hynix.modules.Category;
import su.hynix.modules.Module;
import su.hynix.modules.api.constructors.impl.BindSetting;
import su.hynix.modules.api.constructors.impl.BooleanSetting;
import su.hynix.modules.api.constructors.impl.ModeSetting;
import su.hynix.utils.player.InventoryUtil;
import su.hynix.utils.player.MoveUtil;

public class AutoSwap extends Module {
    private final BindSetting key = new BindSetting("Кнопка свапа");
    private final ModeSetting firstItem = new ModeSetting("Первый предмет", "Тотем", "Щит", "Тотем", "Фейерверк", "Яблоко", "Любая еда", "Шар, Cфера");
    private final ModeSetting secondItem = new ModeSetting("Второй предмет", "Шар, Cфера", "Щит", "Тотем", "Фейерверк", "Яблоко", "Любая еда", "Шар, Cфера");
    private final BooleanSetting ignoreVanillaTotems = new BooleanSetting("Игнорировать обычные тотемы", true);
    private final BooleanSetting non = new BooleanSetting("Обход Matrix", false);
    private final BooleanSetting stop = new BooleanSetting("Полная остановка", false, () -> !non.get());

    private boolean swapRequested = false;

    public AutoSwap() {
        super("AutoSwap", "Автоматически перемещает предметы между главной и второй руками", Category.Combat);
        addSettings(key, firstItem, secondItem, ignoreVanillaTotems, non, stop);
    }

    private Item getItemFromMode(String mode) {
        return switch (mode) {
            case "Тотем" -> Items.TOTEM_OF_UNDYING;
            case "Щит" -> Items.SHIELD;
            case "Фейерверк" -> Items.FIREWORK_ROCKET;
            case "Яблоко" -> Items.GOLDEN_APPLE;
            case "Шар, Cфера" -> Items.PLAYER_HEAD;
            case "Любая еда" -> null;
            default -> Items.AIR;
        };
    }

    private int findItemSlot(Item item) {
        if (item == Items.TOTEM_OF_UNDYING && ignoreVanillaTotems.get()) {
            return InventoryUtil.findEnchantedTotemSlot();
        } else if (item == null) {
            return InventoryUtil.getBestFoodSlot();
        }
        return InventoryUtil.getSlot(item);
    }

    private int getTargetSwapSlot() {
        ItemStack offhandItemStack = mc.player.getHeldItemOffhand();
        Item currentOffhandItem = offhandItemStack.getItem();


        Item first = getItemFromMode(firstItem.get());
        Item second = getItemFromMode(secondItem.get());

        if (currentOffhandItem instanceof AirItem) {
            int firstSlot = findItemSlot(first);
            if (firstSlot >= 0) return firstSlot;
            return findItemSlot(second);
        }


        if ((first != null && currentOffhandItem == first) || (first == null && currentOffhandItem.isFood()))
            return findItemSlot(second);
        else if ((second != null && currentOffhandItem == second) || (second == null && currentOffhandItem.isFood()))
            return findItemSlot(first);

        int firstSlot = findItemSlot(first);
        return firstSlot >= 0 ? firstSlot : findItemSlot(second);
    }

    @EventTarget
    public void onEvent(EventKey event) {
        if (key.get() == event.getKey() && event.isHold() && !swapRequested) {
            swapRequested = true;
        }
    }

    @EventTarget
    public void onUpdate(EventUpdate e) {
        if (swapRequested) performSwap();
    }

    private void performSwap() {
        int targetSlot = getTargetSwapSlot();

        if (targetSlot < 0) {
            swapRequested = false;
            return;
        }

        int invFrom = targetSlot < 9 ? targetSlot + 36 : targetSlot;
        if (non.get()) {
            performMatrixBypassSwap(invFrom);
            swapRequested = false;
        } else {
            if (MoveUtil.isMoving()) MoveComponent.stopTicks = 1;
            if (stop.get()) MoveComponent.stop = true;
            if (!MoveUtil.isMoving() || (!stop.get() && !mc.player.isSprinting())) {
                mc.playerController.windowClick(0, invFrom, 40, ClickType.SWAP, mc.player);
                if (mc.currentScreen == null) {
                    mc.player.connection.sendPacket(new CCloseWindowPacket(mc.player.openContainer.windowId));
                }
                swapRequested = false;
            }
        }
    }

    private boolean isHotbarSlot(int slot) {
        return slot >= 36 && slot <= 44;
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
        swapRequested = false;
        super.onDisable();
    }
}