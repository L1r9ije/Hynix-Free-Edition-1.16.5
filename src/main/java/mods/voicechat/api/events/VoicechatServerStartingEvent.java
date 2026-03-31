package mods.voicechat.api.events;

import mods.voicechat.api.VoicechatSocket;

import javax.annotation.Nullable;

public interface VoicechatServerStartingEvent extends ServerEvent {

    /**
     * @return the custom socket implementation or <code>null</code> to use voice chats default one
     */
    @Nullable
    VoicechatSocket getSocketImplementation();

    /**
     * Sets a custom implementation of the socket used for voice chat traffic.
     *
     * @param socket the custom socket implementation
     */
    void setSocketImplementation(VoicechatSocket socket);

}
