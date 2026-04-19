package su.hynix.modules.impl.miscellaneous;

import com.darkmagician6.eventapi.EventTarget;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.TextFormatting;
import su.hynix.events.EventKey;
import su.hynix.hynix;
import su.hynix.modules.Category;
import su.hynix.modules.Module;
import su.hynix.modules.api.constructors.impl.BindSetting;
import su.hynix.utils.misc.ChatUtil;

public class ClickFriend extends Module {
    private final BindSetting key = new BindSetting("Кнопка взаимодействия");

    public ClickFriend() {
        super("ClickFriend", "Упрощает добавление друзей, позволяя добавить их в список друзей одним кликом", Category.Miscellaneous);
        addSettings(key);
    }

    @EventTarget
    public void onEvent(EventKey event) {
        if (event.getKey() == key.get() && event.isHold() && mc.pointedEntity instanceof PlayerEntity entity) {
            String entityName = entity.getName().getString();

            if (hynix.getInstance().getFriendManager().isFriend(entityName)) {
                hynix.getInstance().getFriendManager().removeFriend(entityName);
                ChatUtil.addText(TextFormatting.RESET + entityName + " Удален из списка друзей!");
            } else {
                hynix.getInstance().getFriendManager().addFriend(entityName);
                ChatUtil.addText(TextFormatting.RESET + entityName + " Добавлен в список друзей!");
            }
        }
    }
}