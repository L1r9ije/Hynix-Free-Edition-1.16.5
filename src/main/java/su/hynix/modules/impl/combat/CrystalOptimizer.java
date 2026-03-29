package su.hynix.modules.impl.combat;


import com.darkmagician6.eventapi.EventTarget;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EnderCrystalEntity;
import net.minecraft.util.Hand;
import su.hynix.events.EventUpdate;
import su.hynix.modules.Category;
import su.hynix.modules.Module;
import su.hynix.modules.api.constructors.impl.BooleanSetting;
import su.hynix.modules.api.constructors.impl.SliderSetting;
import su.hynix.utils.player.RayTraceUtil;

public class CrystalOptimizer extends Module {
    //
    private final SliderSetting distance = new SliderSetting(
            "Distance",
            3,
            1,
            4, 0.1F
    );

    private final BooleanSetting dontExplosion = new BooleanSetting("Dont Explosion Self", false);

    public CrystalOptimizer() {
        super("CrystalOptimizer", "Убирает кд на крис", Category.Combat);
        addSettings(distance, dontExplosion);
    }


    @EventTarget
    public void update(EventUpdate event) {
        for (Entity entity : mc.world.getAllEntities()) {
            if (entity instanceof EnderCrystalEntity && mc.player.getDistance(entity) <= distance.get() && RayTraceUtil.getMouseOver(entity, mc.player.rotationYaw, mc.player.rotationPitch, 6) == entity) {
                double y = entity.getPosY();
                double yPlayer = mc.player.getPosY() + 0.6f;

                if (!dontExplosion.get() || y >= yPlayer) {
                    mc.playerController.attackEntity(mc.player, entity);
                    mc.player.swingArm(Hand.MAIN_HAND);
                }
            }
        }
    }

}
