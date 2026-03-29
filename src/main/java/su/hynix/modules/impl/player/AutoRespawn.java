package su.hynix.modules.impl.player;

import com.darkmagician6.eventapi.EventTarget;
import su.hynix.events.EventDeath;
import su.hynix.hynix;
import su.hynix.modules.Category;
import su.hynix.modules.Module;
import su.hynix.modules.api.constructors.impl.BooleanSetting;
import su.hynix.modules.impl.combat.AttackAura;


public class AutoRespawn extends Module {
    private final BooleanSetting disableAura = new BooleanSetting("Отключать AttackAura", true);

    public AutoRespawn() {
        super("AutoRespawn", "Автоматически возраждает вас после смерти", Category.Player);
        addSettings(disableAura);
    }

    @EventTarget
    public void onUpdate(EventDeath event) {
        if (disableAura.get()) {
            if (hynix.getInstance().getModuleManager().getModule(AttackAura.class).isEnabled()) {
                hynix.getInstance().getModuleManager().getModule(AttackAura.class).toggle();
            }
        }
        mc.player.respawnPlayer();
        mc.displayGuiScreen(null);
    }
}