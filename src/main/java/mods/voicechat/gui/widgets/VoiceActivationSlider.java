package mods.voicechat.gui.widgets;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import mods.voicechat.Voicechat;
import mods.voicechat.VoicechatClient;
import mods.voicechat.voice.common.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

import javax.annotation.Nullable;

public class VoiceActivationSlider extends DebouncedSlider implements MicTestButton.MicListener {

    private static final ResourceLocation SLIDER = new ResourceLocation(Voicechat.MODID + "/textures/gui/voice_activation_slider.png");
    private static final ITextComponent NO_ACTIVATION = new TranslationTextComponent("message.voicechat.voice_activation.disabled").mergeStyle(TextFormatting.RED);

    private double micValue;

    public VoiceActivationSlider(int x, int y, int width, int height) {
        super(x, y, width, height, new StringTextComponent(""), Utils.dbToPerc(VoicechatClient.CLIENT_CONFIG.voiceActivationThreshold.get().floatValue()));
        func_230979_b_();
    }

    @Override
    protected void renderBg(MatrixStack poseStack, Minecraft minecraft, int i, int j) {
        minecraft.getTextureManager().bindTexture(SLIDER);
        RenderSystem.color4f(1F, 1F, 1F, 1F);
        int width = (int) ((getWidth() - 2) * micValue);
        blit(poseStack, x + 1, y + 1, 0, 0, width, 18);
        super.renderBg(poseStack, minecraft, i, j);
    }

    @Override
    protected void func_230979_b_() {
        long db = Math.round(Utils.percToDb(sliderValue));
        TranslationTextComponent component = new TranslationTextComponent("message.voicechat.voice_activation", db);

        if (db >= -10L) {
            component.mergeStyle(TextFormatting.RED);
        }

        setMessage(component);
    }

    @Nullable
    public ITextComponent getHoverText() {
        if (sliderValue >= 1D) {
            return NO_ACTIVATION;
        }
        return null;
    }

    public boolean isHovered() {
        return isHovered;
    }

    @Override
    public void applyDebounced() {
        VoicechatClient.CLIENT_CONFIG.voiceActivationThreshold.set(Utils.percToDb(sliderValue)).save();
    }

    @Override
    public void onMicValue(double percentage) {
        this.micValue = percentage;
    }
}
