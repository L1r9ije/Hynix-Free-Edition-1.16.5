package su.hynix.handlers.impl;

import com.darkmagician6.eventapi.EventTarget;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import su.hynix.events.EventPacket;
import su.hynix.events.EventUpdate;
import su.hynix.managers.impl.notificationmanager.NotificationManager;
import su.hynix.ui.Interface.elements.impl.NotificationRender;
import su.hynix.utils.Wrapper;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class AlertHandler implements Wrapper {

    private final Set<Effect> previousActiveEffects = new HashSet<>();
    private final int[] lastArmorNotifyPercent = new int[4];
    private final Item[] lastArmorItem = new Item[4];

    @EventTarget
    public void onUpdate(EventUpdate e) {
        if (mc.player == null || mc.world == null) return;

        if (NotificationRender.effects.get()) {
            Set<Effect> currentEffects = new HashSet<>();
            for (EffectInstance eff : mc.player.getActivePotionEffects()) currentEffects.add(eff.getPotion());
            for (Iterator<Effect> it = previousActiveEffects.iterator(); it.hasNext(); ) {
                Effect prev = it.next();
                if (!currentEffects.contains(prev)) {
                    NotificationManager.addNotification("a", "Эффект '" + prev.getDisplayName().getString() + "' больше не активен!", -1);
                    it.remove();
                }
            }
            previousActiveEffects.addAll(currentEffects);
        }

        if (NotificationRender.lowstrength.get()) {
            for (int idx = 0; idx < mc.player.inventory.armorInventory.size(); idx++) {
                ItemStack stack = mc.player.inventory.armorInventory.get(idx);
                if (stack.isEmpty() || stack.getMaxDamage() <= 0) {
                    lastArmorItem[idx] = null;
                    lastArmorNotifyPercent[idx] = 0;
                    continue;
                }

                Item item = stack.getItem();
                if (lastArmorItem[idx] != item) {
                    lastArmorItem[idx] = item;
                    lastArmorNotifyPercent[idx] = 0;
                }

                int maxD = stack.getMaxDamage();
                int used = stack.getDamage();
                int remaining = Math.max(0, maxD - used);
                int remainingPercent = (int) ((remaining * 100.0) / maxD);

                if (remainingPercent >= 1 && remainingPercent <= 5 && lastArmorNotifyPercent[idx] != remainingPercent) {
                    ResourceLocation key = Registry.ITEM.getKey(item);
                    ResourceLocation image = new ResourceLocation(key.getNamespace(), "textures/item/" + key.getPath() + ".png");
                    ITextComponent message = new StringTextComponent("").append(stack.getDisplayName()).appendString(" почти сломан!");
                    NotificationManager.addNotification(image, message);
                    lastArmorNotifyPercent[idx] = remainingPercent;
                }
            }
        }
    }

    @EventTarget
    public void onUpdate(EventPacket e) {

    }
}