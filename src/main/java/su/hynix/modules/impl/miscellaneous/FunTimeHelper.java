package su.hynix.modules.impl.miscellaneous;

import com.darkmagician6.eventapi.EventTarget;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.play.client.CAnimateHandPacket;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.network.play.client.CPlayerTryUseItemPacket;
import net.minecraft.util.Hand;
import su.hynix.component.impl.MoveComponent;
import su.hynix.events.EventKey;
import su.hynix.events.EventUpdate;
import su.hynix.modules.Category;
import su.hynix.modules.Module;
import su.hynix.modules.api.constructors.impl.BindSetting;
import su.hynix.utils.player.InventoryUtil;
import su.hynix.utils.player.MoveUtil;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class FunTimeHelper extends Module {
    public BindSetting useDezor = new BindSetting("Дезориентация");
    public BindSetting useTrap = new BindSetting("Трапка");
    public BindSetting usePil = new BindSetting("Явная пыль");
    public BindSetting useSmerch = new BindSetting("Огненный смерч");
    public BindSetting usePlast = new BindSetting("Пласт");
    public BindSetting useAura = new BindSetting("Божья аура");
    public BindSetting useSnow = new BindSetting("Снежок");
    public boolean slow = false;
    boolean canUse = false;
    ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    Item swapItem;

    private boolean progress = false;
    private int oldSlot = -1;

    // Переменные для стейт-машины (тайминги)
    private int state = 0;
    private int tickTimer = 0;
    private boolean wasInHotbar = false;

    public FunTimeHelper() {
        super("FuntimeHelper", "Помощник для сервера Funtime", Category.Miscellaneous);
        addSettings(useDezor, useTrap, usePil, useSmerch, usePlast, useAura, useSnow);
    }

    private void useItemAndClick(Item item, long delay) {
        if (mc.player == null) return;
        if (InventoryUtil.find(item) < 0) return;
        if (progress) return;

        swapItem = item;
        progress = true;
        canUse = true;
        state = 0;
        tickTimer = 0;
        // Запоминаем, был ли предмет уже в хотбаре до начала свапа
        wasInHotbar = InventoryUtil.haveHotBar(item);
    }

    @EventTarget
    public void onUse(EventUpdate e) {
        if (swapItem == null || !progress) return;
        itemController(swapItem);
    }

    private void itemController(Item item) {
        if (wasInHotbar) {
            // Если предмет изначально лежал в хотбаре, то задержки не нужны
            int slot = InventoryUtil.find(item);
            if (slot >= 36) {
                int hotbarSlot = slot - 36;
                mc.player.connection.sendPacket(new CHeldItemChangePacket(hotbarSlot));
                mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.MAIN_HAND));
                mc.player.connection.sendPacket(new CAnimateHandPacket(Hand.MAIN_HAND));
                mc.player.connection.sendPacket(new CHeldItemChangePacket(mc.player.inventory.currentItem));
            }
            resetState();
        } else {
            // Если предмет в инвентаре, работаем через тики (обход античита)
            if (MoveUtil.isMoving()) {
                MoveComponent.stopTicks = 1;
                MoveComponent.stop = true;
                return; // Ждем пока игрок остановится
            }

            int currentSlot = mc.player.inventory.currentItem; // Активный слот (0-8)

            if (state == 0) {
                int slot = InventoryUtil.find(item);
                if (slot < 0) {
                    resetState();
                    return;
                }
                oldSlot = slot;

                // 1. Перемещаем предмет в текущий слот в руке
                mc.playerController.windowClick(0, oldSlot, currentSlot, ClickType.SWAP, mc.player);

                state = 1;
                tickTimer = 2; // Даем 2 тика (~100мс), чтобы сервер обработал перемещение

            } else if (state == 1) {
                if (tickTimer > 0) {
                    tickTimer--;
                    return;
                }
                // 2. Юзаем предмет
                mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.MAIN_HAND));
                mc.player.connection.sendPacket(new CAnimateHandPacket(Hand.MAIN_HAND));

                state = 2;
                tickTimer = 3; // Даем 3 тика (~150мс), чтобы сервер засчитал нажатие перед тем как убрать

            } else if (state == 2) {
                if (tickTimer > 0) {
                    tickTimer--;
                    return;
                }
                // 3. Возвращаем предмет обратно в инвентарь
                mc.playerController.windowClick(0, oldSlot, currentSlot, ClickType.SWAP, mc.player);
                resetState();
            }
        }
    }

    private void resetState() {
        swapItem = null;
        progress = false;
        canUse = false;
        state = 0;
        oldSlot = -1;
        wasInHotbar = false;
    }

    @EventTarget
    public void onKey(EventKey e) {
        if (mc.currentScreen == null && mc.player != null) {
            if (canUse) return;
            boolean keyWasPressed = useDezor.get() == e.getKey() ||
                    useTrap.get() == e.getKey() ||
                    usePil.get() == e.getKey() ||
                    useSmerch.get() == e.getKey() ||
                    usePlast.get() == e.getKey() ||
                    useAura.get() == e.getKey() ||
                    useSnow.get() == e.getKey();

            if (keyWasPressed) {
                this.slow = true;
            }

            long sleep = 0;
            if (useDezor.get() == e.getKey()) useItemAndClick(Items.ENDER_EYE, sleep);
            if (useTrap.get() == e.getKey()) useItemAndClick(Items.NETHERITE_SCRAP, sleep);
            if (usePil.get() == e.getKey()) useItemAndClick(Items.SUGAR, sleep);
            if (useSmerch.get() == e.getKey()) useItemAndClick(Items.FIRE_CHARGE, sleep);
            if (usePlast.get() == e.getKey()) useItemAndClick(Items.DRIED_KELP, sleep);
            if (useAura.get() == e.getKey()) useItemAndClick(Items.PHANTOM_MEMBRANE, sleep);
            if (useSnow.get() == e.getKey()) useItemAndClick(Items.SNOWBALL, sleep);
        }
    }

    @EventTarget
    public void onUpdate(EventUpdate e) {
        this.slow = false;
    }
}