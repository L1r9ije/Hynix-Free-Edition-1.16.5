package mods.voicechat.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import mods.voicechat.Voicechat;
import mods.voicechat.VoicechatClient;
import mods.voicechat.VoicechatGate;
import mods.voicechat.gui.group.GroupScreen;
import mods.voicechat.gui.group.JoinGroupScreen;
import mods.voicechat.gui.tooltips.DisableTooltipSupplier;
import mods.voicechat.gui.tooltips.HideTooltipSupplier;
import mods.voicechat.gui.tooltips.MuteTooltipSupplier;
import mods.voicechat.gui.tooltips.RecordingTooltipSupplier;
import mods.voicechat.gui.volume.AdjustVolumesScreen;
import mods.voicechat.gui.widgets.ImageButton;
import mods.voicechat.gui.widgets.ToggleImageButton;
import mods.voicechat.intercompatibility.ClientCompatibilityManager;
import mods.voicechat.voice.client.AudioRecorder;
import mods.voicechat.voice.client.ClientManager;
import mods.voicechat.voice.client.ClientPlayerStateManager;
import mods.voicechat.voice.client.ClientVoicechat;
import mods.voicechat.voice.common.ClientGroup;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

import javax.annotation.Nullable;

import static net.minecraft.client.GameSettings.KEY_VOICE_CHAT;

public class VoiceChatScreen extends VoiceChatScreenBase {

    private static final ResourceLocation TEXTURE = new ResourceLocation(Voicechat.MODID + "/textures/gui/gui_voicechat.png");
    private static final ResourceLocation MICROPHONE = new ResourceLocation(Voicechat.MODID + "/textures/icons/microphone_button.png");
    private static final ResourceLocation HIDE = new ResourceLocation(Voicechat.MODID + "/textures/icons/hide_button.png");
    private static final ResourceLocation VOLUMES = new ResourceLocation(Voicechat.MODID + "/textures/icons/adjust_volumes.png");
    private static final ResourceLocation SPEAKER = new ResourceLocation(Voicechat.MODID + "/textures/icons/speaker_button.png");
    private static final ResourceLocation RECORD = new ResourceLocation(Voicechat.MODID + "/textures/icons/record_button.png");
    private static final ITextComponent TITLE = new TranslationTextComponent("gui.voicechat.voice_chat.title");
    private static final ITextComponent SETTINGS = new TranslationTextComponent("message.voicechat.settings");
    private static final ITextComponent GROUP = new TranslationTextComponent("message.voicechat.group");
    private static final ITextComponent ADJUST_PLAYER_VOLUMES = new TranslationTextComponent("message.voicechat.adjust_volumes");

    private ToggleImageButton mute;
    private ToggleImageButton disable;
    private HoverArea recordingHoverArea;

    private final ClientPlayerStateManager stateManager;

    public VoiceChatScreen() {
        super(TITLE, 195, 76);
        stateManager = ClientManager.getPlayerStateManager();
    }

