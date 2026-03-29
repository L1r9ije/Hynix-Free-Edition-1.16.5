package su.hynix.modules.impl.visuals;

import com.darkmagician6.eventapi.EventTarget;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.util.Hand;
import net.minecraft.util.HandSide;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;
import su.hynix.events.EventSwingAnimation;
import su.hynix.events.EventSwingSpeed;
import su.hynix.events.EventTransformSideFirstPerson;
import su.hynix.hynix;
import su.hynix.modules.Category;
import su.hynix.modules.Module;
import su.hynix.modules.api.constructors.impl.BooleanSetting;
import su.hynix.modules.api.constructors.impl.ModeSetting;
import su.hynix.modules.api.constructors.impl.SliderSetting;
import su.hynix.modules.impl.combat.AttackAura;

public class SwordAnimations extends Module {
    public final ModeSetting swordAnim = new ModeSetting("Мод", "Мод 1", "Мод 1", "Мод 2", "Мод 3", "Мод 4", "Мод 5", "Мод 6", "Мод 7");
    public final SliderSetting angle = new SliderSetting("Угол", 100, 0, 360, 1, () -> swordAnim.is("Мод 2") || swordAnim.is("Мод 4"));
    public final SliderSetting swipePower = new SliderSetting("Сила взмаха", 8, 1, 10, 1, () -> swordAnim.is("Мод 1") || swordAnim.is("Мод 2") || swordAnim.is("Мод 3") || swordAnim.is("Мод 4") || swordAnim.is("Мод 6"));
    public final SliderSetting transform = new SliderSetting("Сила взмаха вниз", 0, 0, 1, 0.1F);
    public final SliderSetting swipeSpeed = new SliderSetting("Плавность взмаха", 11, 1, 20, 1);
    public final BooleanSetting onlyaura = new BooleanSetting("Только с активной AttackAura", false);

    public SwordAnimations() {
        super("Sword Animations", "Позволяет изменить анимацию удара", Category.Visuals);
        addSettings(swordAnim, angle, swipePower, swipeSpeed, transform, onlyaura);
    }

