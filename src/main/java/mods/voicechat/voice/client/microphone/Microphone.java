package mods.voicechat.voice.client.microphone;

import mods.voicechat.voice.client.MicrophoneException;

public interface Microphone {

    void open() throws MicrophoneException;

    void start();

    void stop();

    void close();

    boolean isOpen();

    boolean isStarted();

    int available();

    short[] read();

}
