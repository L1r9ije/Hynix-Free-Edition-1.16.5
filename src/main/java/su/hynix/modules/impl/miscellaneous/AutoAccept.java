package su.hynix.modules.impl.miscellaneous;

import com.darkmagician6.eventapi.EventTarget;
import net.minecraft.network.play.server.SChatPacket;
import su.hynix.events.EventPacket;
import su.hynix.hynix;
import su.hynix.managers.impl.FriendManager;
import su.hynix.modules.Category;
import su.hynix.modules.Module;
import su.hynix.modules.api.constructors.impl.BooleanSetting;
import su.hynix.utils.misc.ServerUtil;

import java.util.Arrays;

public class AutoAccept extends Module {
    private static final String[] TELEPORT_REQUESTS = {
            "телепортироваться",
            "has requested teleport",
            "просит к вам телепортироваться",
            "запрашивает телепорт к вам"
    };

    private final BooleanSetting onlyFriend = new BooleanSetting("Принимать запросы только от друзей", true);

    public AutoAccept() {
        super("AutoAccept", "Автоматически принимает запросы на телепортацию от других игроков", Category.Miscellaneous);
        addSettings(onlyFriend);
    }

    @EventTarget
    public void onPacket(EventPacket event) {
        if (mc.player == null || mc.world == null || ServerUtil.isPvP() || !(event.getPacket() instanceof SChatPacket chatPacket)) {
            return;
        }

        String message = chatPacket.getChatComponent().getString().toLowerCase();
        if (Arrays.stream(TELEPORT_REQUESTS).anyMatch(message::contains)) {
            if (onlyFriend.get() && !isFriendRequest(message)) {
                return;
            }
            mc.player.sendChatMessage("/tpaccept");
        }
    }

    private boolean isFriendRequest(String message) {
        NameProtect nameProtect = (NameProtect) hynix.getInstance().getModuleManager().getModule(NameProtect.class);
        if (nameProtect.isEnabled() && NameProtect.protectfriends.get() && message.contains("protected")) {
            return true;
        }
        for (FriendManager.FriendEntry friend : hynix.getInstance().getFriendManager().getFriends()) {
            String friendName = friend.name();
            if (message.contains(friendName.toLowerCase())) {
                return true;
            }
        }
        return false;
    }
}