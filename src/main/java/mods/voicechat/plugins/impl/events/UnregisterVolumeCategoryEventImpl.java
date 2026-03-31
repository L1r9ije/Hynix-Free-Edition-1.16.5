package mods.voicechat.plugins.impl.events;

import mods.voicechat.api.VolumeCategory;
import mods.voicechat.api.events.UnregisterVolumeCategoryEvent;

public class UnregisterVolumeCategoryEventImpl extends VolumeCategoryEventImpl implements UnregisterVolumeCategoryEvent {

    public UnregisterVolumeCategoryEventImpl(VolumeCategory category) {
        super(category);
    }

}
