package su.hynix.modules.impl.visuals;

import com.darkmagician6.eventapi.EventTarget;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import su.hynix.events.EventHandsRender;
import su.hynix.modules.Category;
import su.hynix.modules.Module;
import su.hynix.modules.api.constructors.impl.ColorSetting;
import su.hynix.utils.render.ColorUtil;
import su.hynix.utils.render.shader.KawaseBlur;
import su.hynix.utils.render.shader.ShaderUtil;

public class Hands extends Module {

    private final ColorSetting colorSetting = new ColorSetting("Цвет", false, ColorUtil.hex("#8A98FFFF"));

    public Hands() {
        super("Hands", "Накладывает эффект зеркала на ваши руки, изменяя их внешний вид", Category.Visuals);
        addSettings(colorSetting);
    }


    @EventTarget
    private void OnEvent(EventHandsRender.Pre eventHandsRender) {
        ShaderUtil.hands.attach();
        GL13.glActiveTexture(GL20.GL_TEXTURE5);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, KawaseBlur.blur.BLURRED.framebufferTexture);
        GL13.glActiveTexture(GL20.GL_TEXTURE0);

        ShaderUtil.hands.setUniform("originalTexture", 0);
        ShaderUtil.hands.setUniform("blurredTexture", 5);


        float[] rgba = ColorUtil.getColor(colorSetting.get());
        ShaderUtil.hands.setUniformf("multiplier", rgba[0], rgba[1], rgba[2], rgba[3]);
        ShaderUtil.hands.setUniformf("viewOffset", mw.getFramebufferWidth() / 8F, mw.getFramebufferHeight());
        ShaderUtil.hands.setUniformf("resolution", mw.getFramebufferWidth() * 2, mw.getFramebufferHeight() * 2);
    }

    @EventTarget
    private void OnEvent(EventHandsRender.Post eventHandsRender) {
        ShaderUtil.hands.detach();
    }
}
