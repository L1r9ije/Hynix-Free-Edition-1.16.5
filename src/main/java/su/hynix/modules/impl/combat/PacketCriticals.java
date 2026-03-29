package su.hynix.modules.impl.combat;

import su.hynix.modules.Category;
import su.hynix.modules.Module;
import su.hynix.modules.api.constructors.impl.ModeSetting;

public class PacketCriticals extends Module {

    private final ModeSetting mode = new ModeSetting("Режим", "Reallyworld", "Reallyworld");

    public PacketCriticals() {
        super("Criticals", "Наносит критические удары без надобности прыгать", Category.Combat);
        addSettings(mode);
    }
}
