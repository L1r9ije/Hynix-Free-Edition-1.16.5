package su.hynix.modules.impl.player;

import com.darkmagician6.eventapi.EventTarget;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.item.Items;
import net.minecraft.network.play.client.CAnimateHandPacket;
import net.minecraft.network.play.client.CCloseWindowPacket;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.network.play.client.CPlayerTryUseItemPacket;
import net.minecraft.util.Hand;
import su.hynix.component.impl.MoveComponent;
import su.hynix.events.EventKey;
import su.hynix.events.EventUpdate;
import su.hynix.modules.Category;
import su.hynix.modules.Module;
import su.hynix.modules.api.constructors.impl.BindSetting;
import su.hynix.modules.api.constructors.impl.BooleanSetting;
import su.hynix.modules.api.constructors.impl.ModeSetting;
import su.hynix.utils.player.InventoryUtil;
import su.hynix.utils.player.MoveUtil;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class ClickPearl extends Module {

    final BindSetting throwKey = new BindSetting("Кнопка броска");
    private final BooleanSetting stop = new BooleanSetting("Полная остановка", false);
    private final ModeSetting mode = new ModeSetting(
            "Mode",
            "Обычный",
            ("Обычный"),
            ("Пакетный"),
            ("ReallyWorld")
    );
    public boolean slow = false;
    boolean allow = false;
    int ticks = 0;
    ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private boolean pearl = false;
    private long delay = -1L;
    private int oldSlot = -1;

    public ClickPearl() {
        super("ClickPearl", "Позволяет мгновенно бросать эндер-жемчуг, нажатием на определенную кнопку", Category.Player);

        this.addSettings(mode, throwKey, stop);
    }

    @EventTarget
    private void onKey(EventKey e) {
        if (e.getKey() == throwKey.get() && !mc.player.getCooldownTracker().hasCooldown(Items.ENDER_PEARL) && !allow && InventoryUtil.find(Items.ENDER_PEARL) > 0) {
            pearl = true;
            allow = true;
        }
    }

    @EventTarget
    public void update(EventUpdate event) {
        pearlController();
    }


    private void pearlController() {
        int slot = InventoryUtil.find(Items.ENDER_PEARL);
        if (mode.is("Пакетный")) {
            if (slot >= 0) {
                if (pearl && InventoryUtil.haveHotBar(Items.ENDER_PEARL)) {
                    if (slot <= 44 && slot >= 36) {
                        mc.player.connection.sendPacket(new CHeldItemChangePacket(slot - 36));

                        mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.MAIN_HAND));

                        mc.player.connection.sendPacket(new CHeldItemChangePacket(mc.player.inventory.currentItem));


                        pearl = false;
                        allow = false;
                    }

                } else if (pearl) {
                    for (int i = 0; i < 36; ++i) {
                        if (mc.player.inventory.getStackInSlot(i).getItem() == Items.ENDER_PEARL) {

                            mc.playerController.windowClick(0, i, mc.player.inventory.currentItem % 8 + 1, ClickType.SWAP, mc.player);

                            mc.player.connection.sendPacket(new CHeldItemChangePacket(mc.player.inventory.currentItem % 8 + 1));
                            mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.MAIN_HAND));
                            mc.player.connection.sendPacket(new CHeldItemChangePacket(mc.player.inventory.currentItem));

                            mc.playerController.windowClick(0, i, mc.player.inventory.currentItem % 8 + 1, ClickType.SWAP, mc.player);


                            pearl = false;
                            allow = false;
                            break;
                        }
                    }
                }

            }
        }

        if (mode.is("Обычный")) {
            if (slot >= 0) {
                if (pearl) {
                    MoveComponent.stopTicks = 1;
                    if (stop.get()) MoveComponent.stop = true;
                    if (!MoveUtil.isMoving() || (!stop.get() && !mc.player.serverSprintState && !mc.player.isSprinting())) {

                        mc.playerController.windowClick(0, slot, mc.player.inventory.currentItem, ClickType.SWAP, mc.player);
                        mc.playerController.syncCurrentPlayItem();
                        mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.MAIN_HAND));
                        mc.player.connection.sendPacket(new CAnimateHandPacket(Hand.MAIN_HAND));

                        oldSlot = slot;
                        delay = System.currentTimeMillis() + 150L;

                        if (mc.currentScreen == null) {
                            mc.player.connection.sendPacket(new CCloseWindowPacket(mc.player.openContainer.windowId));
                        }


                        pearl = false;
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
                        allow = false;
                    }
                } else {
                    delay = -1;
                    allow = false;
                }
            }

        }

        if (mode.is("ReallyWorld")) {
            if (slot >= 0) {
                if (pearl && InventoryUtil.haveHotBar(Items.ENDER_PEARL)) {

                    mc.player.connection.sendPacket(new CHeldItemChangePacket(slot - 36));
                    mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.MAIN_HAND));
                    mc.player.connection.sendPacket(new CAnimateHandPacket(Hand.MAIN_HAND));

                    delay = System.currentTimeMillis() + 150;


                    pearl = false;
                } else if (pearl) {
                    MoveComponent.stopTicks = 1;
                    if (stop.get()) MoveComponent.stop = true;
                    if (!MoveUtil.isMoving() || (!stop.get() && !mc.player.serverSprintState && !mc.player.isSprinting())) {

                        mc.playerController.windowClick(0, slot, mc.player.inventory.currentItem % 8 + 1, ClickType.SWAP, mc.player);

                        mc.player.connection.sendPacket(new CHeldItemChangePacket(mc.player.inventory.currentItem % 8 + 1));
                        mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.MAIN_HAND));
                        mc.player.connection.sendPacket(new CAnimateHandPacket(Hand.MAIN_HAND));

                        oldSlot = slot;
                        delay = System.currentTimeMillis() + 150;

                        if (mc.currentScreen == null) {
                            mc.player.connection.sendPacket(new CCloseWindowPacket());

                        }


                        pearl = false;
                    }
                }
            }
            if (delay >= 0L && System.currentTimeMillis() >= delay) {

                if (oldSlot != -1) {
                    MoveComponent.stopTicks = 1;
                    if (stop.get()) MoveComponent.stop = true;

                    if (!MoveUtil.isMoving() || (!stop.get() && !mc.player.serverSprintState && !mc.player.isSprinting())) {
                        mc.player.connection.sendPacket(new CHeldItemChangePacket(mc.player.inventory.currentItem));
                        mc.playerController.windowClick(0, oldSlot, mc.player.inventory.currentItem % 8 + 1, ClickType.SWAP, mc.player);

                        if (mc.currentScreen == null) {
                            mc.player.connection.sendPacket(new CCloseWindowPacket());

                        }

                        oldSlot = -1;
                        delay = -1L;
                        allow = false;
                    }
                } else {
                    mc.player.connection.sendPacket(new CHeldItemChangePacket(mc.player.inventory.currentItem));
                    delay = -1;
                    allow = false;
                }
            }
        }


    }

}
