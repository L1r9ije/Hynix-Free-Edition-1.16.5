package mods.voicechat.plugins.impl.events;

import mods.voicechat.api.VolumeCategory;
import mods.voicechat.api.events.RegisterVolumeCategoryEvent;

public class RegisterVolumeCategoryEventImpl extends VolumeCategoryEventImpl implements RegisterVolumeCategoryEvent {

    public RegisterVolumeCategoryEventImpl(VolumeCategory category) {
        super(category);
    }

}
