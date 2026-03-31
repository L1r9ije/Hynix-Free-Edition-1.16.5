package mods.voicechat.plugins.impl.events;

import mods.voicechat.api.VolumeCategory;
import mods.voicechat.api.events.VolumeCategoryEvent;

public class VolumeCategoryEventImpl extends ServerEventImpl implements VolumeCategoryEvent {

    private final VolumeCategory category;

    public VolumeCategoryEventImpl(VolumeCategory category) {
        this.category = category;
    }

    @Override
    public VolumeCategory getVolumeCategory() {
        return category;
    }

    @Override
    public boolean isCancellable() {
        return false;
    }
}
