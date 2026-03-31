package mods.voicechat.plugins.impl.opus;

import de.maxhenkel.opus4j.UnknownPlatformException;
import mods.voicechat.api.opus.OpusDecoder;

import java.io.IOException;

public class NativeOpusDecoderImpl extends de.maxhenkel.opus4j.OpusDecoder implements OpusDecoder {

    public NativeOpusDecoderImpl(int sampleRate, int channels) throws IOException, UnknownPlatformException {
        super(sampleRate, channels);
    }

}
