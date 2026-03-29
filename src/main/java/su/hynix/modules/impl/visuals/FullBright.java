package su.hynix.modules.impl.visuals;

import com.darkmagician6.eventapi.EventTarget;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.LightType;
import su.hynix.events.EventUpdate;
import su.hynix.modules.Category;
import su.hynix.modules.Module;
import su.hynix.modules.api.constructors.impl.BooleanSetting;
import su.hynix.modules.api.constructors.impl.ModeSetting;
import su.hynix.modules.api.constructors.impl.SliderSetting;

public class FullBright extends Module {
    public ModeSetting mode = new ModeSetting("Режим", "Гамма", "Гамма", "Зелье");
    public BooleanSetting adaptiveBrightness = new BooleanSetting("Адаптивная яркость", false, () -> mode.is("Гамма"));
    public SliderSetting gammaValue = new SliderSetting("Яркость", 8.0f, 0.5f, 20.0f, 0.1f, () -> !adaptiveBrightness.get() && mode.is("Гамма"));

    public FullBright() {
        super("Full Bright", "Освещает темные места в мире", Category.Visuals);
        addSettings(mode, adaptiveBrightness, gammaValue);
    }

    @EventTarget
    public void onEvent(EventUpdate event) {
        if (mode.is("Гамма")) {
            if (adaptiveBrightness.get()) {
                BlockPos p = mc.player.getPosition();
                int lvl = Math.max(mc.world.getLightFor(LightType.BLOCK, p), mc.world.getLightFor(LightType.SKY, p));
                double t = 1.2 + Math.pow(1.0 - (lvl / 15.0), 2.0) * (12.0 - 1.2);
                mc.gameSettings.gamma += (MathHelper.clamp(t, 1.2, 12.0) - mc.gameSettings.gamma) * 0.3;
            } else {
                mc.gameSettings.gamma = gammaValue.get() / 5;
            }
        } else {
            mc.gameSettings.gamma = 0;
        }

        if (this.mode.is("Зелье")) {
            mc.player.addPotionEffect(new EffectInstance(Effects.NIGHT_VISION, 9999999, 1));
        } else {
            mc.player.removePotionEffect(Effects.NIGHT_VISION);
        }
    }

    @Override
    public void onDisable() {
        mc.gameSettings.gamma = 0;
        mc.player.removeActivePotionEffect((new EffectInstance(Effects.NIGHT_VISION)).getPotion());
        super.onDisable();
    }
}
