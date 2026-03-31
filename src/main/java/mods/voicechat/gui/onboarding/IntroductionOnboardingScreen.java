package mods.voicechat.gui.onboarding;

import com.mojang.blaze3d.matrix.MatrixStack;
import mods.voicechat.intercompatibility.CommonCompatibilityManager;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

import javax.annotation.Nullable;

public class IntroductionOnboardingScreen extends OnboardingScreenBase {

    private static final ITextComponent TITLE = new TranslationTextComponent("message.voicechat.onboarding.introduction.title", CommonCompatibilityManager.INSTANCE.getModName()).mergeStyle(TextFormatting.BOLD);
    private static final ITextComponent DESCRIPTION = new TranslationTextComponent("message.voicechat.onboarding.introduction.description");
    private static final ITextComponent SKIP = new TranslationTextComponent("message.voicechat.onboarding.introduction.skip");

    public IntroductionOnboardingScreen(@Nullable Screen previous) {
        super(TITLE, previous);
    }

    @Override
    protected void init() {
        super.init();

        Button skipButton = new Button(
                guiLeft + (contentWidth - contentWidth / 2 - PADDING / 2) / 2,
                guiTop + contentHeight - BUTTON_HEIGHT * 2 - PADDING,
                contentWidth / 2 - PADDING / 2,
                BUTTON_HEIGHT,
                SKIP,
                button -> minecraft.displayGuiScreen(new SkipOnboardingScreen(IntroductionOnboardingScreen.this))
        );
        addButton(skipButton);

        addBackOrCancelButton();
        addNextButton();
    }

    @Override
    public Screen getNextScreen() {
        return new MicOnboardingScreen(this);
    }

    @Override
    public void render(MatrixStack stack, int mouseX, int mouseY, float partialTicks) {
        super.render(stack, mouseX, mouseY, partialTicks);
        renderTitle(stack, TITLE);
        renderMultilineText(stack, DESCRIPTION);
    }

}
