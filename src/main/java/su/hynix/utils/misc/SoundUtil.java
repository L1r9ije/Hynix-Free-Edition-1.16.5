package su.hynix.utils.misc;

import su.hynix.hynix;
import su.hynix.modules.impl.miscellaneous.ToggleSounds;
import su.hynix.utils.Wrapper;

import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.Objects;

public class SoundUtil implements Wrapper {

    public static void playSound(final String location) {
        if (hynix.getInstance().getModuleManager().getModule(ToggleSounds.class).isEnabled()) {
            try (AudioInputStream in = AudioSystem.getAudioInputStream(new BufferedInputStream(Objects.requireNonNull(SoundUtil.class.getResourceAsStream("/assets/minecraft/hynix/sounds/" + location + ".wav"))))) {
                AudioFormat baseFormat = in.getFormat();
                AudioFormat decodedFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, baseFormat.getSampleRate(), 16, baseFormat.getChannels(), baseFormat.getChannels() * 2, baseFormat.getSampleRate(), false);
                try (AudioInputStream din = AudioSystem.getAudioInputStream(decodedFormat, in)) {
                    Clip clip = AudioSystem.getClip();
                    clip.open(din);
                    setVolume(clip, ToggleSounds.volume.get() / 100F);
                    clip.start();
                }
            } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
                e.printStackTrace();
            }
        }
    }

    private static void setVolume(Clip clip, double volume) {
        if (volume < 0) volume = 0;
        if (volume > 1) volume = 1;
        FloatControl volumeControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
        float min = volumeControl.getMinimum();
        float max = volumeControl.getMaximum();

        if (volume == 0) {
            volumeControl.setValue(min);
            return;
        }

        float dB = (float) (20.0 * Math.log10(volume));
        if (dB < min) dB = min;
        if (dB > max) dB = max;
        volumeControl.setValue(dB);
    }
}