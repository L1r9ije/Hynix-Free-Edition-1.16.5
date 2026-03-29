package su.hynix.modules.impl.player;

import com.darkmagician6.eventapi.EventTarget;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.Items;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.network.play.client.CPlayerTryUseItemPacket;
import net.minecraft.network.play.server.SJoinGamePacket;
import net.minecraft.util.Hand;
import su.hynix.events.EventPacket;
import su.hynix.events.EventUpdate;
import su.hynix.modules.Category;
import su.hynix.modules.Module;
import su.hynix.modules.api.constructors.impl.ModeSetting;
import su.hynix.modules.api.constructors.impl.SliderSetting;
import su.hynix.utils.math.TimeUtil;
import su.hynix.utils.player.InventoryUtil;


public class AutoJoiner extends Module {

    private final ModeSetting serverMode = new ModeSetting("Режим", "ReallyWorld", "ReallyWorld");
    private final SliderSetting grief = new SliderSetting("Гриферский мир", 1, 1, 54, 1);
    private final SliderSetting speed = new SliderSetting("Скорость", 3, 1, 20, 1);
    private final TimeUtil timerUtil = new TimeUtil();

    public AutoJoiner() {
        super("AutoJoiner", "Автоматически заходит на на указанный сервер", Category.Player);
        addSettings(serverMode, grief, speed);
    }

    @EventTarget
    public void onUpdate(EventUpdate event) {
        if (mc.currentScreen instanceof ContainerScreen container) {
            boolean hasClicked = false;

            for (int i = 0; i < container.getContainer().inventorySlots.size(); i++) {
                Slot slot = container.getContainer().inventorySlots.get(i);
                if (slot.getStack().isEmpty()) continue;

                String s = slot.getStack().getDisplayName().getString();

                if (s.contains("ГРИФЕРСКОЕ ВЫЖИВАНИЕ (1.16.5-1.20.4)") && !hasClicked) {
                    mc.playerController.windowClick(mc.player.openContainer.windowId, i, 0, ClickType.PICKUP, mc.player);
                }

                if (s.contains("ГРИФ #" + grief.get().intValue() + " (1.16.5+)")) {
                    if (timerUtil.hasTimeElapsed(speed.get().intValue() * 100L)) {
                        mc.playerController.windowClick(mc.player.openContainer.windowId, i, 0, ClickType.PICKUP, mc.player);
                        timerUtil.reset();
                        hasClicked = true;
                    }
                }
            }
        }
    }

    @EventTarget
    public void onUpdate(EventPacket event) {
        if (event.getPacket() instanceof SJoinGamePacket) {
            toggle();
        }
    }

    @Override
    public void onEnable() {
        super.onEnable();
        if (serverMode.is("ReallyWorld")) {
            selectJoinerItem();
        }
    }

    @Override
    public void onDisable() {
        timerUtil.reset();
        super.onDisable();
    }

    private void selectJoinerItem() {
        int slot = InventoryUtil.getSlot(Items.COMPASS);
        if (slot == -1) return;
        mc.player.inventory.currentItem = slot;
        mc.player.connection.sendPacket(new CHeldItemChangePacket(slot));
        mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.MAIN_HAND));
    }
}