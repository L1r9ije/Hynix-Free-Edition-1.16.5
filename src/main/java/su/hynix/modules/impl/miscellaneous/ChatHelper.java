package su.hynix.modules.impl.miscellaneous;

import su.hynix.modules.Category;
import su.hynix.modules.Module;
import su.hynix.modules.api.constructors.impl.BooleanSetting;

public class ChatHelper extends Module {

    public static BooleanSetting chatHistory = new BooleanSetting("История чата", true);
    public BooleanSetting antiSpam = new BooleanSetting("Анти Спам", true);


    public ChatHelper() {
        super("ChatHelper", "Вспомогательный набор функций для чата", Category.Miscellaneous);
        addSettings(chatHistory, antiSpam);
    }
}