    @EventTarget
    public void onEvent(EventSwingAnimation event) {
        AttackAura aura = hynix.getInstance().getModuleManager().getAttackAura();
        if (onlyaura.get() && AttackAura.target == null) return;
        MatrixStack matrixStack = event.getMatrixStack();
        float anim = MathHelper.sin(MathHelper.sqrt(event.getSwingProgress()) * (float) Math.PI);
        String mode = swordAnim.get();
        if (event.getHand() == Hand.MAIN_HAND) {
            boolean isLeft = mc.player.getPrimaryHand() == HandSide.LEFT;
            switch (mode) {
                case "Мод 1": {
                    float swingSqrt = MathHelper.sqrt(event.getSwingProgress());
                    float direction = isLeft ? -1 : 1;

                    matrixStack.rotate(Vector3f.YP.rotationDegrees(direction * (45 + MathHelper.sin(event.getSwingProgress() * event.getSwingProgress() * (float) Math.PI) / 4 * -20)));
                    matrixStack.rotate(Vector3f.ZP.rotationDegrees(direction * MathHelper.sin(swingSqrt * (float) Math.PI) * -20));
                    matrixStack.rotate(Vector3f.XP.rotationDegrees(MathHelper.sin(swingSqrt * (float) Math.PI) * -swipePower.get() * 10));
                    matrixStack.rotate(Vector3f.YP.rotationDegrees(-direction * 45));
                    break;
                }
                case "Мод 2": {
                    matrixStack.translate(0, 0.15, -0.3F);
                    matrixStack.rotate(Vector3f.YP.rotationDegrees(isLeft ? -90 : 90));
                    matrixStack.rotate(Vector3f.ZP.rotationDegrees(isLeft ? 60 : -60));
                    matrixStack.rotate(Vector3f.XP.rotationDegrees(-angle.get() - swipePower.get() * 10 * anim));
                    break;
                }
                case "Мод 3": {
                    matrixStack.rotate(Vector3f.XP.rotationDegrees(event.getSwingProgress() * (float) Math.PI - swipePower.get() * 10 * anim));
                    break;
                }
                case "Мод 4": {
                    matrixStack.translate(0, 0.15, -0.3F);
                    matrixStack.rotate(Vector3f.YP.rotationDegrees(isLeft ? -70 : 70));
                    matrixStack.rotate(Vector3f.ZP.rotationDegrees(isLeft ? 30 : -30));
                    matrixStack.rotate(Vector3f.XP.rotationDegrees(-angle.get() - swipePower.get() * 10 * anim));
                    break;
                }
                case "Мод 5": {
                    float direction = isLeft ? -1 : 1;
                    matrixStack.rotate(Vector3f.YP.rotationDegrees(direction * 45.0f));
                    matrixStack.rotate(Vector3f.XP.rotationDegrees(anim * -20.0f));
                    matrixStack.rotate(Vector3f.ZP.rotationDegrees(direction * anim * -20.0f));
                    matrixStack.rotate(Vector3f.XP.rotationDegrees(anim * -80.0f));
                    matrixStack.translate(direction * 0.4f, 0.2f, 0.2f);
                    matrixStack.translate(direction * -0.5f, 0.08f, 0.0f);
                    matrixStack.rotate(Vector3f.YP.rotationDegrees(direction * 20.0f));
                    matrixStack.rotate(Vector3f.XP.rotationDegrees(-80.0f));
                    matrixStack.rotate(Vector3f.YP.rotationDegrees(direction * 20.0f));
                    break;
                }
                case "Мод 7": {

                    // event.getMatrixStack().scale((Float) this.scale.getValue(), (Float) this.scale.getValue(), (Float) this.scale.getValue());
                    event.getMatrixStack().translate(0.4F, 0.0F, -0.5F);
                    event.getMatrixStack().rotate(Vector3f.XP.rotationDegrees(anim * -(swipePower.get() * 5) - 5));
                    event.getMatrixStack().rotate(Vector3f.YP.rotationDegrees(90.0F));
                    event.getMatrixStack().rotate(Vector3f.ZP.rotationDegrees(-60.0F));
                    event.getMatrixStack().rotate(Vector3f.YN.rotationDegrees(-(Float) this.swipePower.get() * 2 * anim));
                    event.getMatrixStack().rotate(Vector3f.XP.rotationDegrees(-40.0F + this.swipePower.get() * 2.0F * anim));

                }
                case "Мод 6": {
                    float i = isLeft ? -1 : 1;
                    float f = MathHelper.sin(event.getSwingProgress() * event.getSwingProgress() * (float) Math.PI);
                    matrixStack.rotate(Vector3f.YP.rotationDegrees(i * (45.0F + f * -20.0F)));
                    float f1 = MathHelper.sin(MathHelper.sqrt(event.getSwingProgress()) * (float) Math.PI);
                    matrixStack.rotate(Vector3f.ZP.rotationDegrees(i * f1 * -20.0F));
                    matrixStack.rotate(Vector3f.XP.rotationDegrees(f1 * -swipePower.get() * 10));
                    matrixStack.rotate(Vector3f.YP.rotationDegrees(i * -45.0F));
                    break;
                }
            }
        }
        event.setCancelled(true);
    }


    @EventTarget
    public void onEvent(EventSwingSpeed event) {
        if (event.getHand() != Hand.MAIN_HAND) return;
        event.setSwipeSpeed(swipeSpeed.get().intValue());
        event.setCancelled(true);
    }

    @EventTarget
    public void onEvent(EventTransformSideFirstPerson event) {
        if (event.getHandSide() != mc.player.getPrimaryHand()) return;
        event.setEquippedProg(transform.get());
    }
}