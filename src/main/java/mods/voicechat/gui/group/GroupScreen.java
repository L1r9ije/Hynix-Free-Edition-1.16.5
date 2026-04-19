package mods.voicechat.gui.group;

import com.mojang.blaze3d.matrix.MatrixStack;
import mods.voicechat.Voicechat;
import mods.voicechat.VoicechatClient;
import mods.voicechat.api.Group;
import mods.voicechat.gui.GroupType;
import mods.voicechat.gui.tooltips.DisableTooltipSupplier;
import mods.voicechat.gui.tooltips.HideGroupHudTooltipSupplier;
import mods.voicechat.gui.tooltips.MuteTooltipSupplier;
import mods.voicechat.gui.widgets.ImageButton;
import mods.voicechat.gui.widgets.ListScreenBase;
import mods.voicechat.gui.widgets.ToggleImageButton;
import mods.voicechat.net.ClientServerNetManager;
import mods.voicechat.net.LeaveGroupPacket;
import mods.voicechat.voice.client.ClientManager;
import mods.voicechat.voice.client.ClientPlayerStateManager;
import mods.voicechat.voice.client.MicrophoneActivationType;
import mods.voicechat.voice.common.ClientGroup;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.Collections;

public class GroupScreen extends ListScreenBase {

    protected static final ResourceLocation TEXTURE = new ResourceLocation(Voicechat.MODID + "/textures/gui/gui_group.png");
    protected static final ResourceLocation LEAVE = new ResourceLocation(Voicechat.MODID + "/textures/icons/leave.png");
    protected static final ResourceLocation MICROPHONE = new ResourceLocation(Voicechat.MODID + "/textures/icons/microphone_button.png");
    protected static final ResourceLocation SPEAKER = new ResourceLocation(Voicechat.MODID + "/textures/icons/speaker_button.png");
    protected static final ResourceLocation GROUP_HUD = new ResourceLocation(Voicechat.MODID + "/textures/icons/group_hud_button.png");
    protected static final ITextComponent TITLE = new TranslationTextComponent("gui.voicechat.group.title");
    protected static final ITextComponent LEAVE_GROUP = new TranslationTextComponent("message.voicechat.leave_group");

    protected static final int HEADER_SIZE = 16;
    protected static final int FOOTER_SIZE = 32;
    protected static final int UNIT_SIZE = 18;
    protected static final int CELL_HEIGHT = 36;
    protected final ClientGroup group;
    protected GroupList groupList;
    protected int units;
    protected ToggleImageButton mute;
    protected ToggleImageButton disable;
    protected ToggleImageButton showHUD;
    protected ImageButton leave;

    public GroupScreen(ClientGroup group) {
        super(TITLE, 236, 0);
        this.group = group;
    }

    @Override
    protected void init() {
        super.init();
        guiLeft = guiLeft + 2;
        guiTop = 32;
        int minUnits = MathHelper.ceil((float) (CELL_HEIGHT + 4) / (float) UNIT_SIZE);
        units = Math.max(minUnits, (height - HEADER_SIZE - FOOTER_SIZE - guiTop * 2) / UNIT_SIZE);
        ySize = HEADER_SIZE + units * UNIT_SIZE + FOOTER_SIZE;

        ClientPlayerStateManager stateManager = ClientManager.getPlayerStateManager();

        if (groupList != null) {
            groupList.updateSize(width, units * UNIT_SIZE, guiTop + HEADER_SIZE);
        } else {
            groupList = new GroupList(this, width, units * UNIT_SIZE, guiTop + HEADER_SIZE, CELL_HEIGHT);
        }
        addListener(groupList);

        int buttonY = guiTop + ySize - 20 - 7;
        int buttonSize = 20;

        mute = new ToggleImageButton(guiLeft + 7, buttonY, MICROPHONE, stateManager::isMuted, button -> {
            stateManager.setMuted(!stateManager.isMuted());
        }, new MuteTooltipSupplier(this, stateManager));
        addButton(mute);

        disable = new ToggleImageButton(guiLeft + 7 + buttonSize + 3, buttonY, SPEAKER, stateManager::isDisabled, button -> {
            stateManager.setDisabled(!stateManager.isDisabled());
        }, new DisableTooltipSupplier(this, stateManager));
        addButton(disable);

        showHUD = new ToggleImageButton(guiLeft + 7 + (buttonSize + 3) * 2, buttonY, GROUP_HUD, VoicechatClient.CLIENT_CONFIG.showGroupHUD::get, button -> {
            VoicechatClient.CLIENT_CONFIG.showGroupHUD.set(!VoicechatClient.CLIENT_CONFIG.showGroupHUD.get()).save();
        }, new HideGroupHudTooltipSupplier(this));
        addButton(showHUD);

        leave = new ImageButton(guiLeft + xSize - buttonSize - 7, buttonY, LEAVE, button -> {
            ClientServerNetManager.sendToServer(new LeaveGroupPacket());
            minecraft.displayGuiScreen(new JoinGroupScreen());
        }, (button, matrices, mouseX, mouseY) -> {
            renderTooltip(matrices, Collections.singletonList(LEAVE_GROUP.func_241878_f()), mouseX, mouseY);
        });
        addButton(leave);

        checkButtons();
    }

    @Override
    public void tick() {
        super.tick();
        checkButtons();
    }

    private void checkButtons() {
        mute.active = VoicechatClient.CLIENT_CONFIG.microphoneActivationType.get().equals(MicrophoneActivationType.VOICE);
        showHUD.active = !VoicechatClient.CLIENT_CONFIG.hideIcons.get();
    }

    @Override
    public void renderBackground(MatrixStack poseStack, int mouseX, int mouseY, float delta) {
        minecraft.getTextureManager().bindTexture(TEXTURE);
        blit(poseStack, guiLeft, guiTop, 0, 0, xSize, HEADER_SIZE);
        for (int i = 0; i < units; i++) {
            blit(poseStack, guiLeft, guiTop + HEADER_SIZE + UNIT_SIZE * i, 0, HEADER_SIZE, xSize, UNIT_SIZE);
        }
        blit(poseStack, guiLeft, guiTop + HEADER_SIZE + UNIT_SIZE * units, 0, HEADER_SIZE + UNIT_SIZE, xSize, FOOTER_SIZE);
        blit(poseStack, guiLeft + 10, guiTop + HEADER_SIZE + 6 - 2, xSize, 0, 12, 12);
    }

    @Override
    public void renderForeground(MatrixStack poseStack, int mouseX, int mouseY, float delta) {
        ITextComponent title;
        if (group.type().equals(Group.Type.NORMAL)) {
            title = new TranslationTextComponent("message.voicechat.group_title", new StringTextComponent(group.name()));
        } else {
            title = new TranslationTextComponent("message.voicechat.group_type_title", new StringTextComponent(group.name()), GroupType.fromType(group.type()).getTranslation());
        }

        font.func_243248_b(poseStack, title, guiLeft + xSize / 2 - font.getStringPropertyWidth(title) / 2, guiTop + 5, FONT_COLOR);

        groupList.render(poseStack, mouseX, mouseY, delta);
    }

}