    @Override
    protected void init() {
        if (!VoicechatGate.isEnabled()) {
            this.minecraft.displayGuiScreen(null);
            return;
        }
        super.init();
        @Nullable ClientVoicechat client = ClientManager.getClient();

        mute = new ToggleImageButton(guiLeft + 6, guiTop + ySize - 6 - 20, MICROPHONE, stateManager::isMuted, button -> {
            stateManager.setMuted(!stateManager.isMuted());
        }, new MuteTooltipSupplier(this, stateManager));
        addButton(mute);

        disable = new ToggleImageButton(guiLeft + 6 + 20 + 2, guiTop + ySize - 6 - 20, SPEAKER, stateManager::isDisabled, button -> {
            stateManager.setDisabled(!stateManager.isDisabled());
        }, new DisableTooltipSupplier(this, stateManager));
        addButton(disable);

        ImageButton volumes = new ImageButton(guiLeft + 6 + 20 + 2 + 20 + 2, guiTop + ySize - 6 - 20, VOLUMES, button -> {
            minecraft.displayGuiScreen(new AdjustVolumesScreen());
        }, (button, matrices, mouseX, mouseY) -> {
            renderTooltip(matrices, ADJUST_PLAYER_VOLUMES, mouseX, mouseY);
        });
        addButton(volumes);

        if (client != null && VoicechatClient.CLIENT_CONFIG.useNatives.get()) {
            if (client.getRecorder() != null || (client.getConnection() != null && client.getConnection().getData().allowRecording())) {
                ToggleImageButton record = new ToggleImageButton(guiLeft + xSize - 6 - 20 - 2 - 20, guiTop + ySize - 6 - 20, RECORD, () -> ClientManager.getClient() != null && ClientManager.getClient().getRecorder() != null, button -> toggleRecording(), new RecordingTooltipSupplier(this));
                addButton(record);
            }
        }

        ToggleImageButton hide = new ToggleImageButton(guiLeft + xSize - 6 - 20, guiTop + ySize - 6 - 20, HIDE, VoicechatClient.CLIENT_CONFIG.hideIcons::get, button -> {
            VoicechatClient.CLIENT_CONFIG.hideIcons.set(!VoicechatClient.CLIENT_CONFIG.hideIcons.get()).save();
        }, new HideTooltipSupplier(this));
        addButton(hide);

        Button settings = new Button(guiLeft + 6, guiTop + 6 + 15, 75, 20, SETTINGS, button -> {
            minecraft.displayGuiScreen(new VoiceChatSettingsScreen());
        });
        addButton(settings);

        Button group = new Button(guiLeft + xSize - 6 - 75 + 1, guiTop + 6 + 15, 75, 20, GROUP, button -> {
            ClientGroup g = stateManager.getGroup();
            if (g != null) {
                minecraft.displayGuiScreen(new GroupScreen(g));
            } else {
                minecraft.displayGuiScreen(new JoinGroupScreen());
            }
        });
        addButton(group);

        group.active = client != null && client.getConnection() != null && client.getConnection().getData().groupsEnabled();
        recordingHoverArea = new HoverArea(6 + 20 + 2 + 20 + 2 + 20 + 2, ySize - 6 - 20, xSize - ((6 + 20 + 2 + 20 + 2) * 2 + 20 + 2), 20);

        checkButtons();
    }

    @Override
    public void tick() {
        super.tick();
        checkButtons();
    }

    private void checkButtons() {
        mute.active = MuteTooltipSupplier.canMuteMic();
        disable.active = stateManager.canEnable();
    }

    private void toggleRecording() {
        ClientVoicechat c = ClientManager.getClient();
        if (c == null) {
            return;
        }
        c.toggleRecording();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == ClientCompatibilityManager.INSTANCE.getBoundKeyOf(KEY_VOICE_CHAT).getKeyCode()) {
            minecraft.displayGuiScreen(null);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void renderBackground(MatrixStack poseStack, int mouseX, int mouseY, float delta) {
        minecraft.getTextureManager().bindTexture(TEXTURE);
        blit(poseStack, guiLeft, guiTop, 0, 0, xSize, ySize);
    }

    @Override
    public void renderForeground(MatrixStack poseStack, int mouseX, int mouseY, float delta) {
        int titleWidth = font.getStringPropertyWidth(TITLE);
        font.func_238422_b_(poseStack, TITLE.func_241878_f(), (float) (guiLeft + (xSize - titleWidth) / 2), guiTop + 7, FONT_COLOR);

        ClientVoicechat client = ClientManager.getClient();
        if (client != null && client.getRecorder() != null) {
            AudioRecorder recorder = client.getRecorder();
            StringTextComponent time = new StringTextComponent(recorder.getDuration());
            font.func_243248_b(poseStack, time.mergeStyle(TextFormatting.DARK_RED), guiLeft + recordingHoverArea.posX() + recordingHoverArea.width() / 2F - font.getStringPropertyWidth(time) / 2F, guiTop + recordingHoverArea.posY() + recordingHoverArea.height() / 2F - font.FONT_HEIGHT / 2F, 0);

            if (recordingHoverArea.isHovered(guiLeft, guiTop, mouseX, mouseY)) {
                renderTooltip(poseStack, new TranslationTextComponent("message.voicechat.storage_size", recorder.getStorage()), mouseX, mouseY);
            }
        }
    }

}
