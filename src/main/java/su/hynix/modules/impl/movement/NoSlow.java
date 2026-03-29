package su.hynix.modules.impl.movement;

import com.darkmagician6.eventapi.EventTarget;
import net.minecraft.item.Items;
import net.minecraft.item.UseAction;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.network.play.client.CPlayerTryUseItemPacket;
import net.minecraft.util.Hand;
import su.hynix.events.EventNoSlow;
import su.hynix.events.EventUpdate;
import su.hynix.modules.Category;
import su.hynix.modules.Module;
import su.hynix.modules.api.constructors.impl.ModeSetting;
import su.hynix.utils.math.TimeUtil;
import su.hynix.utils.player.InventoryUtil;


public class NoSlow extends Module {
    public static int ticks = 0;
    public static int ticki = 0;
    private final ModeSetting mode = new ModeSetting("Режим", "Vanilla", "Vanilla", "Grim Old", "ReallyworldNew", "Reallyworld", "Test");
    private final TimeUtil timerutils = new TimeUtil();
    public boolean isCan2 = true;
    TimeUtil stopWatch = new TimeUtil();

    public NoSlow() {
        super("No Slow", "Убирает замедление при использовании предмета", Category.Movement);
        addSettings(mode);
    }

    @EventTarget
    public void onNoSlow(EventNoSlow event) {
        if (mc.player == null || mc.world == null) return;
        if (mc.player.isElytraFlying()) return;

        if (mc.player.isHandActive()) {
            switch (mode.get()) {
                case "Vanilla" -> event.setCancelled(true);
                case "Grim Old" -> grimOld(event);
                case "ReallyworldNew" -> test(event);
                case "Test" -> spokky(event);
            }
        }
    }

    private void spokky(EventNoSlow event) {
        if (stopWatch.hasTimeElapsed(125)) {
            event.setCancelled(true);
            mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.MAIN_HAND));
            stopWatch.reset();
        }

    }

    @EventTarget
    public void onEvent(EventUpdate event) {
        if (mode.is("ReallyworldNew") && mc.player != null && !mc.player.isElytraFlying()) {
            if (mc.player.isHandActive()) {
                ticks++;
            } else {
                ticks = 0;
            }
        }
        if (mc.player != null && mc.player.hurtTime == 10) {
            timerutils.reset();
        }
    }

    private void grimOld(EventNoSlow eventNoSlow) {
        boolean isBlockingOrEatingOffhand =
                (mc.player.getHeldItemOffhand().getUseAction() == UseAction.BLOCK
                        || mc.player.getHeldItemOffhand().getUseAction() == UseAction.EAT
                        || mc.player.getHeldItemOffhand().getUseAction() == UseAction.DRINK)
                        && mc.player.getActiveHand() == Hand.MAIN_HAND;

        if (isBlockingOrEatingOffhand) return;

        if (mc.player.getActiveHand() == Hand.MAIN_HAND) {
            mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.OFF_HAND));
            eventNoSlow.setCancelled(true);
            return;
        }

        eventNoSlow.setCancelled(true);
        sendItemChangePacket();
    }

    private void sendItemChangePacket() {

        boolean isMoving = mc.player.moveForward != 0.0F || mc.player.moveStrafing != 0.0F;
        if (!isMoving) return;

        int nextSlot = (mc.player.inventory.currentItem % 8) + 1;
        mc.player.connection.sendPacket(new CHeldItemChangePacket(nextSlot));
        mc.player.connection.sendPacket(new CHeldItemChangePacket(mc.player.inventory.currentItem));
    }

    private void test(EventNoSlow e) {
        int bowSlot = InventoryUtil.find(Items.BOW);
        if (mc.player.getActiveHand() == Hand.OFF_HAND) {
            if (bowSlot != -1 && InventoryUtil.isOnHotBar(bowSlot)) {

                if (bowSlot % 9 != mc.player.inventory.currentItem && ticks == 1) {
                    mc.player.connection.sendPacket(new CHeldItemChangePacket(bowSlot % 9));
                    isCan2 = true;
                } else if (ticks > 1 && ticks < 5) {
                    mc.playerController.processRightClick(mc.player, mc.world, Hand.MAIN_HAND);
                } else if (bowSlot % 9 != mc.player.inventory.currentItem && ticks == 6) {
                    mc.player.connection.sendPacket(new CHeldItemChangePacket(mc.player.inventory.currentItem));
                }


                if (mc.player.isSwimming() || timerutils.getTime() < 2000) {
                    mc.player.setSprinting(true);
                    e.setCancelled(true);
                } else {
                    if (ticks > 1) {
                        mc.player.setSprinting(true);
                        e.setCancelled(true);
                    }
                }
            }
        }
    }
}