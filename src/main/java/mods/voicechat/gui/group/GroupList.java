package mods.voicechat.gui.group;

import mods.voicechat.gui.widgets.ListScreenBase;
import mods.voicechat.gui.widgets.ListScreenListBase;
import mods.voicechat.voice.client.ClientManager;
import mods.voicechat.voice.common.PlayerState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class GroupList extends ListScreenListBase<GroupEntry> {

    protected final ListScreenBase parent;

    public GroupList(ListScreenBase parent, int width, int height, int top, int size) {
        super(width, height, top, size);
        this.parent = parent;
        func_244605_b(false);
        func_244606_c(false);
        updateMembers();
    }

    public static void update() {
        Screen screen = Minecraft.getInstance().currentScreen;
        if (screen instanceof GroupScreen) {
            GroupScreen groupScreen = (GroupScreen) screen;
            groupScreen.groupList.updateMembers();
        }
    }

    public void updateMembers() {
        List<PlayerState> playerStates = ClientManager.getPlayerStateManager().getPlayerStates(true);
        UUID group = ClientManager.getPlayerStateManager().getGroupID();
        if (group == null) {
            clearEntries();
            minecraft.displayGuiScreen(null);
            return;
        }
        boolean changed = false;
        List<GroupEntry> toRemove = new LinkedList<>();
        for (GroupEntry entry : getEventListeners()) {
            PlayerState state = ClientManager.getPlayerStateManager().getState(entry.getState().getUuid());
            if (state == null) {
                toRemove.add(entry);
                changed = true;
                continue;
            }
            entry.setState(state);
            if (!isInGroup(state, group)) {
                toRemove.add(entry);
                changed = true;
            }
        }
        for (GroupEntry entry : toRemove) {
            removeEntry(entry);
        }
        for (PlayerState state : playerStates) {
            if (isInGroup(state, group)) {
                if (getEventListeners().stream().noneMatch(groupEntry -> groupEntry.getState().getUuid().equals(state.getUuid()))) {
                    addEntry(new GroupEntry(parent, state));
                    changed = true;
                }
            }
        }

        if (changed) {
            getEventListeners().sort(Comparator.comparing(o -> o.getState().getName()));
        }
    }

    private boolean isInGroup(PlayerState state, UUID group) {
        return state.hasGroup() && state.getGroup().equals(group);
    }

}
