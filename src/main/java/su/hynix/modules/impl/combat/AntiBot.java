package su.hynix.modules.impl.combat;

import com.darkmagician6.eventapi.EventTarget;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import su.hynix.events.EventSwapWorld;
import su.hynix.events.EventUpdate;
import su.hynix.modules.Category;
import su.hynix.modules.Module;

import java.util.ArrayList;
import java.util.List;

public class AntiBot extends Module {
    public static final List<Entity> bot = new ArrayList<>();

    public AntiBot() {
        super("Anti Bot", "Определяет и метит ботов, вызванных системой античита", Category.Combat);
    }

    @EventTarget
    public void onUpdate(EventUpdate e) {
        if (mc.player == null || mc.world == null) return;

        for (Entity entity : mc.world.getAllEntities()) {
            if (!(entity instanceof PlayerEntity player) || player.equals(mc.player)) continue;

            boolean isBot;

            boolean hasValidArmor = player.inventory.armorInventory.stream().allMatch(armorItem -> armorItem.getItem() != Items.AIR && armorItem.isEnchantable() && !armorItem.isDamaged());
            boolean hasValidEquipment = player.getHeldItemOffhand().getItem() == Items.AIR && (player.inventory.armorInventory.stream().anyMatch(armorItem -> armorItem.getItem() == Items.LEATHER_BOOTS || armorItem.getItem() == Items.LEATHER_LEGGINGS || armorItem.getItem() == Items.LEATHER_CHESTPLATE || armorItem.getItem() == Items.LEATHER_HELMET || armorItem.getItem() == Items.IRON_BOOTS || armorItem.getItem() == Items.IRON_LEGGINGS || armorItem.getItem() == Items.IRON_CHESTPLATE || armorItem.getItem() == Items.IRON_HELMET));
            boolean hasFullFood = player.getFoodStats().getFoodLevel() == 20;

            isBot = hasValidArmor && hasValidEquipment && hasFullFood;

            if (isBot) {
                if (!bot.contains(player)) {
                    bot.add(player);
                }
            } else {
                bot.remove(player);
            }
        }
    }

    @EventTarget
    public void onEventWorldChanged(EventSwapWorld e) {
        bot.clear();
    }

    @Override
    public void onDisable() {
        bot.clear();
        super.onDisable();
    }
}