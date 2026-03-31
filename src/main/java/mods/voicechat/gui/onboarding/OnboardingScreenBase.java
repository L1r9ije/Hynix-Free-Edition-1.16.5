package mods.voicechat.gui.onboarding;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import javax.annotation.Nullable;
import java.util.List;

public abstract class OnboardingScreenBase extends Screen {

    public static final ITextComponent NEXT = new TranslationTextComponent("message.voicechat.onboarding.next");
    public static final ITextComponent BACK = new TranslationTextComponent("message.voicechat.onboarding.back");
    public static final ITextComponent CANCEL = new TranslationTextComponent("message.voicechat.onboarding.cancel");

    protected static final int TEXT_COLOR = 0xFFFFFFFF;
    protected static final int PADDING = 8;
    protected static final int SMALL_PADDING = 2;
    protected static final int BUTTON_HEIGHT = 20;

    protected int contentWidth;
    protected int guiLeft;
    protected int guiTop;
    protected int contentHeight;

    @Nullable
    protected Screen previous;

    public OnboardingScreenBase(ITextComponent title, @Nullable Screen previous) {
        super(title);
        this.previous = previous;
    }

    @Override
    protected void init() {
        super.init();

        contentWidth = width / 2;
        guiLeft = (width - contentWidth) / 2;
        guiTop = 20;
        contentHeight = height - guiTop * 2;
    }

    @Override
    public void render(MatrixStack stack, int mouseX, int mouseY, float partialTicks) {
        renderBackground(stack);
        super.render(stack, mouseX, mouseY, partialTicks);
    }

    @Nullable
    public Screen getNextScreen() {
        return null;
    }

    protected void addPositiveButton(ITextComponent text, Button.IPressable onPress) {
        Button nextButton = new Button(guiLeft + contentWidth / 2 + PADDING / 2, guiTop + contentHeight - BUTTON_HEIGHT, contentWidth / 2 - PADDING / 2, BUTTON_HEIGHT, text, onPress);
        addButton(nextButton);
    }

    protected void addNextButton() {
        addPositiveButton(NEXT, button -> {
            minecraft.displayGuiScreen(getNextScreen());
        });
    }

    protected void addBackOrCancelButton(boolean big) {
        ITextComponent text = CANCEL;
        if (previous instanceof OnboardingScreenBase) {
            text = BACK;
        }
        Button cancel = new Button(big ? guiLeft + (contentWidth - contentWidth / 2 - PADDING / 2) / 2 : guiLeft, guiTop + contentHeight - BUTTON_HEIGHT, contentWidth / 2 - PADDING / 2, BUTTON_HEIGHT, text, button -> {
            minecraft.displayGuiScreen(previous);
        });
        addButton(cancel);
    }

    protected void addBackOrCancelButton() {
        addBackOrCancelButton(false);
    }

    protected void renderTitle(MatrixStack stack, ITextComponent titleComponent) {
        int titleWidth = font.getStringPropertyWidth(titleComponent);
        font.func_238407_a_(stack, titleComponent.func_241878_f(), width / 2 - titleWidth / 2, guiTop, TEXT_COLOR);
    }

    protected void renderMultilineText(MatrixStack stack, ITextComponent textComponent) {
        List<IReorderingProcessor> text = font.trimStringToWidth(textComponent, contentWidth);

        for (int i = 0; i < text.size(); i++) {
            IReorderingProcessor line = text.get(i);
            font.func_238407_a_(stack, line, width / 2 - font.func_243245_a(line) / 2, guiTop + font.FONT_HEIGHT + 20 + i * (font.FONT_HEIGHT + 1), TEXT_COLOR);
        }
    }

}
