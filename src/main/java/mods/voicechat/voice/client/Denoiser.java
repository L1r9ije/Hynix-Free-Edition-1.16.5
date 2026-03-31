package mods.voicechat.voice.client;

import de.maxhenkel.rnnoise4j.UnknownPlatformException;
import mods.voicechat.Voicechat;
import mods.voicechat.intercompatibility.CrossSideManager;
import mods.voicechat.voice.common.Utils;

import javax.annotation.Nullable;
import java.io.IOException;

public class Denoiser extends de.maxhenkel.rnnoise4j.Denoiser {

    private Denoiser() throws IOException, UnknownPlatformException {
        super();
    }

    @Nullable
    public static Denoiser createDenoiser() {
        if (!CrossSideManager.get().useNatives()) {
            return null;
        }
        return Utils.createSafe(Denoiser::new, e -> {
            Voicechat.LOGGER.warn("Failed to load RNNoise", e);
        });
    }

}
