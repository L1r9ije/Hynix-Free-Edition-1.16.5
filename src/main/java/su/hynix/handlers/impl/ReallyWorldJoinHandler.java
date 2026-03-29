package su.hynix.handlers.impl;

import com.darkmagician6.eventapi.EventTarget;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.Items;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.network.play.client.CPlayerTryUseItemPacket;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.util.Hand;
import su.hynix.events.EventUpdate;
import su.hynix.utils.Wrapper;
import su.hynix.utils.math.TimeUtil;
import su.hynix.utils.player.InventoryUtil;

public class ReallyWorldJoinHandler implements Wrapper {

    private static final TimeUtil timer = new TimeUtil();
    private static Phase phase = Phase.IDLE;
    private static int targetGrief = -1;
    private static Integer manualGrief = null;

    public static void startRejoin() {
        if (mc.player == null) return;
        targetGrief = manualGrief != null ? manualGrief : getGrief();
        if (targetGrief <= 0) return;
        phase = Phase.SENT_HUB;
        mc.player.sendChatMessage("/hub");
        timer.reset();
    }

    public static int getGrief() {
        if (mc.world == null) return -1;
        for (ScoreObjective team : mc.world.getScoreboard().getScoreObjectives()) {
            String board = team.getDisplayName().getString();
            if (board.contains("ГРИФ #")) {
                return Integer.parseInt(board.split("#")[1].replace(" ", ""));
            }
        }
        return -1;
    }

    private static void reset() {
        phase = Phase.IDLE;
        targetGrief = -1;
        timer.reset();
    }

    public static void setManualGrief(int grief) {
        manualGrief = grief;
    }

    public static void clearManualGrief() {
        manualGrief = null;
    }

    @EventTarget
    public void onUpdate(EventUpdate e) {
        if (phase == Phase.IDLE) return;

        if (phase == Phase.SENT_HUB && timer.hasTimeElapsed(500)) {
            useCompass();
            phase = Phase.USED_COMPASS;
            timer.reset();
            return;
        }

        if (phase == Phase.USED_COMPASS && !(mc.currentScreen instanceof ContainerScreen)) {
            if (timer.hasTimeElapsed(500)) {
                useCompass();
                timer.reset();
            }
            return;
        }

        if (!(mc.currentScreen instanceof ContainerScreen container)) return;

        for (int i = 0; i < container.getContainer().inventorySlots.size(); i++) {
            Slot slot = container.getContainer().inventorySlots.get(i);
            if (slot.getStack().isEmpty()) continue;

            if (phase == Phase.USED_COMPASS && slot.getStack().getDisplayName().getString().contains("ГРИФЕРСКОЕ ВЫЖИВАНИЕ (1.16.5-1.20.4)")) {
                clickSlot(i);
                phase = Phase.CLICKED_GRIEF_SURVIVAL;
                break;
            }

            if (phase == Phase.CLICKED_GRIEF_SURVIVAL && slot.getStack().getDisplayName().getString().contains("ГРИФ #" + targetGrief + " (1.16.5+)") && timer.hasTimeElapsed(50)) {
                clickSlot(i);
                reset();
                break;
            }
        }
    }

    private void useCompass() {
        int slot = InventoryUtil.getSlot(Items.COMPASS);
        if (slot == -1 || mc.player == null) return;

        mc.player.inventory.currentItem = slot;
        mc.player.connection.sendPacket(new CHeldItemChangePacket(slot));
        mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.MAIN_HAND));
    }

    private void clickSlot(int slotIndex) {
        mc.playerController.windowClick(mc.player.openContainer.windowId, slotIndex, 0, ClickType.PICKUP, mc.player);
        timer.reset();
    }

    private enum Phase {
        IDLE, SENT_HUB, USED_COMPASS, CLICKED_GRIEF_SURVIVAL
    }
}