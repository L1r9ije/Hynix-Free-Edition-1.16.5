package su.hynix.modules.impl.combat;

import com.darkmagician6.eventapi.EventTarget;
import net.minecraft.client.gui.screen.inventory.ChestScreen;
import net.minecraft.client.gui.screen.inventory.CreativeScreen;
import net.minecraft.client.gui.screen.inventory.ShulkerBoxScreen;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.network.play.server.SHeldItemChangePacket;
import net.minecraft.util.math.MathHelper;
import su.hynix.events.EventHandleMouseClick;
import su.hynix.events.EventPacket;
import su.hynix.events.EventUpdate;
import su.hynix.handlers.impl.PickItemFixHandler;
import su.hynix.modules.Category;
import su.hynix.modules.Module;
import su.hynix.modules.api.constructors.impl.BooleanSetting;
import su.hynix.modules.api.constructors.impl.MultiBooleanSetting;
import su.hynix.utils.player.InventoryUtil;

public class NoSlotChange extends Module {
    private final MultiBooleanSetting modes = new MultiBooleanSetting("На что работать",
            new BooleanSetting("Не свапать слоты", true),
            new BooleanSetting("Не выкидать элитру", false),
            new BooleanSetting("Не выкидать нагрудник", false),
            new BooleanSetting("Не выкидать шар", false),
            new BooleanSetting("Не выкидать осколки", false));

    public NoSlotChange() {
        super("ItemSwapFix", "Исправляет ошибки, связанные со сменой предметов в инвентаре", Category.Miscellaneous);
        addSettings(modes);
    }

    @EventTarget
    public void onEvent(EventHandleMouseClick event) {
        Slot clicked = event.getSlotIn();
        ClickType clickType = event.getType();
        if (clicked == null || clickType == ClickType.SWAP || clickType == ClickType.QUICK_MOVE || mc.currentScreen instanceof ChestScreen || mc.currentScreen instanceof CreativeScreen || mc.currentScreen instanceof ShulkerBoxScreen) {
            return;
        }
        Item item = clicked.getStack().getItem();
        if (modes.getIndex(1).get() && item == Items.ELYTRA) {
            event.setCancelled(true);
        }
        if (modes.getIndex(2).get() && item instanceof ArmorItem armorItem && armorItem.getEquipmentSlot() == EquipmentSlotType.CHEST) {
            event.setCancelled(true);
        }
        if (modes.getIndex(3).get() && item == Items.PLAYER_HEAD) {
            event.setCancelled(true);
        }
        if (modes.is("Не выкидать осколки") && hasBukkitDisplayAndCustomModel(clicked.getStack())) {
            event.setCancelled(true);
        }
    }

    @EventTarget
    public void onEvent(EventUpdate event) {
        if (mc.currentScreen instanceof ChestScreen || mc.currentScreen instanceof CreativeScreen || mc.currentScreen instanceof ShulkerBoxScreen) {
            return;
        }
        ItemStack itemStack = mc.player.inventory.getItemStack();
        int n = InventoryUtil.findEmptyInventorySlot();
        boolean bl = n != -1;
        if (modes.is("Не выкидать элитру") && InventoryUtil.getSlot(Items.ELYTRA) == -1 && bl && itemStack.getItem() == Items.ELYTRA) {
            mc.playerController.windowClick(0, n, 1, ClickType.PICKUP, mc.player);
        }
        if (modes.is("Не выкидать нагрудник") && InventoryUtil.getChestPlateSlot() == -1 && bl && itemStack.getItem() instanceof ArmorItem armorItem && armorItem.getEquipmentSlot() == EquipmentSlotType.CHEST) {
            mc.playerController.windowClick(0, n, 1, ClickType.PICKUP, mc.player);
        }
        if (modes.is("Не выкидать шар") && InventoryUtil.getSlot(Items.PLAYER_HEAD) == -1 && bl && itemStack.getItem() == Items.PLAYER_HEAD) {
            mc.playerController.windowClick(0, n, 1, ClickType.PICKUP, mc.player);
        }
        if (modes.is("Не выкидать осколки") && hasBukkitDisplayAndCustomModel(itemStack)) {
            mc.playerController.windowClick(0, n, 1, ClickType.PICKUP, mc.player);
        }
    }

    @EventTarget
    public void onEvent(EventPacket e) {
        if (modes.is("Не свапать слоты")) {
            if (!PickItemFixHandler.isFixActive()) {
                if ((e.getPacket() instanceof SHeldItemChangePacket fix) && e.isReceive()) {
                    if (fix.getHeldItemHotbarIndex() != mc.player.inventory.currentItem) {
                        int newSlot = MathHelper.clamp(mc.player.inventory.currentItem >= 8 ? mc.player.inventory.currentItem - 1 : mc.player.inventory.currentItem + 1, 0, 8);
                        mc.player.connection.sendPacket(new CHeldItemChangePacket(newSlot));
                        mc.player.connection.sendPacket(new CHeldItemChangePacket(mc.player.inventory.currentItem));
                        e.setCancelled(true);
                    }
                }
            }
        }
    }

    private boolean hasBukkitDisplayAndCustomModel(ItemStack stack) {
        if (stack == null || !stack.hasTag()) return false;
        CompoundNBT tag = stack.getTag();
        if (tag == null) return false;
        boolean hasPBV = tag.contains("PublicBukkitValues", 10);
        boolean hasDisplay = tag.contains("display", 10);
        boolean hasCmd = tag.contains("CustomModelData", 3) || (hasDisplay && tag.getCompound("display").contains("CustomModelData", 3));
        return hasPBV && hasDisplay && hasCmd;
    }
}
