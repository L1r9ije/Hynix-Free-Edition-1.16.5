package mods.voicechat.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import mods.voicechat.Voicechat;
import mods.voicechat.net.ClientServerNetManager;
import mods.voicechat.net.JoinGroupPacket;
import mods.voicechat.voice.common.ClientGroup;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import org.lwjgl.glfw.GLFW;

public class EnterPasswordScreen extends VoiceChatScreenBase {

    private static final ResourceLocation TEXTURE = new ResourceLocation(Voicechat.MODID + "/textures/gui/gui_enter_password.png");
    private static final ITextComponent TITLE = new TranslationTextComponent("gui.voicechat.enter_password.title");
    private static final ITextComponent JOIN_GROUP = new TranslationTextComponent("message.voicechat.join_group");
    private static final ITextComponent ENTER_GROUP_PASSWORD = new TranslationTextComponent("message.voicechat.enter_group_password");
    private static final ITextComponent PASSWORD = new TranslationTextComponent("message.voicechat.password");

    private TextFieldWidget password;
    private Button joinGroup;
    private final ClientGroup group;

    public EnterPasswordScreen(ClientGroup group) {
        super(TITLE, 195, 74);
        this.group = group;
    }

    @Override
    protected void init() {
        super.init();
        hoverAreas.clear();
        children.clear();
        buttons.clear();

        minecraft.keyboardListener.enableRepeatEvents(true);

        password = new TextFieldWidget(font, guiLeft + 7, guiTop + 7 + (font.FONT_HEIGHT + 5) * 2 - 5 + 1, xSize - 7 * 2, 12, new StringTextComponent(""));
        password.setMaxStringLength(32);
        password.setValidator(s -> s.isEmpty() || Voicechat.GROUP_REGEX.matcher(s).matches());
        addButton(password);

        joinGroup = new Button(guiLeft + 7, guiTop + ySize - 20 - 7, xSize - 7 * 2, 20, JOIN_GROUP, button -> {
            joinGroup();
        });
        addButton(joinGroup);
    }

    private void joinGroup() {
        if (!password.getText().isEmpty()) {
            ClientServerNetManager.sendToServer(new JoinGroupPacket(group.id(), password.getText()));
        }
    }

    @Override
    public void tick() {
        super.tick();
        password.tick();
        joinGroup.active = !password.getText().isEmpty();
    }

    @Override
    public void onClose() {
        super.onClose();
        minecraft.keyboardListener.enableRepeatEvents(false);
    }

    @Override
    public void renderBackground(MatrixStack poseStack, int mouseX, int mouseY, float delta) {
        minecraft.getTextureManager().bindTexture(TEXTURE);
        blit(poseStack, guiLeft, guiTop, 0, 0, xSize, ySize);
    }

    @Override
    public void renderForeground(MatrixStack poseStack, int mouseX, int mouseY, float delta) {
        font.func_243248_b(poseStack, ENTER_GROUP_PASSWORD, guiLeft + xSize / 2 - font.getStringPropertyWidth(ENTER_GROUP_PASSWORD) / 2, guiTop + 7, FONT_COLOR);
        font.func_243248_b(poseStack, PASSWORD, guiLeft + 8, guiTop + 7 + font.FONT_HEIGHT + 5, FONT_COLOR);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            minecraft.displayGuiScreen(null);
            return true;
        }
        if (super.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ENTER) {
            joinGroup();
            return true;
        }
        return false;
    }

    @Override
    public void resize(Minecraft client, int width, int height) {
        String passwordText = password.getText();
        init(client, width, height);
        password.setText(passwordText);
    }

}
