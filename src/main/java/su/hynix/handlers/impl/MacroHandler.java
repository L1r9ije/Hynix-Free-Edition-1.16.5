package su.hynix.handlers.impl;

import com.darkmagician6.eventapi.EventTarget;
import su.hynix.events.EventKey;
import su.hynix.hynix;
import su.hynix.managers.impl.MacroManager;
import su.hynix.utils.Wrapper;

public class MacroHandler implements Wrapper {

    @EventTarget
    public void onKey(EventKey e) {
        if (mc.player == null || e.isHold() || mc.world == null) return;
        for (MacroManager.MacroEntry m : hynix.getInstance().getMacroManager().getMacros()) {
            if (m.keyCode() == e.getKey()) {
                String cmd = m.command();
                if (cmd != null && !cmd.isEmpty()) {
                    mc.player.sendChatMessage(cmd);
                }
            }
        }
    }
}


