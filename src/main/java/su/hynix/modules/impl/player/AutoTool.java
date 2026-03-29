package su.hynix.modules.impl.player;

import com.darkmagician6.eventapi.EventTarget;
import su.hynix.events.EventBlockDamage;
import su.hynix.modules.Category;
import su.hynix.modules.Module;
import su.hynix.utils.player.InventoryUtil;

public class AutoTool extends Module {
    private int lastSlot = -1;

    public AutoTool() {
        super("AutoTool", "Автоматически выбирает лучший инструмент в инвентаре для добычи блоков и атаки противника", Category.Player);
    }

    @EventTarget
    public void onBlockDamage(EventBlockDamage e) {
        if (e.getState() == EventBlockDamage.State.START) {
            this.lastSlot = mc.player.inventory.currentItem;
            int bestToolSlot = InventoryUtil.findBestToolForBlock(e.getBlockState());

            if (bestToolSlot != -1) {
                mc.player.inventory.currentItem = bestToolSlot;
            }
        } else if (lastSlot != -1) {
            mc.player.inventory.currentItem = lastSlot;
            lastSlot = -1;
        }
    }
}
