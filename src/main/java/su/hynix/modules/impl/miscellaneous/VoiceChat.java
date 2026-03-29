package su.hynix.modules.impl.miscellaneous;

import lombok.Getter;
import mods.voicechat.gui.VoiceChatScreen;
import mods.voicechat.gui.onboarding.OnboardingManager;
import mods.voicechat.voice.client.ClientManager;
import mods.voicechat.voice.client.ClientVoicechat;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import su.hynix.managers.impl.notificationmanager.NotificationManager;
import su.hynix.modules.Category;
import su.hynix.modules.Module;
import su.hynix.modules.api.constructors.impl.ClickSetting;
import su.hynix.utils.misc.ChatUtil;
import su.hynix.utils.render.ColorUtil;

@Getter
public class VoiceChat extends Module {
    public ClickSetting voiceModeGui = new ClickSetting("Открыть настройки", () -> {
        mc.displayGuiScreen(new VoiceChatScreen());
    });
    public ClickSetting instructionGui = new ClickSetting("Открыть гайд", () -> {
        OnboardingManager.startOnboarding(null);
    });

    public VoiceChat() {
        super("Voice Chat", "Позволяет общаться с другими игроками с помощью голосового чата", Category.Miscellaneous);
        addSettings(instructionGui, voiceModeGui);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        ChatUtil.addText(new StringTextComponent("Перезайдите в мир, для работы Voice Chat").mergeStyle(TextFormatting.GREEN, TextFormatting.BOLD));
        NotificationManager.addNotification("G", "Перезайдите в мир, для работы Voice Chat", ColorUtil.hex("#54FB54FF"));
    }

    @Override
    public void onDisable() {
        super.onDisable();
        ClientVoicechat client = ClientManager.getClient();
        if (client != null) {
            client.close();
        }
        ChatUtil.addText(new StringTextComponent("Подключение разорвано").mergeStyle(TextFormatting.RED, TextFormatting.BOLD));
        NotificationManager.addNotification("M", "Подключение разорвано", ColorUtil.hex("#FB5454FF"));
    }
}