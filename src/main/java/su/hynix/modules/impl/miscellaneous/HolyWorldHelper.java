package su.hynix.modules.impl.miscellaneous;

import com.darkmagician6.eventapi.EventTarget;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.play.client.CAnimateHandPacket;
import net.minecraft.network.play.client.CCloseWindowPacket;
import net.minecraft.network.play.client.CPlayerTryUseItemPacket;
import net.minecraft.util.Hand;
import su.hynix.component.impl.MoveComponent;
import su.hynix.events.EventKey;
import su.hynix.events.EventUpdate;
import su.hynix.modules.Category;
import su.hynix.modules.Module;
import su.hynix.modules.api.constructors.impl.BindSetting;
import su.hynix.modules.api.constructors.impl.BooleanSetting;
import su.hynix.utils.player.InventoryUtil;
import su.hynix.utils.player.MoveUtil;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class HolyWorldHelper extends Module {

    public BindSetting useBlazeRod = new BindSetting("Штучка");
    public BindSetting useNetherStar = new BindSetting("Стан");
    public BindSetting usePrismarineShard = new BindSetting("Взрывная Трапка");
    public BindSetting useChorusFruit = new BindSetting("Трапка (Хорус)");
    public BindSetting useSnowball = new BindSetting("Снежок");
    public BindSetting useShulkerBox = new BindSetting("Рюкзак (Шалкер)");
    public boolean slow = false;
    BooleanSetting stop = new BooleanSetting("Полная остановка", false);
    boolean canUse = false;
    ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    Item swapItem;
    private boolean progress = false;
    private boolean allow;
    private long delay = -1L;
    private int oldSlot = -1;

    public HolyWorldHelper() {
        super("Holyworld Assit", "Ассит под Holyworld", Category.Miscellaneous);
        this.addSettings(useBlazeRod, useNetherStar, usePrismarineShard, useChorusFruit, useSnowball, useShulkerBox, stop);
    }

    private void useItemAndClick(Item item, long delay) {
        if (mc.player == null) return;
        if (InventoryUtil.find(item) < 0) return;
        if (progress) return;

        swapItem = item;
        progress = true;
        allow = true;
        canUse = true;
    }

    @EventTarget
    public void onUse(EventUpdate e) {
        if (swapItem == null) return;
        itemController(swapItem);
    }

    private void itemController(Item item) {
        int slot = InventoryUtil.find(item);
        if (slot >= 0) {

            if (allow) {
                if (MoveUtil.isMoving()) MoveComponent.stopTicks = 1;
                if (stop.get()) MoveComponent.stop = true;
                if (!MoveUtil.isMoving() || (!stop.get() && !mc.player.serverSprintState && !mc.player.isSprinting())) {

                    mc.playerController.windowClick(0, slot, mc.player.inventory.currentItem, ClickType.SWAP, mc.player);
                    mc.playerController.syncCurrentPlayItem();
                    mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.MAIN_HAND));
                    mc.player.connection.sendPacket(new CAnimateHandPacket(Hand.MAIN_HAND));

                    oldSlot = slot;
                    delay = System.currentTimeMillis() + 100L;

                    if (mc.currentScreen == null) {
                        mc.player.connection.sendPacket(new CCloseWindowPacket(mc.player.openContainer.windowId));
                    }


                    allow = false;
                }
            }
        }
        if (delay >= 0L && System.currentTimeMillis() >= delay) {

            if (oldSlot != -1) {
                if (MoveUtil.isMoving()) MoveComponent.stopTicks = 1;
                if (stop.get()) MoveComponent.stop = true;

                if (!MoveUtil.isMoving() || (!stop.get() && !mc.player.serverSprintState && !mc.player.isSprinting())) {
                    mc.playerController.windowClick(0, oldSlot, mc.player.inventory.currentItem, ClickType.SWAP, mc.player);
                    mc.playerController.syncCurrentPlayItem();

                    if (mc.currentScreen == null) {
                        mc.player.connection.sendPacket(new CCloseWindowPacket(mc.player.openContainer.windowId));
                    }

                    oldSlot = -1;
                    delay = -1L;
                    swapItem = null;
                    progress = false;
                    canUse = false;
                }
            } else {
                delay = -1L;
                swapItem = null;
                progress = false;
                canUse = false;
            }
        }


    }

    @EventTarget
    public void onKey(EventKey e) {
        if (mc.currentScreen == null && mc.player != null) {
            if (canUse) return;
            boolean keyWasPressed = e.getKey() == this.useBlazeRod.get() ||
                    e.getKey() == this.useNetherStar.get() ||
                    e.getKey() == this.usePrismarineShard.get() ||
                    e.getKey() == this.useChorusFruit.get() ||
                    e.getKey() == this.useSnowball.get() ||
                    e.getKey() == this.useShulkerBox.get();

            if (keyWasPressed) {
                this.slow = true;
            }

            long sleep = 0;
            if (useBlazeRod.get() == e.getKey()) {
                useItemAndClick(Items.FIRE_CHARGE, sleep);
            }

            if (useNetherStar.get() == e.getKey()) {
                useItemAndClick(Items.NETHER_STAR, sleep);
            }

            if (usePrismarineShard.get() == e.getKey()) {
                useItemAndClick(Items.PRISMARINE_SHARD, sleep);
            }

            if (useChorusFruit.get() == e.getKey()) {
                useItemAndClick(Items.CHORUS_FRUIT, sleep);
            }

            if (useSnowball.get() == e.getKey()) {
                useItemAndClick(Items.SNOWBALL, sleep);
            }

            if (useShulkerBox.get() == e.getKey()) {
                useItemAndClick(Items.SHULKER_BOX, sleep);
            }
        }
    }

    @EventTarget
    public void onUpdate(EventUpdate e) {

        this.slow = false;
    }
}