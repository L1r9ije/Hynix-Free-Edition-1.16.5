package mods.voicechat.api.events;

import mods.voicechat.api.VolumeCategory;

public interface VolumeCategoryEvent extends ServerEvent {

    /**
     * @return the volume category
     */
    VolumeCategory getVolumeCategory();

}
