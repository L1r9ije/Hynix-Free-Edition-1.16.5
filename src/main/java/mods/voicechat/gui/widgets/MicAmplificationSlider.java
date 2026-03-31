package mods.voicechat.gui.widgets;

import mods.voicechat.VoicechatClient;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class MicAmplificationSlider extends DebouncedSlider {

    private static final float MAXIMUM = 4F;

    public MicAmplificationSlider(int xIn, int yIn, int widthIn, int heightIn) {
        super(xIn, yIn, widthIn, heightIn, new StringTextComponent(""), VoicechatClient.CLIENT_CONFIG.microphoneAmplification.get().floatValue() / MAXIMUM);
        func_230979_b_();
    }

    @Override
    protected void func_230979_b_() {
        long amp = Math.round(sliderValue * MAXIMUM * 100F - 100F);
        setMessage(new TranslationTextComponent("message.voicechat.microphone_amplification", (amp > 0F ? "+" : "") + amp + "%"));
    }

    @Override
    public void applyDebounced() {
        VoicechatClient.CLIENT_CONFIG.microphoneAmplification.set(sliderValue * MAXIMUM).save();
    }
}
