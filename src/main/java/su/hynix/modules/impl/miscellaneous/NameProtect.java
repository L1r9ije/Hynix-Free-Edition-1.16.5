package su.hynix.modules.impl.miscellaneous;

import su.hynix.hynix;
import su.hynix.managers.impl.FriendManager;
import su.hynix.modules.Category;
import su.hynix.modules.Module;
import su.hynix.modules.api.constructors.impl.BooleanSetting;

public class NameProtect extends Module {
    public static BooleanSetting protectfriends = new BooleanSetting("Скрывать друзей", false);

    public NameProtect() {
        super("Name Protect", "Скрывает ваш ник, обеспечивая конфиденциальность и защиту от преследования", Category.Miscellaneous);
        addSettings(protectfriends);
    }

    public static String replaceName(String name) {
        name = name.replace(mc.session.getUsername(), "Protected");

        if (protectfriends.get()) {
            for (FriendManager.FriendEntry friend : hynix.getInstance().getFriendManager().getFriends()) {
                name = name.replace(friend.name(), "Protected");
            }
        }

        return name;
    }
}