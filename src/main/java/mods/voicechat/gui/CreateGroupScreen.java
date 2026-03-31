package mods.voicechat.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import mods.voicechat.Voicechat;
import mods.voicechat.net.ClientServerNetManager;
import mods.voicechat.net.CreateGroupPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import org.lwjgl.glfw.GLFW;

public class CreateGroupScreen extends VoiceChatScreenBase {

    private static final ResourceLocation TEXTURE = new ResourceLocation(Voicechat.MODID + "/textures/gui/gui_create_group.png");
    private static final ITextComponent TITLE = new TranslationTextComponent("gui.voicechat.create_group.title");
    private static final ITextComponent CREATE = new TranslationTextComponent("message.voicechat.create");
    private static final ITextComponent CREATE_GROUP = new TranslationTextComponent("message.voicechat.create_group");
    private static final ITextComponent GROUP_NAME = new TranslationTextComponent("message.voicechat.group_name");
    private static final ITextComponent OPTIONAL_PASSWORD = new TranslationTextComponent("message.voicechat.optional_password");
    private static final ITextComponent GROUP_TYPE = new TranslationTextComponent("message.voicechat.group_type");

    private TextFieldWidget groupName;
    private TextFieldWidget password;
    private GroupType groupType;
    private Button groupTypeButton;
    private Button createGroup;

    public CreateGroupScreen() {
        super(TITLE, 195, 124);
        groupType = GroupType.NORMAL;
    }

    @Override
    protected void init() {
        super.init();
        hoverAreas.clear();
        children.clear();
        buttons.clear();

        minecraft.keyboardListener.enableRepeatEvents(true);

        groupName = new TextFieldWidget(font, guiLeft + 7, guiTop + 31, xSize - 7 * 2, 12, new StringTextComponent(""));
        groupName.setMaxStringLength(24);
        groupName.setValidator(s -> s.isEmpty() || Voicechat.GROUP_REGEX.matcher(s).matches());
        addButton(groupName);

        password = new TextFieldWidget(font, guiLeft + 7, guiTop + 57, xSize - 7 * 2, 12, new StringTextComponent(""));
        password.setMaxStringLength(32);
        password.setValidator(s -> s.isEmpty() || Voicechat.GROUP_REGEX.matcher(s).matches());
        addButton(password);

        groupTypeButton = new Button(guiLeft + 6, guiTop + 74, xSize - 12, 20, GROUP_TYPE, (button) -> {
            groupType = GroupType.values()[(groupType.ordinal() + 1) % GroupType.values().length];
        }) {
            public ITextComponent getMessage() {
                return new TranslationTextComponent("message.voicechat.group_type").appendString(": ").append(groupType.getTranslation());
            }
        };
        addButton(groupTypeButton);

        createGroup = new Button(guiLeft + 6, guiTop + ySize - 27, xSize - 12, 20, CREATE, button -> {
            createGroup();
        });
        addButton(createGroup);
    }

    private void createGroup() {
        if (!groupName.getText().isEmpty()) {
            ClientServerNetManager.sendToServer(new CreateGroupPacket(groupName.getText(), password.getText().isEmpty() ? null : password.getText(), groupType.getType()));
        }
    }

    @Override
    public void tick() {
        super.tick();
        groupName.tick();
        password.tick();
        createGroup.active = !groupName.getText().isEmpty();
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
        font.func_243248_b(poseStack, CREATE_GROUP, guiLeft + xSize / 2 - font.getStringPropertyWidth(CREATE_GROUP) / 2, guiTop + 7, FONT_COLOR);
        font.func_243248_b(poseStack, GROUP_NAME, guiLeft + 8, guiTop + 7 + font.FONT_HEIGHT + 5, FONT_COLOR);
        font.func_243248_b(poseStack, OPTIONAL_PASSWORD, guiLeft + 8, guiTop + 7 + (font.FONT_HEIGHT + 5) * 2 + 10 + 2, FONT_COLOR);

        if (mouseX >= groupTypeButton.x && mouseY >= groupTypeButton.y && mouseX < groupTypeButton.x + groupTypeButton.getWidth() && mouseY < groupTypeButton.y + groupTypeButton.getHeightRealms()) {
            renderTooltip(poseStack, minecraft.fontRenderer.trimStringToWidth(groupType.getDescription(), 200), mouseX, mouseY);
        }
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
            createGroup();
            return true;
        }
        return false;
    }

    @Override
    public void resize(Minecraft client, int width, int height) {
        String groupNameText = groupName.getText();
        String passwordText = password.getText();
        init(client, width, height);
        groupName.setText(groupNameText);
        password.setText(passwordText);
    }

}
