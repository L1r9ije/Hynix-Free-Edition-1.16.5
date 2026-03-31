package mods.voicechat.gui.onboarding;

import com.mojang.blaze3d.matrix.MatrixStack;
import mods.voicechat.VoicechatClient;
import mods.voicechat.gui.VoiceChatScreen;
import mods.voicechat.intercompatibility.ClientCompatibilityManager;
import mods.voicechat.voice.client.MicrophoneActivationType;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.*;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nullable;

import static net.minecraft.client.GameSettings.*;

public class FinalOnboardingScreen extends OnboardingScreenBase {

    private static final ITextComponent TITLE = new TranslationTextComponent("message.voicechat.onboarding.final").mergeStyle(TextFormatting.BOLD);
    private static final ITextComponent FINISH_SETUP = new TranslationTextComponent("message.voicechat.onboarding.final.finish_setup");

    protected ITextComponent description;

    public FinalOnboardingScreen(@Nullable Screen previous) {
        super(TITLE, previous);
        description = new StringTextComponent("");
    }

    @Override
    protected void init() {
        super.init();

        IFormattableTextComponent text = new TranslationTextComponent("message.voicechat.onboarding.final.description.success",
                KEY_VOICE_CHAT.getTranslatedKeyMessage().deepCopy().mergeStyle(TextFormatting.BOLD, TextFormatting.UNDERLINE)
        ).appendString("\n\n");

        if (VoicechatClient.CLIENT_CONFIG.microphoneActivationType.get().equals(MicrophoneActivationType.PTT)) {
            text = text.append(new TranslationTextComponent("message.voicechat.onboarding.final.description.ptt",
                    KEY_PTT.getTranslatedKeyMessage().deepCopy().mergeStyle(TextFormatting.BOLD, TextFormatting.UNDERLINE)
            ).mergeStyle(TextFormatting.BOLD)).appendString("\n\n");
        } else {
            text = text.append(new TranslationTextComponent("message.voicechat.onboarding.final.description.voice",
                    KEY_MUTE.getTranslatedKeyMessage().deepCopy().mergeStyle(TextFormatting.BOLD, TextFormatting.UNDERLINE)
            ).mergeStyle(TextFormatting.BOLD)).appendString("\n\n");
        }

        description = text.append(new TranslationTextComponent("message.voicechat.onboarding.final.description.configuration"));

        addBackOrCancelButton();
        addPositiveButton(FINISH_SETUP, button -> OnboardingManager.finishOnboarding());
    }

    @Override
    public void render(MatrixStack stack, int mouseX, int mouseY, float partialTicks) {
        super.render(stack, mouseX, mouseY, partialTicks);
        renderTitle(stack, TITLE);
        renderMultilineText(stack, description);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            OnboardingManager.finishOnboarding();
            return true;
        }
        if (keyCode == ClientCompatibilityManager.INSTANCE.getBoundKeyOf(KEY_VOICE_CHAT).getKeyCode()) {
            OnboardingManager.finishOnboarding();
            minecraft.displayGuiScreen(new VoiceChatScreen());
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

}
