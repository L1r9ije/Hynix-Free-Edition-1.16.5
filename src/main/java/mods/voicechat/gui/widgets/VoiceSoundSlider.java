package mods.voicechat.gui.widgets;

import mods.voicechat.VoicechatClient;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class VoiceSoundSlider extends DebouncedSlider {

    public VoiceSoundSlider(int x, int y, int width, int height) {
        super(x, y, width, height, new StringTextComponent(""), VoicechatClient.CLIENT_CONFIG.voiceChatVolume.get().floatValue() / 2F);
        func_230979_b_();
    }

    @Override
    protected void func_230979_b_() {
        setMessage(getMsg());
    }

    public ITextComponent getMsg() {
        return new TranslationTextComponent("message.voicechat.voice_chat_volume", Math.round(sliderValue * 200F) + "%");
    }

    @Override
    public void applyDebounced() {
        VoicechatClient.CLIENT_CONFIG.voiceChatVolume.set(sliderValue * 2F).save();
    }
}
