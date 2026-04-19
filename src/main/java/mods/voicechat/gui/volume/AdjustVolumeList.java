package mods.voicechat.gui.volume;

import com.google.common.collect.Lists;
import mods.voicechat.VoicechatClient;
import mods.voicechat.gui.widgets.ListScreenListBase;
import mods.voicechat.plugins.impl.VolumeCategoryImpl;
import mods.voicechat.voice.client.ClientManager;
import mods.voicechat.voice.common.PlayerState;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Util;

import java.util.*;

public class AdjustVolumeList extends ListScreenListBase<VolumeEntry> {

    protected final List<VolumeEntry> entries;
    protected AdjustVolumesScreen screen;
    protected String filter;

    public AdjustVolumeList(int width, int height, int top, int size, AdjustVolumesScreen screen) {
        super(width, height, top, size);
        this.screen = screen;
        this.entries = Lists.newArrayList();
        this.filter = "";
        func_244605_b(false);
        func_244606_c(false);
        updateEntryList();
    }

    public static void update() {
        if (Minecraft.getInstance().currentScreen instanceof AdjustVolumesScreen) {
            ((AdjustVolumesScreen) Minecraft.getInstance().currentScreen).volumeList.updateEntryList();
        }
    }

    public void updateEntryList() {
        Collection<PlayerState> onlinePlayers = ClientManager.getPlayerStateManager().getPlayerStates(false);
        entries.clear();

        for (VolumeCategoryImpl category : ClientManager.getCategoryManager().getCategories()) {
            entries.add(new CategoryVolumeEntry(category, screen));
        }

        for (PlayerState state : onlinePlayers) {
            entries.add(new PlayerVolumeEntry(state, screen));
        }

        if (VoicechatClient.CLIENT_CONFIG.offlinePlayerVolumeAdjustment.get()) {
            addOfflinePlayers(onlinePlayers);
        }

        updateFilter();
    }

    private void addOfflinePlayers(Collection<PlayerState> onlinePlayers) {
        for (UUID uuid : VoicechatClient.VOLUME_CONFIG.getPlayerVolumes().keySet()) {
            if (uuid.equals(Util.DUMMY_UUID)) {
                continue;
            }
            if (onlinePlayers.stream().anyMatch(state -> uuid.equals(state.getUuid()))) {
                continue;
            }

            String name = VoicechatClient.USERNAME_CACHE.getUsername(uuid);

            if (name == null) {
                continue;
            }

            entries.add(new PlayerVolumeEntry(new PlayerState(uuid, name, false, true), screen));
        }
    }

    public void updateFilter() {
        clearEntries();
        List<VolumeEntry> filteredEntries = new ArrayList<>(entries);
        if (!filter.isEmpty()) {
            filteredEntries.removeIf(volumeEntry -> {
                if (volumeEntry instanceof PlayerVolumeEntry playerVolumeEntry) {
                    return playerVolumeEntry.getState() == null || !playerVolumeEntry.getState().getName().toLowerCase(Locale.ROOT).contains(filter);
                } else if (volumeEntry instanceof CategoryVolumeEntry categoryVolumeEntry) {
                    return !categoryVolumeEntry.getCategory().name().toLowerCase(Locale.ROOT).contains(filter);
                }
                return true;
            });
        }
        filteredEntries.sort((e1, e2) -> {
            if (!e1.getClass().equals(e2.getClass())) {
                if (e1 instanceof PlayerVolumeEntry) {
                    return 1;
                } else {
                    return -1;
                }
            }

            return volumeEntryToString(e1).compareToIgnoreCase(volumeEntryToString(e2));
        });
        if (filter.isEmpty()) {
            filteredEntries.add(0, new PlayerVolumeEntry(null, screen));
        }
        replaceEntries(filteredEntries);
    }

    private String volumeEntryToString(VolumeEntry entry) {
        if (entry instanceof PlayerVolumeEntry playerVolumeEntry) {
            return playerVolumeEntry.getState() == null ? "" : playerVolumeEntry.getState().getName();
        } else if (entry instanceof CategoryVolumeEntry categoryVolumeEntry) {
            return categoryVolumeEntry.getCategory().name();
        }
        return "";
    }

    public void setFilter(String filter) {
        this.filter = filter;
        updateFilter();
    }

    public boolean isEmpty() {
        return getEventListeners().isEmpty();
    }

}
