package su.hynix.modules.impl.combat;

import com.darkmagician6.eventapi.EventTarget;
import net.minecraft.entity.player.PlayerEntity;
import su.hynix.events.EventAttack;
import su.hynix.hynix;
import su.hynix.modules.Category;
import su.hynix.modules.Module;

public class NoFriendDamage extends Module {

    public NoFriendDamage() {
        super("NoFriendDamage", "Отключает возможность наносить урон друзьям", Category.Combat);
    }

    @EventTarget
    public void onEvent(EventAttack event) {
        AttackAura attackAura = (AttackAura) hynix.getInstance().getModuleManager().getModule(AttackAura.class);
        if (event.getTarget() instanceof PlayerEntity player && hynix.getInstance().getFriendManager().isFriend(player.getGameProfile().getName()) && !(attackAura.isEnabled() && AttackAura.target == event.getTarget())) {
            event.setCancelled(true);
        }
    }
}