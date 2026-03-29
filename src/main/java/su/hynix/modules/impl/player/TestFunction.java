package su.hynix.modules.impl.player;

import com.darkmagician6.eventapi.EventTarget;
import net.minecraft.potion.Effects;
import net.minecraft.util.math.vector.Vector3d;
import su.hynix.events.EventUpdate;
import su.hynix.modules.Category;
import su.hynix.modules.Module;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TestFunction extends Module {


    private final Map<UUID, Vector3d> lastPearlPos = new HashMap<>();

    public TestFunction() {
        super("TestFunction", "Тест Функция", Category.Player);
    }

    @EventTarget
    public void update(EventUpdate eventUpdate) {
        boolean hasEffects = (mc.player.isPotionActive(Effects.LEVITATION));
        if (hasEffects) {
            mc.player.removePotionEffect(Effects.LEVITATION);
        }

    }
}
