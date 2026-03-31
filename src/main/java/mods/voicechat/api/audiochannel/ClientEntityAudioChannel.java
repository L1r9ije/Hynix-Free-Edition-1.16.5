package mods.voicechat.api.audiochannel;

public interface ClientEntityAudioChannel extends ClientAudioChannel {

    /**
     * @return if the entity is whispering
     */
    boolean isWhispering();

    /**
     * @param whispering if the entity should whisper
     */
    void setWhispering(boolean whispering);

    /**
     * @return the distance, the audio can be heard
     */
    float getDistance();

    /**
     * @param distance the distance, the audio can be heard
     */
    void setDistance(float distance);

}
